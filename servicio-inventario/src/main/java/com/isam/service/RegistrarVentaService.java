package com.isam.service;

import com.isam.dto.movimiento.MovimientoInventarioDto;
import com.isam.dto.producto.ConsultarProductoDto;
import com.isam.dto.venta.ItemVentaDto;
import com.isam.dto.venta.RegistrarVentaRequestDto;
import com.isam.dto.venta.RegistrarVentaResponseDto;
import com.isam.model.EstadoLote;
import com.isam.model.Inventario;
import com.isam.model.Lote;
import com.isam.model.MovimientoInventario;
import com.isam.model.TipoMovimiento;
import com.isam.model.UnidadMedida;
import com.isam.repository.InventarioRepository;
import com.isam.repository.LoteRepository;
import com.isam.repository.MovimientoInventarioRepository;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarVentaService {

    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final InventarioRepository inventarioRepository;
    private final com.isam.grpc.client.CatalogoGrpcClient catalogoGrpcClient;

    /**
     * Registra una venta completa.
     * Si el producto se vendió, se descuenta el stock aunque el sistema diga que hay 0.
     * Esto puede resultar en stock negativo, lo cual genera una discrepancia para auditoría posterior.
     */
    @Transactional
    public RegistrarVentaResponseDto registrarVenta(RegistrarVentaRequestDto dto) {
        log.info("Registrando venta: Ticket='{}', Items={}", dto.numeroTicket(), dto.items().size());

        List<MovimientoInventarioDto> movimientosRespuesta = new ArrayList<>();

        // Procesar cada item de la venta
        for (ItemVentaDto item : dto.items()) {
            movimientosRespuesta.addAll(procesarItemVenta(item, dto.numeroTicket()));
        }

        String mensaje = String.format(
            "Venta registrada exitosamente. Ticket: %s, Movimientos generados: %d",
            dto.numeroTicket(), movimientosRespuesta.size()
        );

        log.info(mensaje);

        return new RegistrarVentaResponseDto(movimientosRespuesta);
    }

    private List<MovimientoInventarioDto> procesarItemVenta(ItemVentaDto item, String numeroTicket) {
        List<MovimientoInventarioDto> dtosSalida = new ArrayList<>();
        List<Lote> lotesAActualizar = new ArrayList<>();
        List<MovimientoInventario> movimientosAGuardar = new ArrayList<>();

        BigDecimal cantidadPendiente = item.cantidad();
        String sku = item.sku();
        UnidadMedida unidadMedida = item.unidadMedida();


        // Obtener entidad Inventario global (para actualizar totales)
        // AUTO-REPARACIÓN: Si no existe inventario, consultamos catálogo para crearlo al vuelo
        Inventario inventarioGlobal = inventarioRepository.findBySku(sku)
            .orElseGet(() -> recuperarOCrearInventario(sku, unidadMedida));

        // Buscar lotes con stock positivo en estantería  con Estrategia FIFO)
        // TODO - Tenemos que implementar un sitema que permita usar otras estrategias, como FEFO.
        List<Lote> lotes = loteRepository.findBySkuAndCantidadEstanteriaGreaterThan(sku, BigDecimal.ZERO);

        // Iterar sobre lotes con stock para descontar
        Lote ultimoLoteUsado = null;

        for (Lote lote : lotes) {
            if (cantidadPendiente.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal stockDisponible = lote.getCantidadEstanteria();
            BigDecimal aDescontar = cantidadPendiente.min(stockDisponible);

            // Actualizar lote
            lote.setCantidadEstanteria(stockDisponible.subtract(aDescontar));
            lotesAActualizar.add(lote);
            ultimoLoteUsado = lote;

            // Crear movimiento
            movimientosAGuardar.add(crearMovimiento(sku, lote.getIdLote(), aDescontar, lote.getUnidadMedida(), numeroTicket));

            // Actualizar pendiente
            cantidadPendiente = cantidadPendiente.subtract(aDescontar);
        }

        // MANEJO DE STOCK INSUFICIENTE - VENTA FORZADA
        if (cantidadPendiente.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("Discrepancia de inventario detectada: Venta forzada para SKU '{}'. Faltan {} unidades.", 
                     sku, cantidadPendiente);

            // Estrategia: Asignar el negativo al último lote usado o buscar cualquiera disponible
            if (ultimoLoteUsado == null) {
                // Caso extremo - No había ningún lote con stock > 0. Buscamos cualquier lote activo.
                Optional<Lote> loteRespaldo = loteRepository.findBySku(sku).stream()
                        .filter(l -> l.getEstado() == EstadoLote.DISPONIBLE)
                        .findFirst();
                
                if (loteRespaldo.isPresent()) {
                    ultimoLoteUsado = loteRespaldo.get();
                }
            }

            if (ultimoLoteUsado != null) {
                // Forzamos el negativo en el lote seleccionado
                // NOTE - Si el lote ya estaba en lotesAActualizar, hibernate manejará la referencia correctamente
                BigDecimal nuevoStock = ultimoLoteUsado.getCantidadEstanteria().subtract(cantidadPendiente);
                ultimoLoteUsado.setCantidadEstanteria(nuevoStock);
                
                // Si no estaba en la lista de actualizar (porque lo acabamos de recuperar del backup), lo añadimos
                if (!lotesAActualizar.contains(ultimoLoteUsado)) {
                    lotesAActualizar.add(ultimoLoteUsado);
                }

                movimientosAGuardar.add(crearMovimiento(
                    sku, ultimoLoteUsado.getIdLote(), cantidadPendiente, 
                    ultimoLoteUsado.getUnidadMedida(), numeroTicket + " (Stock Forzado)"
                ));
            } else {
                // Caso Crítico - No existe NINGUN lote para este producto.
                // Creamos un movimiento huerfano para dejar constancia, sin ID de lote.
                log.error("CRÍTICO: Venta de SKU '{}' sin lotes existentes. Se registra movimiento sin lote.", sku);
                movimientosAGuardar.add(crearMovimiento(
                    sku, null, cantidadPendiente, 
                    inventarioGlobal.getUnidadMedida(), numeroTicket + " (Sin Lote)"
                ));
            }
        }

        // Actualizar el Inventario Global Totales
        BigDecimal totalDescontado = item.cantidad(); // Siempre descontamos lo que se vendió físicamente
        inventarioGlobal.setCantidadEstanteria(inventarioGlobal.getCantidadEstanteria().subtract(totalDescontado));
        
        // Guardar todo en Batch 
        loteRepository.saveAll(lotesAActualizar);
        inventarioRepository.save(inventarioGlobal);
        List<MovimientoInventario> movimientosGuardados = movimientoInventarioRepository.saveAll(movimientosAGuardar);

        // Mapear a DTOs de salida
        for (MovimientoInventario mov : movimientosGuardados) {
            dtosSalida.add(new MovimientoInventarioDto(
                mov.getIdMovimiento(),
                mov.getSku(),
                mov.getIdLote(),
                mov.getTipoMovimiento().name(),
                mov.getCantidad(),
                mov.getUnidadMedida().name(),
                mov.getFechaHora().toString(),
                mov.getIdUsuario(),
                mov.getMotivo(),
                mov.getObservaciones()
            ));
        }

        return dtosSalida;
    }

    /**
     * Intenta recuperar datos del catálogo para crear un registro de inventario inicial.
     * Si el producto no existe en catálogo o falla la comunicación, usa valores por defecto (Fallback)
     * para asegurar que la venta quede registrada contablemente en inventario.
     */
    private Inventario recuperarOCrearInventario(String sku, UnidadMedida udMedida) {
        log.warn("Inventario no encontrado para SKU: {}. Iniciando auto-reparación (Lazy Initialization)...", sku);
        
        // Valores por defecto
        com.isam.model.UnidadMedida unidadMedida = udMedida != null ? udMedida : com.isam.model.UnidadMedida.UNIDAD;
        String ean = null;
        String plu = null;

        try {
            // Consultar al Catálogo
            ConsultarProductoDto productoDto = catalogoGrpcClient.consultarProducto(sku);
            
            // Mapear Unidad de Medida
            if (productoDto.unidadMedida() != null) {
                try {
                    unidadMedida = productoDto.unidadMedida();
                } catch (IllegalArgumentException e) {
                    log.warn("Unidad de medida desconocida en catálogo: {}. Usando UNIDAD.", productoDto.unidadMedida());
                }
            }
            ean = productoDto.ean();
            plu = productoDto.plu();
            
            log.info("Datos recuperados del catálogo para SKU {}: UM={}, EAN={}, PLU={}", sku, unidadMedida, ean, plu);

        } catch (Exception e) {
            // FALLBACK CRÍTICO: El producto no existe ni en catálogo o servicio caído.
            // Creamos el inventario igualmente para no perder la traza del movimiento de salida.
            log.error("FALLBACK CRÍTICO: No se pudo recuperar SKU {} del catálogo (Error: {}). Se creará inventario genérico 'UNIDAD' para registrar la venta.", sku, e.getMessage());
        }

        // Crear inventario con stock 0 para permitir que luego pase a negativo
        Inventario nuevoInventario = new Inventario(sku, ean, plu, unidadMedida);
        nuevoInventario.setEsProvisional(true); // Marcamos como provisional para permitir corrección posterior
        return inventarioRepository.save(nuevoInventario);
    }

    private MovimientoInventario crearMovimiento(String sku, String idLote, BigDecimal cantidad, 
                                                 com.isam.model.UnidadMedida unidadMedida, String referencia) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setSku(sku);
        movimiento.setIdLote(idLote);
        movimiento.setTipoMovimiento(TipoMovimiento.VENTA);
        movimiento.setCantidad(cantidad.abs());
        movimiento.setUnidadMedida(unidadMedida);
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setIdUsuario("SISTEMA_VENTAS");
        movimiento.setMotivo("Venta");
        movimiento.setObservaciones("Ticket: " + referencia);
        return movimiento;
    }
}