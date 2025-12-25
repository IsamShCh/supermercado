package com.isam.service;

import com.isam.model.Inventario;
import com.isam.model.Lote;
import com.isam.model.MovimientoInventario;
import com.isam.model.Proveedor;
import com.isam.repository.InventarioRepository;
import com.isam.repository.LoteRepository;
import com.isam.repository.MovimientoInventarioRepository;
import com.isam.repository.ProveedorRepository;
import com.isam.repository.ProductoCacheRepository;
import com.isam.model.ProductoCache;
import com.isam.dto.proveedor.AgregarProveedorRequestDto;
import com.isam.dto.proveedor.ProveedorDto;
import com.isam.dto.existencias.RegistrarNuevasExistenciasRequestDto;
import com.isam.dto.existencias.RegistrarNuevasExistenciasResponseDto;
import com.isam.dto.lote.LoteDto;
import com.isam.dto.producto.ConsultarProductoDto;
import com.isam.dto.lote.DetalleLoteDto;
import com.isam.dto.inventario.CrearInventarioRequestDto;
import com.isam.dto.inventario.DetallesInventarioCompletoDto;
import com.isam.dto.inventario.InventarioDto;
import com.isam.dto.inventario.ConsultarInventarioRequestDto;
import com.isam.dto.inventario.ConsultarInventarioResponseDto;
import com.isam.model.EstadoLote;
import com.isam.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.access.prepost.PreAuthorize;

import io.grpc.Status;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProveedorRepository proveedorRepository;
    private final AjusteInventarioService ajusteInventarioService;
    private final com.isam.grpc.client.CatalogoGrpcClient catalogoGrpcClient;
    private final TransactionTemplate transactionTemplate;
    private final InventarioEventService inventarioEventService;
    private final ProductoCacheRepository productoCacheRepository;

    /**
     * Crea un proveedor a partir de DTO, gestionando validaciones y la creación de
     * la entidad.
     * Este método contiene la lógica empresarial para la creación de proveedores.
     */
    @Transactional
    @PreAuthorize("hasAuthority('CREAR_PROVEEDORES')")
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

        // Crear el proveedor
        Proveedor proveedor = new Proveedor();
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
                proveedorGuardado.getEmail());
    }

    /**
     * Registra nuevas existencias en el inventario (AC14).
     * Este método crea un nuevo lote y actualiza el inventario correspondiente.
     */
    @Transactional
    @PreAuthorize("hasAuthority('CREAR_LOTES')")
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

        // Validar que el inventario no sea provisional (Dummy de venta)
        if (Boolean.TRUE.equals(inventario.getEsProvisional())) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("El inventario para SKU '" + dto.sku()
                            + "' es provisional (creado automáticamente por una venta). " +
                            "Debe oficializarlo utilizando la función 'Crear Inventario' antes de registrar existencias.")
                    .asRuntimeException();
        }

        // Validar que la unidad de medida coincida
        if (!inventario.getUnidadMedida().equals(dto.unidadMedida())) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("La unidad de medida no coincide con la del inventario existente ("
                            + inventario.getUnidadMedida() + " vs " + dto.unidadMedida() + ")")
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
        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);

        // Publicar evento de movimiento para BI
        inventarioEventService.publicarMovimiento(movimientoGuardado);

        // Convertir entidades a DTOs
        LoteDto loteDto = new LoteDto(
                loteGuardado.getIdLote(),
                loteGuardado.getSku(),
                loteGuardado.getIdInventario(),
                loteGuardado.getNumeroLote(),
                loteGuardado.getCantidadEntrada(),
                loteGuardado.getCantidadAlmacen(),
                loteGuardado.getCantidadEstanteria(),
                loteGuardado.getFechaCaducidad() != null ? loteGuardado.getFechaCaducidad().toString() : null,
                loteGuardado.getIdProveedor(),
                loteGuardado.getFechaIngreso().toString(),
                loteGuardado.getUnidadMedida().name(),
                loteGuardado.getEstado().name());

        InventarioDto inventarioDto = new InventarioDto(
                inventarioActualizado.getIdInventario(),
                inventarioActualizado.getSku(),
                inventarioActualizado.getEan(),
                inventarioActualizado.getPlu(),
                inventarioActualizado.getCantidadAlmacen(),
                inventarioActualizado.getCantidadEstanteria(),
                inventarioActualizado.getUnidadMedida().name());

        return new RegistrarNuevasExistenciasResponseDto(loteDto, inventarioDto);
    }

    private boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    @PreAuthorize("hasAuthority('CREAR_INVENTARIO')")
    public InventarioDto crearInventario(CrearInventarioRequestDto dto) {
        // Validar que solo tenga EAN o PLU, no ambos --> Estos ya deberia comprobarlo
        // el validator
        if (isNotNullOrEmpty(dto.ean()) && isNotNullOrEmpty(dto.plu())) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("Un producto solo puede tener o EAN o PLU, pero no ambos")
                    .asRuntimeException();
        }

        // Si no está en caché, hacer fallback a gRPC (para compatibilidad durante
        // migración)
        Optional<ProductoCache> productoEnCache = productoCacheRepository.findBySku(dto.sku());

        if (productoEnCache.isPresent()) {
            // Usar caché local - desacoplado del servicio de catálogo
            ProductoCache cache = productoEnCache.get();
            log.debug("Producto {} encontrado en caché local", dto.sku());

            // Validar unidad de medida contra la caché
            if (cache.getUnidadMedida() != null && !cache.getUnidadMedida().equals(dto.unidadMedida())) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("La unidad de medida del producto no coincide con la del catalogo")
                        .asRuntimeException();
            }

            // Validar EAN si está presente en ambos
            if (isNotNullOrEmpty(cache.getEan()) && isNotNullOrEmpty(dto.ean())
                    && !cache.getEan().equals(dto.ean())) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("El ean del producto no coincide con el del catalogo")
                        .asRuntimeException();
            }

            // Validar PLU si está presente en ambos
            if (isNotNullOrEmpty(cache.getPlu()) && isNotNullOrEmpty(dto.plu())
                    && !cache.getPlu().equals(dto.plu())) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("El plu del producto no coincide con el del catalogo")
                        .asRuntimeException();
            }
        } else {
            // Fallback a gRPC - solo si el producto no está en caché
            log.debug("Producto {} no encontrado en caché, consultando via gRPC", dto.sku());

            ConsultarProductoDto productoEnCatalogo = catalogoGrpcClient.consultarProducto(dto.sku());

            if (isNotNullOrEmpty(productoEnCatalogo.ean()) && isNotNullOrEmpty(dto.ean())
                    && !productoEnCatalogo.ean().equals(dto.ean())) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("El ean del producto no coincide con el del catalogo")
                        .asRuntimeException();
            }

            if (isNotNullOrEmpty(productoEnCatalogo.plu()) && isNotNullOrEmpty(dto.plu())
                    && !productoEnCatalogo.plu().equals(dto.plu())) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("El plu del producto no coincide con el del catalogo")
                        .asRuntimeException();
            }

            if (!productoEnCatalogo.unidadMedida().equals(dto.unidadMedida())) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("La unidad de medida del producto no coincide con la del catalogo")
                        .asRuntimeException();
            }
        }

        // Creamos un metodo auxiliar que si es transactional para evitar secuestrar la
        // bbdd mientras esperamos la respuesta grpc.
        return transactionTemplate.<InventarioDto>execute(status -> {
            return crearInventarioAux(dto);
        });
    }

    private InventarioDto crearInventarioAux(CrearInventarioRequestDto dto) {

        // Comprobamos si existe un inventario
        Optional<Inventario> inventarioExistenteOpt = inventarioRepository.findBySku(dto.sku());

        if (inventarioExistenteOpt.isPresent()) {
            Inventario inventario = inventarioExistenteOpt.get();

            // Si es PROVISIONAL, lo sobrescribimos con los datos oficiales
            if (Boolean.TRUE.equals(inventario.getEsProvisional())) {
                log.info("Oficializando inventario provisional para SKU: {}", dto.sku());
                inventario.setUnidadMedida(dto.unidadMedida());
                inventario.setEan(dto.ean());
                inventario.setPlu(dto.plu());
                inventario.setEsProvisional(false); // Quitamos la marca de provisional

                Inventario inventarioGuardado = inventarioRepository.save(inventario);

                return new InventarioDto(
                        inventarioGuardado.getIdInventario(),
                        inventarioGuardado.getSku(),
                        inventarioGuardado.getEan(),
                        inventarioGuardado.getPlu(),
                        inventarioGuardado.getCantidadAlmacen(),
                        inventarioGuardado.getCantidadEstanteria(),
                        inventarioGuardado.getUnidadMedida().name());
            }

            // Si es OFICIAL (no provisional) y la unidad no coincide, entonce emitimos un
            // error
            if (!inventario.getUnidadMedida().equals(dto.unidadMedida())) {
                throw Status.ALREADY_EXISTS
                        .withDescription("Ya existe un inventario oficial para SKU '" + dto.sku()
                                + "' con unidad de medida diferente ("
                                + inventario.getUnidadMedida() + " vs " + dto.unidadMedida() + ")")
                        .asRuntimeException();
            }

            // Si existe, es oficial y coincide entonces Devolver el existente
            return new InventarioDto(
                    inventario.getIdInventario(),
                    inventario.getSku(),
                    inventario.getEan(),
                    inventario.getPlu(),
                    inventario.getCantidadAlmacen(),
                    inventario.getCantidadEstanteria(),
                    inventario.getUnidadMedida().name());
        } else {
            // Crear nuevo inventario
            Inventario nuevoInventario = new Inventario(dto.sku(), dto.ean(), dto.plu(), dto.unidadMedida());
            // Por defecto esProvisional es false
            Inventario inventarioGuardado = inventarioRepository.save(nuevoInventario);
            return new InventarioDto(
                    inventarioGuardado.getIdInventario(),
                    inventarioGuardado.getSku(),
                    inventarioGuardado.getEan(),
                    inventarioGuardado.getPlu(),
                    inventarioGuardado.getCantidadAlmacen(),
                    inventarioGuardado.getCantidadEstanteria(),
                    inventarioGuardado.getUnidadMedida().name());
        }
    }

    /**
     * Consulta el inventario de un producto (AC20).
     * Este método obtiene la información completa del inventario de un producto,
     * incluyendo detalles por lote.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('LEER_INVENTARIO')")
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
                        lote.getCantidadAlmacen() != null ? lote.getCantidadAlmacen() : BigDecimal.ZERO, // Cantidad en
                                                                                                         // almacén
                        lote.getCantidadEstanteria() != null ? lote.getCantidadEstanteria() : BigDecimal.ZERO, // Cantidad
                                                                                                               // en
                                                                                                               // estantería
                        lote.getFechaCaducidad() != null ? lote.getFechaCaducidad().toString() : null,
                        lote.getFechaIngreso() != null ? lote.getFechaIngreso().toString() : null))
                .toList();

        // DESACOPLAMIENTO: Intentar obtener el nombre del producto de la caché local
        // primero
        String nombreProducto = productoCacheRepository.findBySku(dto.sku())
                .map(ProductoCache::getNombre)
                .orElse(dto.sku()); // Fallback al SKU si no está en caché

        // Construir el DTO de detalles completos con null safety
        DetallesInventarioCompletoDto detallesCompletos = new DetallesInventarioCompletoDto(
                inventario.getSku(),
                nombreProducto,
                inventario.getCantidadAlmacen() != null ? inventario.getCantidadAlmacen() : BigDecimal.ZERO,
                inventario.getCantidadEstanteria() != null ? inventario.getCantidadEstanteria() : BigDecimal.ZERO,
                inventario.getUnidadMedida(),
                detallesLotes);

        // Construir y devolver la respuesta con el DTO correcto
        return new ConsultarInventarioResponseDto(detallesCompletos);
    }

    /**
     * Mueve stock del almacén a las estanterías (AC18).
     * Este método traslada una cantidad específica de un lote desde el almacén a la
     * estantería.
     */
    @Transactional
    @PreAuthorize("hasAuthority('ACTUALIZAR_INVENTARIO')")
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
                    .withDescription(
                            "El lote '" + peticionDto.idLote() + "' no pertenece al SKU '" + peticionDto.sku() + "'")
                    .asRuntimeException();
        }

        // Validar que el lote está disponible
        if (lote.getEstado() != EstadoLote.DISPONIBLE) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("El lote '" + peticionDto.idLote() + "' no está disponible. Estado actual: "
                            + lote.getEstado())
                    .asRuntimeException();
        }

        // Validar que la unidad de medida coincida
        if (!lote.getUnidadMedida().equals(peticionDto.unidadMedida())) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("La unidad de medida no coincide con la del lote. Esperada: "
                            + lote.getUnidadMedida() + ", Recibida: " + peticionDto.unidadMedida())
                    .asRuntimeException();
        }

        // Validar que hay suficiente cantidad en el almacén del lote
        if (lote.getCantidadAlmacen().compareTo(peticionDto.cantidadTransladar()) < 0) {
            throw Status.FAILED_PRECONDITION
                    .withDescription(
                            "Stock insuficiente en almacén para el lote '" + peticionDto.idLote() + "'. Disponible: "
                                    + lote.getCantidadAlmacen() + ", Solicitado: " + peticionDto.cantidadTransladar())
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
        movimiento.setObservaciones("Lote: " + loteActualizado.getNumeroLote() + " - Cantidad trasladada: "
                + peticionDto.cantidadTransladar());
        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);

        // Publicar evento de movimiento
        inventarioEventService.publicarMovimiento(movimientoGuardado);

        // Convertir entidades a DTOs
        com.isam.dto.inventario.InventarioDto inventarioDto = new com.isam.dto.inventario.InventarioDto(
                inventarioActualizado.getIdInventario(),
                inventarioActualizado.getSku(),
                inventarioActualizado.getEan(),
                inventarioActualizado.getPlu(),
                inventarioActualizado.getCantidadAlmacen(),
                inventarioActualizado.getCantidadEstanteria(),
                inventarioActualizado.getUnidadMedida().name());

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
                movimientoGuardado.getObservaciones());

        return new com.isam.dto.stock.MoverStockEstanteriaResponseDto(inventarioDto, movimientoDto);
    }

    /**
     * Aplica un ajuste manual al inventario (AC16).
     * Delega la lógica al servicio especializado AjusteInventarioService.
     */
    @Transactional
    @PreAuthorize("hasAuthority('ACTUALIZAR_INVENTARIO')")
    public com.isam.dto.inventario.AjustarInventarioManualResponseDto ajustarInventarioManual(
            com.isam.dto.inventario.AjustarInventarioManualRequestDto dto) {
        return ajusteInventarioService.ajustarInventarioManual(dto);
    }

    /**
     * Contabiliza el stock manualmente (AC19).
     * Permite contabilizar estantería, almacén, o ambos.
     * Para almacén, admite dos modalidades: rápida (total) o precisa (por lotes).
     */
    @Transactional
    @PreAuthorize("hasAuthority('ACTUALIZAR_INVENTARIO')")
    public com.isam.dto.inventario.ContabilizarStockManualResponseDto contabilizarStockManual(
            com.isam.dto.inventario.ContabilizarStockManualRequestDto dto) {

        // Buscar el inventario para este SKU
        Inventario inventario = inventarioRepository.findBySku(dto.sku())
                .orElseThrow(() -> Status.NOT_FOUND
                        .withDescription("Inventario no encontrado para SKU '" + dto.sku() + "'")
                        .asRuntimeException());

        // Variables para el reporte
        BigDecimal stockLogicoEstanteria = inventario.getCantidadEstanteria();
        BigDecimal stockFisicoEstanteria = dto.stockFisicoEstanteria() != null ? dto.stockFisicoEstanteria()
                : stockLogicoEstanteria;
        BigDecimal stockLogicoAlmacen = inventario.getCantidadAlmacen();
        BigDecimal stockFisicoAlmacen = stockLogicoAlmacen;

        List<com.isam.dto.inventario.AjusteLoteDto> ajustesRealizados = new java.util.ArrayList<>();
        List<com.isam.dto.movimiento.MovimientoInventarioDto> movimientos = new java.util.ArrayList<>();

        // Procesar contabilización de estantería si está presente
        if (dto.stockFisicoEstanteria() != null) {
            procesarContabilizacionEstanteria(
                    inventario,
                    dto.stockFisicoEstanteria(),
                    stockLogicoEstanteria,
                    ajustesRealizados,
                    movimientos);
        }

        // Procesar contabilización de almacén si se ha proporcionado
        if (dto.contabilizacionLotes() != null) {
            // Modalidad precisa: por lotes
            stockFisicoAlmacen = procesarContabilizacionAlmacenPorLotes(
                    inventario,
                    dto.contabilizacionLotes(),
                    stockLogicoAlmacen,
                    ajustesRealizados,
                    movimientos);
        }

        // Guardar inventario actualizado
        Inventario inventarioActualizado = inventarioRepository.save(inventario);

        // Construir DTOs de respuesta
        com.isam.dto.inventario.InventarioDto inventarioDto = new com.isam.dto.inventario.InventarioDto(
                inventarioActualizado.getIdInventario(),
                inventarioActualizado.getSku(),
                inventarioActualizado.getEan(),
                inventarioActualizado.getPlu(),
                inventarioActualizado.getCantidadAlmacen(),
                inventarioActualizado.getCantidadEstanteria(),
                inventarioActualizado.getUnidadMedida().name());

        // Construir reporte de discrepancias
        com.isam.dto.inventario.ReporteDiscrepanciasDto reporte = new com.isam.dto.inventario.ReporteDiscrepanciasDto(
                dto.sku(),
                stockLogicoEstanteria,
                stockFisicoEstanteria,
                stockFisicoEstanteria.subtract(stockLogicoEstanteria),
                stockLogicoAlmacen,
                stockFisicoAlmacen,
                stockFisicoAlmacen.subtract(stockLogicoAlmacen),
                ajustesRealizados);

        return new com.isam.dto.inventario.ContabilizarStockManualResponseDto(
                inventarioDto,
                movimientos,
                reporte);
    }

    private void procesarContabilizacionEstanteria(
            Inventario inventario,
            BigDecimal stockFisico,
            BigDecimal stockLogico,
            List<com.isam.dto.inventario.AjusteLoteDto> ajustesRealizados,
            List<com.isam.dto.movimiento.MovimientoInventarioDto> movimientos) {

        BigDecimal discrepancia = stockFisico.subtract(stockLogico);

        if (discrepancia.compareTo(BigDecimal.ZERO) == 0) {
            return; // No hay discrepancia, no hacer nada
        }

        // Obtener lotes con stock en estantería
        List<Lote> lotes = loteRepository.findBySkuAndCantidadEstanteriaGreaterThan(
                inventario.getSku(),
                BigDecimal.ZERO); // findBySkuAndCantidadEstanteriaGreaterThan() ejecuta una query personalizada
                                  // que tiene un "ORDER BY l.fechaIngreso ASC"

        // Distribuir el ajuste entre los lotes usando FIFO
        BigDecimal restante = discrepancia;

        for (Lote lote : lotes) {
            if (restante.compareTo(BigDecimal.ZERO) == 0)
                break;

            BigDecimal stockAnterior = lote.getCantidadEstanteria();
            BigDecimal ajuste;

            if (discrepancia.compareTo(BigDecimal.ZERO) > 0) {
                // Incremento: distribuir proporcionalmente
                ajuste = restante.divide(BigDecimal.valueOf(lotes.size()), 3, java.math.RoundingMode.HALF_UP);
            } else {
                // Decremento: aplicar FIFO
                ajuste = stockAnterior.min(restante.abs()).negate();
            }

            lote.setCantidadEstanteria(stockAnterior.add(ajuste));
            loteRepository.save(lote);

            // Registrar ajuste
            ajustesRealizados.add(new com.isam.dto.inventario.AjusteLoteDto(
                    lote.getIdLote(),
                    lote.getNumeroLote(),
                    "ESTANTERIA",
                    ajuste,
                    stockAnterior,
                    lote.getCantidadEstanteria()));

            restante = restante.subtract(ajuste);
        }

        // Actualizar inventario general
        inventario.setCantidadEstanteria(stockFisico);

        // Crear movimiento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setSku(inventario.getSku());
        movimiento.setIdLote(null);
        // Usar el nuevo método para determinar el tipo de ajuste según el signo
        movimiento.setTipoMovimiento(TipoMovimiento.ajustePorCantidad(discrepancia)); // Devuelve o AJUSTE_POSITIVO o
                                                                                      // AJUSTE_NEGATIVO
        // Convertir discrepancia a valor absoluto (siempre positiva)
        movimiento.setCantidad(discrepancia.abs());
        movimiento.setUnidadMedida(inventario.getUnidadMedida());
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setIdUsuario("SYSTEM");
        movimiento.setMotivo("Contabilización manual - Estantería");
        movimiento.setObservaciones("Discrepancia: " + discrepancia.doubleValue());

        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);

        // Publicar evento
        inventarioEventService.publicarMovimiento(movimientoGuardado);

        movimientos.add(new com.isam.dto.movimiento.MovimientoInventarioDto(
                movimientoGuardado.getIdMovimiento(),
                movimientoGuardado.getSku(),
                movimientoGuardado.getIdLote(),
                movimientoGuardado.getTipoMovimiento().name(),
                movimientoGuardado.getCantidad(),
                movimientoGuardado.getUnidadMedida().name(),
                movimientoGuardado.getFechaHora().toString(),
                movimientoGuardado.getIdUsuario(),
                movimientoGuardado.getMotivo(),
                movimientoGuardado.getObservaciones()));
    }

    private BigDecimal procesarContabilizacionAlmacenPorLotes(
            Inventario inventario,
            com.isam.dto.inventario.ContabilizacionPorLotesDto contabilizacion,
            BigDecimal stockLogico,
            List<com.isam.dto.inventario.AjusteLoteDto> ajustesRealizados,
            List<com.isam.dto.movimiento.MovimientoInventarioDto> movimientos) {

        BigDecimal stockFisicoTotal = BigDecimal.ZERO;
        BigDecimal ajusteTotalInventario = BigDecimal.ZERO;

        // Procesar cada lote individualmente
        for (com.isam.dto.inventario.StockFisicoLoteDto stockLote : contabilizacion.lotes()) {
            Lote lote = loteRepository.findById(stockLote.idLote())
                    .orElseThrow(() -> Status.NOT_FOUND
                            .withDescription("Lote no encontrado: " + stockLote.idLote())
                            .asRuntimeException());

            // Validar que el lote pertenece al SKU
            if (!lote.getSku().equals(inventario.getSku())) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("El lote '" + stockLote.idLote() + "' no pertenece al SKU '"
                                + inventario.getSku() + "'")
                        .asRuntimeException();
            }

            BigDecimal stockAnterior = lote.getCantidadAlmacen();
            BigDecimal stockFisico = stockLote.stockFisicoAlmacen();
            BigDecimal discrepancia = stockFisico.subtract(stockAnterior);

            // Actualizar el lote
            lote.setCantidadAlmacen(stockFisico);
            loteRepository.save(lote);

            // Registrar ajuste
            if (discrepancia.compareTo(BigDecimal.ZERO) != 0) {
                ajustesRealizados.add(new com.isam.dto.inventario.AjusteLoteDto(
                        lote.getIdLote(),
                        lote.getNumeroLote(),
                        "ALMACEN",
                        discrepancia,
                        stockAnterior,
                        stockFisico));

                // Crear movimiento para este lote
                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setSku(inventario.getSku());
                movimiento.setIdLote(lote.getIdLote());
                // Usar el nuevo método para determinar el tipo de ajuste según el signo
                movimiento.setTipoMovimiento(TipoMovimiento.ajustePorCantidad(discrepancia));
                // Convertir discrepancia a valor absoluto (siempre positiva)
                movimiento.setCantidad(discrepancia.abs());
                movimiento.setUnidadMedida(inventario.getUnidadMedida());
                movimiento.setFechaHora(LocalDateTime.now());
                movimiento.setIdUsuario("SYSTEM");
                movimiento.setMotivo("Contabilización manual - Almacén (modo preciso)");
                movimiento.setObservaciones(
                        "Lote: " + lote.getNumeroLote() + ", Discrepancia: " + discrepancia.doubleValue());

                MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);

                // Publicar evento
                inventarioEventService.publicarMovimiento(movimientoGuardado);

                movimientos.add(new com.isam.dto.movimiento.MovimientoInventarioDto(
                        movimientoGuardado.getIdMovimiento(),
                        movimientoGuardado.getSku(),
                        movimientoGuardado.getIdLote(),
                        movimientoGuardado.getTipoMovimiento().name(),
                        movimientoGuardado.getCantidad(),
                        movimientoGuardado.getUnidadMedida().name(),
                        movimientoGuardado.getFechaHora().toString(),
                        movimientoGuardado.getIdUsuario(),
                        movimientoGuardado.getMotivo(),
                        movimientoGuardado.getObservaciones()));
            }

            stockFisicoTotal = stockFisicoTotal.add(stockFisico);
            ajusteTotalInventario = ajusteTotalInventario.add(discrepancia);
        }

        // Actualizar inventario general
        inventario.setCantidadAlmacen(stockFisicoTotal);

        return stockFisicoTotal;
    }

    /**
     * Actualiza la caché local de un producto.
     * Llamado por el consumidor de eventos para mantener el desacoplamiento.
     */
    @Transactional
    public void actualizarCacheProducto(String sku, String nombre, com.isam.model.UnidadMedida unidadMedida,
            String ean, String plu) {
        log.info("Actualizando caché local para SKU: {}", sku);

        ProductoCache cache = productoCacheRepository.findBySku(sku)
                .orElse(new ProductoCache());

        cache.setSku(sku);
        cache.setNombre(nombre);
        cache.setUnidadMedida(unidadMedida);
        cache.setEan(ean != null && !ean.isEmpty() ? ean : null);
        cache.setPlu(plu != null && !plu.isEmpty() ? plu : null);
        cache.setFechaActualizacion(LocalDateTime.now());

        productoCacheRepository.save(cache);
    }
}
