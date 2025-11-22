package com.isam.service;

import com.isam.model.Inventario;
import com.isam.model.Lote;
import com.isam.model.MovimientoInventario;
import com.isam.model.Proveedor;
import com.isam.repository.InventarioRepository;
import com.isam.repository.LoteRepository;
import com.isam.repository.MovimientoInventarioRepository;
import com.isam.repository.ProveedorRepository;
import com.isam.dto.proveedor.AgregarProveedorRequestDto;
import com.isam.dto.proveedor.ProveedorDto;
import com.isam.dto.existencias.RegistrarNuevasExistenciasRequestDto;
import com.isam.dto.existencias.RegistrarNuevasExistenciasResponseDto;
import com.isam.dto.lote.LoteDto;
import com.isam.dto.lote.DetalleLoteDto;
import com.isam.dto.inventario.CrearInventarioRequestDto;
import com.isam.dto.inventario.DetallesInventarioCompletoDto;
import com.isam.dto.inventario.InventarioDto;
import com.isam.dto.inventario.ConsultarInventarioRequestDto;
import com.isam.dto.inventario.ConsultarInventarioResponseDto;
import com.isam.model.EstadoLote;
import com.isam.model.TipoMovimiento;
import com.isam.model.UnidadMedida;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.grpc.Status;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;
    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private MovimientoInventarioRepository movimientoRepository;
    @Autowired
    private ProveedorRepository proveedorRepository;

    
    /**
     * Crea un proveedor a partir de DTO, gestionando validaciones y la creación de la entidad.
     * Este método contiene la lógica empresarial para la creación de proveedores.
     */
    @Transactional
    public ProveedorDto agregarProveedor(AgregarProveedorRequestDto dto) {

        // Verificar duplicado por nombre ANTES de crear la entidad
        if (proveedorRepository.existsByNombreProveedor(dto.nombreProveedor())) {
            throw Status.ALREADY_EXISTS
                .withDescription("Ya existe un proveedor con el nombre: " + dto.nombreProveedor())
                .asRuntimeException();
        }

        // Verificar duplicado por email si se proporciona
        if (dto.email() != null && !dto.email().isBlank()) {
            if (proveedorRepository.existsByEmail(dto.email())) {
                throw Status.ALREADY_EXISTS
                    .withDescription("Ya existe un proveedor con el email: " + dto.email())
                    .asRuntimeException();
            }
        }

        // Generar ID único para el proveedor
        String idProveedor = "PROV-" + System.currentTimeMillis();

        // Crear el proveedor
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(idProveedor);
        proveedor.setNombreProveedor(dto.nombreProveedor());
        proveedor.setContacto(dto.contacto());
        proveedor.setDireccion(dto.direccion());
        proveedor.setTelefono(dto.telefono());
        proveedor.setEmail(dto.email());

        // Guardar proveedor
        Proveedor proveedorGuardado = proveedorRepository.save(proveedor);

        // Convertir a DTO para devolver
        return new ProveedorDto(
            proveedorGuardado.getIdProveedor(),
            proveedorGuardado.getNombreProveedor(),
            proveedorGuardado.getContacto(),
            proveedorGuardado.getDireccion(),
            proveedorGuardado.getTelefono(),
            proveedorGuardado.getEmail()
        );
    }

    /**
     * Registra nuevas existencias en el inventario (AC14).
     * Este método crea un nuevo lote y actualiza el inventario correspondiente.
     */
    @Transactional
    public RegistrarNuevasExistenciasResponseDto registrarNuevasExistencias(RegistrarNuevasExistenciasRequestDto dto) {
        
        // Verificar que el proveedor existe
        Proveedor proveedor = proveedorRepository.findById(dto.idProveedor())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Proveedor no encontrado con ID '" + dto.idProveedor() + "'")
                .asRuntimeException());

        // Buscar el inventario para este SKU
        Inventario inventario = inventarioRepository.findBySku(dto.sku())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Inventario no encontrado para SKU '" + dto.sku() + "'")
                .asRuntimeException());

        // Validar que la unidad de medida coincida
        if (!inventario.getUnidadMedida().equals(dto.unidadMedida())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("La unidad de medida no coincide con la del inventario existente")
                .asRuntimeException();
        }

        // Parsear fecha de caducidad si existe
        LocalDate fechaCaducidad = null;
        if (dto.fechaCaducidad() != null && !dto.fechaCaducidad().isBlank()) {
            try {
                fechaCaducidad = LocalDate.parse(dto.fechaCaducidad());
            } catch (Exception e) {
                throw Status.INVALID_ARGUMENT
                    .withDescription("Formato de fecha de caducidad inválido. Use YYYY-MM-DD")
                    .asRuntimeException();
            }
        }

        // Crear el nuevo lote
        Lote lote = new Lote();
        lote.setSku(dto.sku());
        lote.setIdInventario(inventario.getIdInventario());
        lote.setNumeroLote(dto.numeroLote());
        lote.setCantidadEntrada(dto.cantidad());
        lote.setCantidadAlmacen(dto.cantidad());
        lote.setCantidadEstanteria(BigDecimal.ZERO);
        lote.setFechaCaducidad(fechaCaducidad);
        lote.setIdProveedor(dto.idProveedor());
        lote.setFechaIngreso(LocalDate.now());
        lote.setUnidadMedida(dto.unidadMedida());
        lote.setEstado(EstadoLote.DISPONIBLE);

        // Guardar el lote
        Lote loteGuardado = loteRepository.save(lote);

        // Actualizar el inventario: sumar la cantidad al almacén
        inventario.setCantidadAlmacen(inventario.getCantidadAlmacen().add(dto.cantidad()));
        Inventario inventarioActualizado = inventarioRepository.save(inventario);

        // Crear movimiento de inventario
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setSku(dto.sku());
        movimiento.setIdLote(loteGuardado.getIdLote());
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA);
        movimiento.setCantidad(dto.cantidad());
        movimiento.setUnidadMedida(dto.unidadMedida());
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setIdUsuario("SYSTEM"); // TODO: Obtener del contexto de seguridad
        movimiento.setMotivo("Registro de nuevas existencias - Lote: " + dto.numeroLote());
        movimiento.setObservaciones("Proveedor: " + proveedor.getNombreProveedor());
        movimientoRepository.save(movimiento);

        // Convertir entidades a DTOs
        LoteDto loteDto = new LoteDto(
            loteGuardado.getIdLote(),
            loteGuardado.getSku(),
            loteGuardado.getIdInventario(),
            loteGuardado.getNumeroLote(),
            loteGuardado.getCantidadEntrada().doubleValue(),
            loteGuardado.getCantidadAlmacen().doubleValue(),
            loteGuardado.getCantidadEstanteria().doubleValue(),
            loteGuardado.getFechaCaducidad() != null ? loteGuardado.getFechaCaducidad().toString() : null,
            loteGuardado.getIdProveedor(),
            loteGuardado.getFechaIngreso().toString(),
            loteGuardado.getUnidadMedida().name(),
            loteGuardado.getEstado().name()
        );

        InventarioDto inventarioDto = new InventarioDto(
            inventarioActualizado.getIdInventario(),
            inventarioActualizado.getSku(),
            inventarioActualizado.getEan(),
            inventarioActualizado.getPlu(),
            inventarioActualizado.getCantidadAlmacen().doubleValue(),
            inventarioActualizado.getCantidadEstanteria().doubleValue(),
            inventarioActualizado.getUnidadMedida().name()
        );

        return new RegistrarNuevasExistenciasResponseDto(loteDto, inventarioDto);
    }

    private boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }



    @Transactional
    public InventarioDto crearInventario(CrearInventarioRequestDto dto) {
        // Validar que solo tenga EAN o PLU, no ambos --> Estos ya deberia comprobarlo el validator
        if (isNotNullOrEmpty(dto.ean()) && isNotNullOrEmpty(dto.plu())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Un producto solo puede tener o EAN o PLU, pero no ambos")
                .asRuntimeException();
        }
        
        // Comprobamos que no exista un inventario ya existente. Si existe y es igual a los datos del dto, lo dejamos pasar, si no, tiramos error
        Optional<Inventario> inventarioExistenteOpt = inventarioRepository.findBySku(dto.sku());
        
        if (inventarioExistenteOpt.isPresent()) {
            Inventario inventarioExistente = inventarioExistenteOpt.get();
            if (!inventarioExistente.getUnidadMedida().equals(dto.unidadMedida())) {
                throw Status.ALREADY_EXISTS
                    .withDescription("Ya existe un inventario para SKU '" + dto.sku() + "' con unidad de medida diferente")
                    .asRuntimeException();
            }
            // Si existe y coincide, devolver el DTO existente
            return new InventarioDto(
                inventarioExistente.getIdInventario(),
                inventarioExistente.getSku(),
                inventarioExistente.getEan(),
                inventarioExistente.getPlu(),
                inventarioExistente.getCantidadAlmacen().doubleValue(),
                inventarioExistente.getCantidadEstanteria().doubleValue(),
                inventarioExistente.getUnidadMedida().name()
            );
        } else {
            // Crear nuevo inventario
            Inventario nuevoInventario = new Inventario(dto.sku(), dto.ean(), dto.plu(), dto.unidadMedida());
            Inventario inventarioGuardado = inventarioRepository.save(nuevoInventario);
            return new InventarioDto(
                inventarioGuardado.getIdInventario(),
                inventarioGuardado.getSku(),
                inventarioGuardado.getEan(),
                inventarioGuardado.getPlu(),
                inventarioGuardado.getCantidadAlmacen().doubleValue(),
                inventarioGuardado.getCantidadEstanteria().doubleValue(),
                inventarioGuardado.getUnidadMedida().name()
            );
        }
    }
        /**
         * Consulta el inventario de un producto (AC20).
         * Este método obtiene la información completa del inventario de un producto,
         * incluyendo detalles por lote.
         */
        @Transactional(readOnly = true)
        public ConsultarInventarioResponseDto consultarInventario(ConsultarInventarioRequestDto dto) {
            
            // Buscar el inventario para este SKU
            Inventario inventario = inventarioRepository.findBySku(dto.sku())
                .orElseThrow(() -> Status.NOT_FOUND
                    .withDescription("Inventario no encontrado para SKU '" + dto.sku() + "'")
                    .asRuntimeException());
    
            // Buscar todos los lotes asociados a este inventario
            List<Lote> lotes = loteRepository.findBySku(dto.sku());
            
            // Convertir lotes a DetalleLoteDto
            List<DetalleLoteDto> detallesLotes = lotes.stream()
                .map(lote -> new DetalleLoteDto(
                    lote.getIdLote(),
                    lote.getNumeroLote(),
                    lote.getCantidadAlmacen() != null ? lote.getCantidadAlmacen().doubleValue() : 0.0,  // Cantidad en almacén
                    lote.getCantidadEstanteria() != null ? lote.getCantidadEstanteria().doubleValue() : 0.0,  // Cantidad en estantería
                    lote.getFechaCaducidad() != null ? lote.getFechaCaducidad().toString() : null,
                    lote.getFechaIngreso() != null ? lote.getFechaIngreso().toString() : null
                ))
                .toList();
    
            // TODO: Obtener nombre del producto del servicio de catálogo
            // Por ahora, usamos el SKU como nombre
            String nombreProducto = dto.sku();
    
            // Construir el DTO de detalles completos con null safety
            DetallesInventarioCompletoDto detallesCompletos = new DetallesInventarioCompletoDto(
                inventario.getSku(),
                nombreProducto,
                inventario.getCantidadAlmacen() != null ? inventario.getCantidadAlmacen().doubleValue() : 0.0,
                inventario.getCantidadEstanteria() != null ? inventario.getCantidadEstanteria().doubleValue() : 0.0,
                inventario.getUnidadMedida(),
                detallesLotes
            );
            
            // Construir y devolver la respuesta con el DTO correcto
            return new ConsultarInventarioResponseDto(detallesCompletos);
        }

    /**
     * Mueve stock de almacén a estantería (AC18).
     * Este método traslada cantidad de un lote específico del almacén a estantería.
     */
    @Transactional
    public void moverStockEstanteria(String sku, String idLote, BigDecimal cantidadMover, UnidadMedida unidadMedida, String idUsuario) {
        
        // Validar que la cantidad a mover sea positiva
        if (cantidadMover.compareTo(BigDecimal.ZERO) <= 0) {
            throw Status.INVALID_ARGUMENT
                .withDescription("La cantidad a mover debe ser mayor a cero")
                .asRuntimeException();
        }

        // Buscar el lote
        Lote lote = loteRepository.findById(idLote)
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Lote no encontrado con ID '" + idLote + "'")
                .asRuntimeException());

        // Validar estado del lote
        if (lote.getEstado() != EstadoLote.DISPONIBLE) {
            throw Status.FAILED_PRECONDITION
                .withDescription("No se puede mover stock de un lote que no está DISPONIBLE (Estado actual: " + lote.getEstado() + ")")
                .asRuntimeException();
        }

        // Validar que el SKU coincida
        if (!lote.getSku().equals(sku)) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El SKU del lote no coincide con el SKU proporcionado")
                .asRuntimeException();
        }

        // Validar unidad de medida
        if (!lote.getUnidadMedida().equals(unidadMedida)) {
            throw Status.INVALID_ARGUMENT
                .withDescription("La unidad de medida no coincide con la del lote")
                .asRuntimeException();
        }

        // Validar que haya suficiente stock en almacén
        if (lote.getCantidadAlmacen().compareTo(cantidadMover) < 0) {
            throw Status.FAILED_PRECONDITION
                .withDescription("Stock insuficiente en almacén. Disponible: " + lote.getCantidadAlmacen() + ", solicitado: " + cantidadMover)
                .asRuntimeException();
        }

        // Mover stock
        lote.moverAEstanteria(cantidadMover);
        loteRepository.save(lote);

        // Actualizar inventario
        Inventario inventario = inventarioRepository.findBySku(sku)
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Inventario no encontrado para SKU '" + sku + "'")
                .asRuntimeException());
        
        inventario.setCantidadAlmacen(inventario.getCantidadAlmacen().subtract(cantidadMover));
        inventario.setCantidadEstanteria(inventario.getCantidadEstanteria().add(cantidadMover));
        inventarioRepository.save(inventario);

        // Crear movimiento de inventario
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setSku(sku);
        movimiento.setIdLote(idLote);
        movimiento.setTipoMovimiento(TipoMovimiento.TRASLADO_ESTANTERIA);
        movimiento.setCantidad(cantidadMover);
        movimiento.setUnidadMedida(unidadMedida);
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setIdUsuario(idUsuario);
        movimiento.setMotivo("Traslado a estantería - Lote: " + lote.getNumeroLote());
        movimientoRepository.save(movimiento);
    }

    /**
     * Aplica una venta descontando stock de estantería según política FIFO.
     */
    @Transactional
    public void aplicarVenta(String sku, BigDecimal cantidadVender, UnidadMedida unidadMedida, String idUsuario) {
        
        // Validar que la cantidad a vender sea positiva
        if (cantidadVender.compareTo(BigDecimal.ZERO) <= 0) {
            throw Status.INVALID_ARGUMENT
                .withDescription("La cantidad a vender debe ser mayor a cero")
                .asRuntimeException();
        }

        // Buscar todos los lotes disponibles para este SKU ordenados por fecha de ingreso (FIFO)
        List<Lote> lotes = loteRepository.findBySkuAndEstadoOrderByFechaIngresoAsc(sku, EstadoLote.DISPONIBLE);
        
        if (lotes.isEmpty()) {
            throw Status.NOT_FOUND
                .withDescription("No hay lotes disponibles para el SKU '" + sku + "'")
                .asRuntimeException();
        }

        // Validar stock total en estantería
        BigDecimal stockTotalEstanteria = lotes.stream()
            .map(Lote::getCantidadEstanteria)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        if (stockTotalEstanteria.compareTo(cantidadVender) < 0) {
            throw Status.FAILED_PRECONDITION
                .withDescription("Stock insuficiente en estantería. Disponible: " + stockTotalEstanteria + ", solicitado: " + cantidadVender)
                .asRuntimeException();
        }

        BigDecimal cantidadRestante = cantidadVender;
        
        // Aplicar venta a lotes en orden FIFO
        for (Lote lote : lotes) {
            if (cantidadRestante.compareTo(BigDecimal.ZERO) <= 0) break;
            
            BigDecimal disponibleEstanteria = lote.getCantidadEstanteria();
            if (disponibleEstanteria.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal cantidadDescontar = disponibleEstanteria.min(cantidadRestante);
                
                // Descontar del lote
                lote.descontarVenta(cantidadDescontar);
                loteRepository.save(lote);
                
                cantidadRestante = cantidadRestante.subtract(cantidadDescontar);
                
                // Crear movimiento de venta
                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setSku(sku);
                movimiento.setIdLote(lote.getIdLote());
                movimiento.setTipoMovimiento(TipoMovimiento.VENTA);
                movimiento.setCantidad(cantidadDescontar);
                movimiento.setUnidadMedida(unidadMedida);
                movimiento.setFechaHora(LocalDateTime.now());
                movimiento.setIdUsuario(idUsuario);
                movimiento.setMotivo("Venta - Lote: " + lote.getNumeroLote());
                movimientoRepository.save(movimiento);
            }
        }

        // Actualizar inventario total
        Inventario inventario = inventarioRepository.findBySku(sku)
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Inventario no encontrado para SKU '" + sku + "'")
                .asRuntimeException());
        
        inventario.setCantidadEstanteria(inventario.getCantidadEstanteria().subtract(cantidadVender));
        // El inventario no tiene campo cantidad total, solo cantidadAlmacen y cantidadEstanteria
        // La cantidad total se calcula como suma de ambas
        inventarioRepository.save(inventario);
    }
}