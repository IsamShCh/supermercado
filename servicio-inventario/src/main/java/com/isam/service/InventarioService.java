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
    @Autowired
    private AjusteInventarioService ajusteInventarioService;

    
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
     * Delega la lógica al servicio especializado AjusteInventarioService.
     */
    @Transactional
    public com.isam.dto.inventario.AjustarInventarioManualResponseDto ajustarInventarioManual(com.isam.dto.inventario.AjustarInventarioManualRequestDto dto) {
        return ajusteInventarioService.ajustarInventarioManual(dto);
    }
    /**
     * Contabiliza el stock manualmente (AC19).
     * Permite contabilizar estantería, almacén, o ambos.
     * Para almacén, admite dos modalidades: rápida (total) o precisa (por lotes).
     */
    @Transactional
    public com.isam.dto.inventario.ContabilizarStockManualResponseDto contabilizarStockManual(
            com.isam.dto.inventario.ContabilizarStockManualRequestDto dto) {
        
        // Buscar el inventario para este SKU
        Inventario inventario = inventarioRepository.findBySku(dto.sku())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Inventario no encontrado para SKU '" + dto.sku() + "'")
                .asRuntimeException());

        // Variables para el reporte
        double stockLogicoEstanteria = inventario.getCantidadEstanteria().doubleValue();
        double stockFisicoEstanteria = dto.stockFisicoEstanteria() != null ? dto.stockFisicoEstanteria() : stockLogicoEstanteria;
        double stockLogicoAlmacen = inventario.getCantidadAlmacen().doubleValue();
        double stockFisicoAlmacen = stockLogicoAlmacen;
        
        List<com.isam.dto.inventario.AjusteLoteDto> ajustesRealizados = new java.util.ArrayList<>();
        List<com.isam.dto.movimiento.MovimientoInventarioDto> movimientos = new java.util.ArrayList<>();

        // Procesar contabilización de estantería si está presente
        if (dto.stockFisicoEstanteria() != null) {
            procesarContabilizacionEstanteria(
                inventario, 
                dto.stockFisicoEstanteria(), 
                stockLogicoEstanteria,
                ajustesRealizados,
                movimientos
            );
        }

        // Procesar contabilización de almacén si se ha proporcionado
        if (dto.contabilizacionLotes() != null) {
            // Modalidad precisa: por lotes
            stockFisicoAlmacen = procesarContabilizacionAlmacenPorLotes(
                inventario,
                dto.contabilizacionLotes(),
                stockLogicoAlmacen,
                ajustesRealizados,
                movimientos
            );
        }

        // Guardar inventario actualizado
        Inventario inventarioActualizado = inventarioRepository.save(inventario);

        // Construir DTOs de respuesta
        com.isam.dto.inventario.InventarioDto inventarioDto = new com.isam.dto.inventario.InventarioDto(
            inventarioActualizado.getIdInventario(),
            inventarioActualizado.getSku(),
            inventarioActualizado.getEan(),
            inventarioActualizado.getPlu(),
            inventarioActualizado.getCantidadAlmacen().doubleValue(),
            inventarioActualizado.getCantidadEstanteria().doubleValue(),
            inventarioActualizado.getUnidadMedida().name()
        );

        // Construir reporte de discrepancias
        com.isam.dto.inventario.ReporteDiscrepanciasDto reporte = new com.isam.dto.inventario.ReporteDiscrepanciasDto(
            dto.sku(),
            stockLogicoEstanteria,
            stockFisicoEstanteria,
            stockFisicoEstanteria - stockLogicoEstanteria,
            stockLogicoAlmacen,
            stockFisicoAlmacen,
            stockFisicoAlmacen - stockLogicoAlmacen,
            ajustesRealizados
        );

        return new com.isam.dto.inventario.ContabilizarStockManualResponseDto(
            inventarioDto,
            movimientos,
            reporte
        );
    }

    private void procesarContabilizacionEstanteria(
            Inventario inventario,
            double stockFisico,
            double stockLogico,
            List<com.isam.dto.inventario.AjusteLoteDto> ajustesRealizados,
            List<com.isam.dto.movimiento.MovimientoInventarioDto> movimientos) {
        
        BigDecimal discrepancia = BigDecimal.valueOf(stockFisico).subtract(BigDecimal.valueOf(stockLogico));
        
        if (discrepancia.compareTo(BigDecimal.ZERO) == 0) {
            return; // No hay discrepancia, no hacer nada
        }

        // Obtener lotes con stock en estantería
        List<Lote> lotes = loteRepository.findBySkuAndCantidadEstanteriaGreaterThan(
            inventario.getSku(), 
            BigDecimal.ZERO
        ); // findBySkuAndCantidadEstanteriaGreaterThan() ejecuta una query personalizada que tiene un "ORDER BY l.fechaIngreso ASC"

        // Distribuir el ajuste entre los lotes usando FIFO
        BigDecimal restante = discrepancia;
        
        for (Lote lote : lotes) {
            if (restante.compareTo(BigDecimal.ZERO) == 0) break;
            
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
                ajuste.doubleValue(),
                stockAnterior.doubleValue(),
                lote.getCantidadEstanteria().doubleValue()
            ));
            
            restante = restante.subtract(ajuste);
        }

        // Actualizar inventario general
        inventario.setCantidadEstanteria(BigDecimal.valueOf(stockFisico));

        // Crear movimiento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setSku(inventario.getSku());
        movimiento.setIdLote(null);
        movimiento.setTipoMovimiento(TipoMovimiento.AJUSTE);
        movimiento.setCantidad(discrepancia);
        movimiento.setUnidadMedida(inventario.getUnidadMedida());
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setIdUsuario("SYSTEM");
        movimiento.setMotivo("Contabilización manual - Estantería");
        movimiento.setObservaciones("Discrepancia: " + discrepancia.doubleValue());
        
        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);
        
        movimientos.add(new com.isam.dto.movimiento.MovimientoInventarioDto(
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
        ));
    }

    private double procesarContabilizacionAlmacenPorLotes(
            Inventario inventario,
            com.isam.dto.inventario.ContabilizacionPorLotesDto contabilizacion,
            double stockLogico,
            List<com.isam.dto.inventario.AjusteLoteDto> ajustesRealizados,
            List<com.isam.dto.movimiento.MovimientoInventarioDto> movimientos) {
        
        double stockFisicoTotal = 0.0;
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
                    .withDescription("El lote '" + stockLote.idLote() + "' no pertenece al SKU '" + inventario.getSku() + "'")
                    .asRuntimeException();
            }

            BigDecimal stockAnterior = lote.getCantidadAlmacen();
            BigDecimal stockFisico = BigDecimal.valueOf(stockLote.stockFisicoAlmacen());
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
                    discrepancia.doubleValue(),
                    stockAnterior.doubleValue(),
                    stockFisico.doubleValue()
                ));

                // Crear movimiento para este lote
                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setSku(inventario.getSku());
                movimiento.setIdLote(lote.getIdLote());
                movimiento.setTipoMovimiento(TipoMovimiento.AJUSTE);
                movimiento.setCantidad(discrepancia);
                movimiento.setUnidadMedida(inventario.getUnidadMedida());
                movimiento.setFechaHora(LocalDateTime.now());
                movimiento.setIdUsuario("SYSTEM");
                movimiento.setMotivo("Contabilización manual - Almacén (modo preciso)");
                movimiento.setObservaciones("Lote: " + lote.getNumeroLote() + ", Discrepancia: " + discrepancia.doubleValue());
                
                MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);
                
                movimientos.add(new com.isam.dto.movimiento.MovimientoInventarioDto(
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
                ));
            }

            stockFisicoTotal += stockFisico.doubleValue();
            ajusteTotalInventario = ajusteTotalInventario.add(discrepancia);
        }

        // Actualizar inventario general
        inventario.setCantidadAlmacen(BigDecimal.valueOf(stockFisicoTotal));

        return stockFisicoTotal;
    }



}
