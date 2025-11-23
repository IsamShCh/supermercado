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
     * Mueve stock del almacén a las estanterías (AC18).
     * Este método traslada una cantidad específica de un lote desde el almacén a la estantería.
     */
    @Transactional
    public com.isam.dto.stock.MoverStockEstanteriaResponseDto moverStockEstanteria(
            com.isam.dto.stock.MoverStockEstanteriaRequestDto peticionDto) {
        
        // Buscar el inventario para este SKU
        Inventario inventario = inventarioRepository.findBySku(peticionDto.sku())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Inventario no encontrado para SKU '" + peticionDto.sku() + "'")
                .asRuntimeException());

        // Buscar el lote específico
        Lote lote = loteRepository.findById(peticionDto.idLote())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Lote no encontrado con ID '" + peticionDto.idLote() + "'")
                .asRuntimeException());

        // Validar que el lote pertenece al SKU especificado
        if (!lote.getSku().equals(peticionDto.sku())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El lote '" + peticionDto.idLote() + "' no pertenece al SKU '" + peticionDto.sku() + "'")
                .asRuntimeException();
        }

        // Validar que el lote está disponible
        if (lote.getEstado() != EstadoLote.DISPONIBLE) {
            throw Status.FAILED_PRECONDITION
                .withDescription("El lote '" + peticionDto.idLote() + "' no está disponible. Estado actual: " + lote.getEstado())
                .asRuntimeException();
        }

        // Validar que la unidad de medida coincida
        if (!lote.getUnidadMedida().equals(peticionDto.unidadMedida())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("La unidad de medida no coincide con la del lote. Esperada: " + lote.getUnidadMedida() + ", Recibida: " + peticionDto.unidadMedida())
                .asRuntimeException();
        }

        // Validar que hay suficiente cantidad en el almacén del lote
        if (lote.getCantidadAlmacen().compareTo(peticionDto.cantidadTransladar()) < 0) {
            throw Status.FAILED_PRECONDITION
                .withDescription("Stock insuficiente en almacén para el lote '" + peticionDto.idLote() + "'. Disponible: " + lote.getCantidadAlmacen() + ", Solicitado: " + peticionDto.cantidadTransladar())
                .asRuntimeException();
        }

        // Mover el stock del lote
        lote.moverAEstanteria(peticionDto.cantidadTransladar());
        Lote loteActualizado = loteRepository.save(lote);

        // Actualizar el inventario general
        inventario.setCantidadAlmacen(inventario.getCantidadAlmacen().subtract(peticionDto.cantidadTransladar()));
        inventario.setCantidadEstanteria(inventario.getCantidadEstanteria().add(peticionDto.cantidadTransladar()));
        Inventario inventarioActualizado = inventarioRepository.save(inventario);

        // Crear movimiento de inventario
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setSku(peticionDto.sku());
        movimiento.setIdLote(peticionDto.idLote());
        movimiento.setTipoMovimiento(TipoMovimiento.TRASLADO_ESTANTERIA);
        movimiento.setCantidad(peticionDto.cantidadTransladar());
        movimiento.setUnidadMedida(peticionDto.unidadMedida());
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setIdUsuario("SYSTEM"); // TODO: Obtener del contexto de seguridad
        movimiento.setMotivo("Traslado de almacén a estantería");
        movimiento.setObservaciones("Lote: " + loteActualizado.getNumeroLote() + " - Cantidad trasladada: " + peticionDto.cantidadTransladar());
        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);

        // Convertir entidades a DTOs
        com.isam.dto.inventario.InventarioDto inventarioDto = new com.isam.dto.inventario.InventarioDto(
            inventarioActualizado.getIdInventario(),
            inventarioActualizado.getSku(),
            inventarioActualizado.getEan(),
            inventarioActualizado.getPlu(),
            inventarioActualizado.getCantidadAlmacen().doubleValue(),
            inventarioActualizado.getCantidadEstanteria().doubleValue(),
            inventarioActualizado.getUnidadMedida().name()
        );

        com.isam.dto.movimiento.MovimientoInventarioDto movimientoDto = new com.isam.dto.movimiento.MovimientoInventarioDto(
            movimientoGuardado.getIdMovimiento(),
            movimientoGuardado.getSku(),
            movimientoGuardado.getIdLote(),
            movimientoGuardado.getTipoMovimiento().name(),
            movimientoGuardado.getCantidad().doubleValue(),
            movimientoGuardado.getUnidadMedida().name(),
            movimientoGuardado.getFechaHora().toString(),
            movimientoGuardado.getIdUsuario(),
            movimientoGuardado.getMotivo(),
            movimientoGuardado.getObservaciones()
        );

        return new com.isam.dto.stock.MoverStockEstanteriaResponseDto(inventarioDto, movimientoDto);
    }
    /**
     * Aplica un ajuste manual al inventario (AC16).
     * Este método permite realizar ajustes manuales de inventario por varios motivos.
     */
    @Transactional
    public com.isam.dto.inventario.AjustarInventarioManualResponseDto ajustarInventarioManual(com.isam.dto.inventario.AjustarInventarioManualRequestDto dto) {
        
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
        if (dto.ubicacionAjuste() instanceof com.isam.dto.inventario.AjustarAlmacenDto ajustarAlmacen) {
            return procesarAjusteAlmacen(inventario, dto, ajustarAlmacen);
        } else if (dto.ubicacionAjuste() instanceof com.isam.dto.inventario.AjustarEstanteriaDto ajustarEstanteria) {
            return procesarAjusteEstanteria(inventario, dto, ajustarEstanteria);
        }
        
        throw Status.INVALID_ARGUMENT
            .withDescription("Tipo de ajuste no soportado")
            .asRuntimeException();
    }

    private com.isam.dto.inventario.AjustarInventarioManualResponseDto procesarAjusteAlmacen(
            Inventario inventario,
            com.isam.dto.inventario.AjustarInventarioManualRequestDto dto,
            com.isam.dto.inventario.AjustarAlmacenDto ajustarAlmacen) {
        
        BigDecimal cantidadAjuste = dto.cantidadAjuste();
        
        if (ajustarAlmacen.idLote() != null) {
            // Caso: Se especificó lote específico
            return procesarAjusteAlmacenConLote(inventario, dto, ajustarAlmacen, cantidadAjuste);
        } else {
            // Caso: No se especificó lote, aplicar al total de almacén
            return procesarAjusteAlmacenGeneral(inventario, dto, cantidadAjuste);
        }
    }

    private com.isam.dto.inventario.AjustarInventarioManualResponseDto procesarAjusteAlmacenConLote(
            Inventario inventario,
            com.isam.dto.inventario.AjustarInventarioManualRequestDto dto,
            com.isam.dto.inventario.AjustarAlmacenDto ajustarAlmacen,
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

    private com.isam.dto.inventario.AjustarInventarioManualResponseDto procesarAjusteAlmacenGeneral(
            Inventario inventario,
            com.isam.dto.inventario.AjustarInventarioManualRequestDto dto,
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

    private com.isam.dto.inventario.AjustarInventarioManualResponseDto procesarAjusteEstanteria(
            Inventario inventario,
            com.isam.dto.inventario.AjustarInventarioManualRequestDto dto,
            com.isam.dto.inventario.AjustarEstanteriaDto ajustarEstanteria) {
        
        BigDecimal cantidadAjuste = dto.cantidadAjuste();
        
        if (ajustarEstanteria.idLote() != null) {
            // Caso: Se especificó lote específico
            return procesarAjusteEstanteriaConLote(inventario, dto, ajustarEstanteria, cantidadAjuste);
        } else {
            // Caso: No se especificó lote, aplicar al total de estantería
            return procesarAjusteEstanteriaGeneral(inventario, dto, cantidadAjuste);
        }
    }

    private com.isam.dto.inventario.AjustarInventarioManualResponseDto procesarAjusteEstanteriaConLote(
            Inventario inventario,
            com.isam.dto.inventario.AjustarInventarioManualRequestDto dto,
            com.isam.dto.inventario.AjustarEstanteriaDto ajustarEstanteria,
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

    private com.isam.dto.inventario.AjustarInventarioManualResponseDto procesarAjusteEstanteriaGeneral(
            Inventario inventario,
            com.isam.dto.inventario.AjustarInventarioManualRequestDto dto,
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
            com.isam.dto.inventario.AjustarInventarioManualRequestDto dto, 
            String idLote, 
            BigDecimal cantidad) {
        
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setSku(dto.sku());
        movimiento.setIdLote(idLote);
        movimiento.setTipoMovimiento(TipoMovimiento.AJUSTE);  // Siempre AJUSTE
        movimiento.setCantidad(cantidad);
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

    private com.isam.dto.inventario.AjustarInventarioManualResponseDto buildResponse(
            Inventario inventarioActualizado, 
            MovimientoInventario movimientoGuardado) {
        
        // Convertir inventario a DTO
        InventarioDto inventarioDto = new InventarioDto(
            inventarioActualizado.getIdInventario(),
            inventarioActualizado.getSku(),
            inventarioActualizado.getEan(),
            inventarioActualizado.getPlu(),
            inventarioActualizado.getCantidadAlmacen().doubleValue(),
            inventarioActualizado.getCantidadEstanteria().doubleValue(),
            inventarioActualizado.getUnidadMedida().name()
        );
        
        // Convertir movimiento a DTO
        com.isam.dto.movimiento.MovimientoInventarioDto movimientoDto = new com.isam.dto.movimiento.MovimientoInventarioDto(
            movimientoGuardado.getIdMovimiento(),
            movimientoGuardado.getSku(),
            movimientoGuardado.getIdLote(),
            movimientoGuardado.getTipoMovimiento().name(),
            movimientoGuardado.getCantidad().doubleValue(),
            movimientoGuardado.getUnidadMedida().name(),
            movimientoGuardado.getFechaHora().toString(),
            movimientoGuardado.getIdUsuario(),
            movimientoGuardado.getMotivo(),
            movimientoGuardado.getObservaciones()
        );
        
        return new com.isam.dto.inventario.AjustarInventarioManualResponseDto(inventarioDto, movimientoDto);
    }


}
