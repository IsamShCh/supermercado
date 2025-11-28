package com.isam.service;

import com.isam.model.Inventario;
import com.isam.model.Lote;
import com.isam.model.MovimientoInventario;
import com.isam.repository.InventarioRepository;
import com.isam.repository.LoteRepository;
import com.isam.repository.MovimientoInventarioRepository;
import com.isam.dto.inventario.AjustarInventarioManualRequestDto;
import com.isam.dto.inventario.AjustarInventarioManualResponseDto;
import com.isam.dto.inventario.AjustarAlmacenDto;
import com.isam.dto.inventario.AjustarEstanteriaDto;
import com.isam.dto.inventario.InventarioDto;
import com.isam.model.TipoMovimiento;
import com.isam.model.UnidadMedida;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.grpc.Status;

import java.util.List;

/**
 * Servicio especializado en gestionar ajustes manuales de inventario.
 * Maneja la lógica de ajustes en almacén y estantería, con o sin lote específico.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AjusteInventarioService {

    private final InventarioRepository inventarioRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    /**
     * Aplica un ajuste manual al inventario (AC16).
     * Este método permite realizar ajustes manuales de inventario por varios motivos.
     */
    @Transactional
    public AjustarInventarioManualResponseDto ajustarInventarioManual(AjustarInventarioManualRequestDto dto) {
        
        // Buscar el inventario para este SKU
        Inventario inventario = inventarioRepository.findBySku(dto.sku())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Inventario no encontrado para SKU '" + dto.sku() + "'")
                .asRuntimeException());

        // Validar ubicación del ajuste
        if (dto.ubicacionAjuste() == null) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Debe especificar la ubicación del ajuste (almacén o estantería)")
                .asRuntimeException();
        }

        // Procesar según ubicación
        if (dto.ubicacionAjuste() instanceof AjustarAlmacenDto ajustarAlmacen) {
            return procesarAjusteAlmacen(inventario, dto, ajustarAlmacen);
        } else if (dto.ubicacionAjuste() instanceof AjustarEstanteriaDto ajustarEstanteria) {
            return procesarAjusteEstanteria(inventario, dto, ajustarEstanteria);
        }
        
        throw Status.INVALID_ARGUMENT
            .withDescription("Tipo de ajuste no soportado")
            .asRuntimeException();
    }

    private AjustarInventarioManualResponseDto procesarAjusteAlmacen(
            Inventario inventario,
            AjustarInventarioManualRequestDto dto,
            AjustarAlmacenDto ajustarAlmacen) {
        
        BigDecimal cantidadAjuste = dto.cantidadAjuste();
        
        if (ajustarAlmacen.idLote() != null) {
            // Caso: Se especificó lote específico
            return procesarAjusteAlmacenConLote(inventario, dto, ajustarAlmacen, cantidadAjuste);
        } else {
            // Caso: No se especificó lote, aplicar al total de almacén
            return procesarAjusteAlmacenGeneral(inventario, dto, cantidadAjuste);
        }
    }

    private AjustarInventarioManualResponseDto procesarAjusteAlmacenConLote(
            Inventario inventario,
            AjustarInventarioManualRequestDto dto,
            AjustarAlmacenDto ajustarAlmacen,
            BigDecimal cantidadAjuste) {
        
        // Validar lote
        Lote lote = loteRepository.findById(ajustarAlmacen.idLote())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Lote no encontrado")
                .asRuntimeException());

        // Validar que el lote pertenece al SKU
        if (!lote.getSku().equals(dto.sku())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El lote no pertenece al SKU especificado")
                .asRuntimeException();
        }

        // Validar stock disponible solo si es un ajuste negativo (restar)
        if (cantidadAjuste.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal cantidadAbsoluta = cantidadAjuste.abs();
            if (lote.getCantidadAlmacen().compareTo(cantidadAbsoluta) < 0) {
                throw Status.FAILED_PRECONDITION
                    .withDescription("Stock insuficiente en almacén para el lote especificado")
                    .asRuntimeException();
            }
        }

        // Aplicar ajuste al lote específico respetando el signo
        lote.setCantidadAlmacen(lote.getCantidadAlmacen().add(cantidadAjuste));
        inventario.setCantidadAlmacen(inventario.getCantidadAlmacen().add(cantidadAjuste));
        
        // Guardar cambios
        loteRepository.save(lote);
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        
        // Crear movimiento asociado al lote
        MovimientoInventario movimiento = crearMovimientoAjuste(dto, lote.getIdLote(), cantidadAjuste);
        
        return buildResponse(inventarioActualizado, movimiento);
    }

    private AjustarInventarioManualResponseDto procesarAjusteAlmacenGeneral(
            Inventario inventario,
            AjustarInventarioManualRequestDto dto,
            BigDecimal cantidadAjuste) {
        
        // Validar stock total en almacén solo si es un ajuste negativo (restar)
        if (cantidadAjuste.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal cantidadAbsoluta = cantidadAjuste.abs();
            if (inventario.getCantidadAlmacen().compareTo(cantidadAbsoluta) < 0) {
                throw Status.FAILED_PRECONDITION
                    .withDescription("Stock insuficiente en almacén")
                    .asRuntimeException();
            }
        }

        // Distribuir el ajuste entre lotes (política FIFO) ANTES de aplicar al inventario general
        List<Lote> lotes = loteRepository.findBySkuAndCantidadAlmacenGreaterThan(dto.sku(), BigDecimal.ZERO);
        distribuirAjusteEntreLotesAlmacen(lotes, cantidadAjuste);
        
        // Aplicar ajuste al total de almacén respetando el signo
        inventario.setCantidadAlmacen(inventario.getCantidadAlmacen().add(cantidadAjuste));
        
        // Guardar cambios
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        
        // Crear movimiento sin lote específico
        MovimientoInventario movimiento = crearMovimientoAjuste(dto, null, cantidadAjuste);
        
        return buildResponse(inventarioActualizado, movimiento);
    }

    private AjustarInventarioManualResponseDto procesarAjusteEstanteria(
            Inventario inventario,
            AjustarInventarioManualRequestDto dto,
            AjustarEstanteriaDto ajustarEstanteria) {
        
        BigDecimal cantidadAjuste = dto.cantidadAjuste();
        
        if (ajustarEstanteria.idLote() != null) {
            // Caso: Se especificó lote específico
            return procesarAjusteEstanteriaConLote(inventario, dto, ajustarEstanteria, cantidadAjuste);
        } else {
            // Caso: No se especificó lote, aplicar al total de estantería
            return procesarAjusteEstanteriaGeneral(inventario, dto, cantidadAjuste);
        }
    }

    private AjustarInventarioManualResponseDto procesarAjusteEstanteriaConLote(
            Inventario inventario,
            AjustarInventarioManualRequestDto dto,
            AjustarEstanteriaDto ajustarEstanteria,
            BigDecimal cantidadAjuste) {
        
        // Validar lote
        Lote lote = loteRepository.findById(ajustarEstanteria.idLote())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Lote no encontrado")
                .asRuntimeException());

        // Validar que el lote pertenece al SKU
        if (!lote.getSku().equals(dto.sku())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El lote no pertenece al SKU especificado")
                .asRuntimeException();
        }

        // Validar stock disponible solo si es un ajuste negativo (restar)
        if (cantidadAjuste.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal cantidadAbsoluta = cantidadAjuste.abs();
            if (lote.getCantidadEstanteria().compareTo(cantidadAbsoluta) < 0) {
                throw Status.FAILED_PRECONDITION
                    .withDescription("Stock insuficiente en estantería para el lote especificado")
                    .asRuntimeException();
            }
        }

        // Aplicar ajuste al lote específico respetando el signo
        lote.setCantidadEstanteria(lote.getCantidadEstanteria().add(cantidadAjuste));
        inventario.setCantidadEstanteria(inventario.getCantidadEstanteria().add(cantidadAjuste));
        
        // Guardar cambios
        loteRepository.save(lote);
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        
        // Crear movimiento asociado al lote
        MovimientoInventario movimiento = crearMovimientoAjuste(dto, lote.getIdLote(), cantidadAjuste);
        
        return buildResponse(inventarioActualizado, movimiento);
    }

    private AjustarInventarioManualResponseDto procesarAjusteEstanteriaGeneral(
            Inventario inventario,
            AjustarInventarioManualRequestDto dto,
            BigDecimal cantidadAjuste) {
        
        // Validar stock total en estantería solo si es un ajuste negativo (restar)
        if (cantidadAjuste.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal cantidadAbsoluta = cantidadAjuste.abs();
            if (inventario.getCantidadEstanteria().compareTo(cantidadAbsoluta) < 0) {
                throw Status.FAILED_PRECONDITION
                    .withDescription("Stock insuficiente en estantería")
                    .asRuntimeException();
            }
        }

        // Distribuir el ajuste entre lotes (política FIFO) ANTES de aplicar al inventario general
        List<Lote> lotes = loteRepository.findBySkuAndCantidadEstanteriaGreaterThan(dto.sku(), BigDecimal.ZERO);
        distribuirAjusteEntreLotesEstanteria(lotes, cantidadAjuste);
        
        // Aplicar ajuste al total de estantería respetando el signo
        inventario.setCantidadEstanteria(inventario.getCantidadEstanteria().add(cantidadAjuste));
        
        // Guardar cambios
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        
        // Crear movimiento sin lote específico
        MovimientoInventario movimiento = crearMovimientoAjuste(dto, null, cantidadAjuste);
        
        return buildResponse(inventarioActualizado, movimiento);
    }

    private void distribuirAjusteEntreLotesAlmacen(List<Lote> lotes, BigDecimal cantidadTotal) {
        // Si es un ajuste positivo (suma), distribuimos entre todos los lotes
        if (cantidadTotal.compareTo(BigDecimal.ZERO) > 0) {
            distribuirSumaEntreLotesAlmacen(lotes, cantidadTotal);
        } else {
            // Si es un ajuste negativo (resta), aplicamos la lógica FIFO existente
            BigDecimal restante = cantidadTotal.abs();
            
            for (Lote lote : lotes) {
                if (restante.compareTo(BigDecimal.ZERO) <= 0) break;
                
                BigDecimal cantidadLote = lote.getCantidadAlmacen();
                BigDecimal aDeducir = cantidadLote.min(restante);
                
                lote.setCantidadAlmacen(lote.getCantidadAlmacen().subtract(aDeducir));
                restante = restante.subtract(aDeducir);
                
                loteRepository.save(lote);
            }
        }
    }

    private void distribuirAjusteEntreLotesEstanteria(List<Lote> lotes, BigDecimal cantidadTotal) {
        // Si es un ajuste positivo (suma), distribuimos entre todos los lotes
        if (cantidadTotal.compareTo(BigDecimal.ZERO) > 0) {
            distribuirSumaEntreLotesEstanteria(lotes, cantidadTotal);
        } else {
            // Si es un ajuste negativo (resta), aplicamos la lógica FIFO existente
            BigDecimal restante = cantidadTotal.abs();
            
            for (Lote lote : lotes) {
                if (restante.compareTo(BigDecimal.ZERO) <= 0) break;
                
                BigDecimal cantidadLote = lote.getCantidadEstanteria();
                BigDecimal aDeducir = cantidadLote.min(restante);
                
                lote.setCantidadEstanteria(lote.getCantidadEstanteria().subtract(aDeducir));
                restante = restante.subtract(aDeducir);
                
                loteRepository.save(lote);
            }
        }
    }

    private void distribuirSumaEntreLotesAlmacen(List<Lote> lotes, BigDecimal cantidadTotal) {
        // Para sumas, distribuimos proporcionalmente entre todos los lotes existentes
        if (lotes.isEmpty()) return;
        
        BigDecimal restante = cantidadTotal;
        
        for (int i = 0; i < lotes.size() && restante.compareTo(BigDecimal.ZERO) > 0; i++) {
            Lote lote = lotes.get(i);
            
            // Si es el último lote, le asignamos todo lo que queda
            BigDecimal aSumar = (i == lotes.size() - 1) ? restante : restante.divide(BigDecimal.valueOf(lotes.size() - i), 3, BigDecimal.ROUND_HALF_UP);
            
            lote.setCantidadAlmacen(lote.getCantidadAlmacen().add(aSumar));
            restante = restante.subtract(aSumar);
            
            loteRepository.save(lote);
        }
    }

    private void distribuirSumaEntreLotesEstanteria(List<Lote> lotes, BigDecimal cantidadTotal) {
        // Para sumas, distribuimos proporcionalmente entre todos los lotes existentes
        if (lotes.isEmpty()) return;
        
        BigDecimal restante = cantidadTotal;
        
        for (int i = 0; i < lotes.size() && restante.compareTo(BigDecimal.ZERO) > 0; i++) {
            Lote lote = lotes.get(i);
            
            // Si es el último lote, le asignamos todo lo que queda
            BigDecimal aSumar = (i == lotes.size() - 1) ? restante : restante.divide(BigDecimal.valueOf(lotes.size() - i), 3, BigDecimal.ROUND_HALF_UP);
            
            lote.setCantidadEstanteria(lote.getCantidadEstanteria().add(aSumar));
            restante = restante.subtract(aSumar);
            
            loteRepository.save(lote);
        }
    }

    private MovimientoInventario crearMovimientoAjuste(
            AjustarInventarioManualRequestDto dto,
            String idLote,
            BigDecimal cantidad) {
        
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setSku(dto.sku());
        movimiento.setIdLote(idLote);
        // Usar el nuevo método para determinar el tipo de ajuste según el signo
        movimiento.setTipoMovimiento(TipoMovimiento.ajustePorCantidad(cantidad));
        // Convertir cantidad a valor absoluto (siempre positiva)
        movimiento.setCantidad(cantidad.abs());
        movimiento.setUnidadMedida(obtenerUnidadMedidaDelInventario(dto.sku()));
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setIdUsuario("SYSTEM"); // TODO: Obtener del contexto de seguridad
        movimiento.setMotivo(dto.tipoAjuste().name()); // "ROBO", "CADUCADO", etc.
        movimiento.setObservaciones(dto.motivoDetallado());
        
        return movimientoRepository.save(movimiento);
    }

    private UnidadMedida obtenerUnidadMedidaDelInventario(String sku) {
        return inventarioRepository.findBySku(sku)
            .map(Inventario::getUnidadMedida)
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Inventario no encontrado para SKU: " + sku)
                .asRuntimeException());
    }

    private AjustarInventarioManualResponseDto buildResponse(
            Inventario inventarioActualizado, 
            MovimientoInventario movimientoGuardado) {
        
        // Convertir inventario a DTO
        InventarioDto inventarioDto = new InventarioDto(
            inventarioActualizado.getIdInventario(),
            inventarioActualizado.getSku(),
            inventarioActualizado.getEan(),
            inventarioActualizado.getPlu(),
            inventarioActualizado.getCantidadAlmacen(),
            inventarioActualizado.getCantidadEstanteria(),
            inventarioActualizado.getUnidadMedida().name()
        );

        // Convertir movimiento a DTO
        com.isam.dto.movimiento.MovimientoInventarioDto movimientoDto = new com.isam.dto.movimiento.MovimientoInventarioDto(
            movimientoGuardado.getIdMovimiento(),
            movimientoGuardado.getSku(),
            movimientoGuardado.getIdLote(),
            movimientoGuardado.getTipoMovimiento().name(),
            movimientoGuardado.getCantidad(),
            movimientoGuardado.getUnidadMedida().name(),
            movimientoGuardado.getFechaHora().toString(),
            movimientoGuardado.getIdUsuario(),
            movimientoGuardado.getMotivo(),
            movimientoGuardado.getObservaciones()
        );
        
        return new AjustarInventarioManualResponseDto(inventarioDto, movimientoDto);
    }
}