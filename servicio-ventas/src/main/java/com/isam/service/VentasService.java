package com.isam.service;

import com.isam.dto.AnadirProductoTicketRequestDto;
import com.isam.dto.AnadirProductoTicketResponseDto;
import com.isam.dto.CancelarTicketRequestDto;
import com.isam.dto.CancelarTicketResponseDto;
import com.isam.dto.CerrarTicketRequestDto;
import com.isam.dto.CerrarTicketResponseDto;
import com.isam.dto.CrearNuevoTicketResponseDto;
import com.isam.dto.LineaVentaDto;
import com.isam.dto.ProcesarPagoRequestDto;
import com.isam.dto.ProcesarPagoResponseDto;
import com.isam.grpc.catalogo.ProductoProto;
import com.isam.grpc.client.CatalogoGrpcClient;
import com.isam.grpc.client.InventarioGrpcClient;
import com.isam.grpc.inventario.ItemVenta;
import com.isam.model.EstadoTicket;
import com.isam.model.ItemTicket;
import com.isam.model.MetodoPago;
import com.isam.model.Pago;
import com.isam.model.Ticket;
import com.isam.repository.PagoRepository;
import com.isam.repository.TicketRepository;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.grpc.Status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VentasService {

    private final TicketRepository ticketRepository;
    private final PagoRepository pagoRepository;
    private final CatalogoGrpcClient catalogoGrpcClient;
    private final InventarioGrpcClient inventarioGrpcClient;

    @Transactional
    public CrearNuevoTicketResponseDto crearNuevoTicket(String idUsuario, String nombreCajero) {
        
        // Validar que el ID de usuario no sea nulo o vacío
        if (!isNotNullOrEmpty(idUsuario)) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Problema con credenciales login. El ID de usuario es obligatorio.")
                .asRuntimeException();
        }
        
        // Validar que el nombre del cajero no sea nulo o vacío
        if (!isNotNullOrEmpty(nombreCajero)) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Problema con credenciales login. El nombre del cajero es obligatorio")
                .asRuntimeException();
        }


        // Crear nueva entidad Ticket
        Ticket ticket = new Ticket();
        ticket.setIdUsuario(idUsuario.trim());
        
        ticket.setFechaHora(LocalDateTime.now());
        ticket.setEstadoTicket(EstadoTicket.TEMPORAL);
        
        // Guardar en base de datos
        Ticket ticketGuardado = ticketRepository.save(ticket);
        
        // Formatear fecha y hora en formato ISO 8601
        String fechaHoraFormateada = ticketGuardado.getFechaHora()
            .toString();
        
        // Construir y retornar respuesta
        return new CrearNuevoTicketResponseDto(
            ticketGuardado.getIdTicket(),
            fechaHoraFormateada,
            nombreCajero.trim()
        );
    }
    
    /**
     * Añade un producto al ticket temporal
     * @param dto DTO con el ID del ticket temporal y el código de barras del producto
     * @return Respuesta con los detalles del producto añadido y el subtotal actualizado
     */
    @Transactional
    public AnadirProductoTicketResponseDto anadirProductoTicket(AnadirProductoTicketRequestDto dto) {
        log.info("Añadiendo producto al ticket: idTicket='{}', codigoBarras='{}'",
            dto.idTicketTemporal(), dto.codigoBarras());
        
        // Validar que el ID del ticket no sea nulo o vacío
        if (!isNotNullOrEmpty(dto.idTicketTemporal())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El ID del ticket temporal es obligatorio")
                .asRuntimeException();
        }
        
        // Validar que el código de barras no sea nulo o vacío
        if (!isNotNullOrEmpty(dto.codigoBarras())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El código de barras es obligatorio")
                .asRuntimeException();
        }
        
        // Buscar el ticket en la base de datos
        Ticket ticket = ticketRepository.findById(dto.idTicketTemporal())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Ticket temporal no encontrado con ID '" + dto.idTicketTemporal() + "'")
                .asRuntimeException());
        
        // Validar que el ticket esté en estado TEMPORAL
        if (ticket.getEstadoTicket() != EstadoTicket.TEMPORAL) {
            if (ticket.getEstadoTicket() == EstadoTicket.PAGADO) {
                throw Status.FAILED_PRECONDITION
                    .withDescription("El ticket ya está PAGADO. No se pueden añadir más productos.")
                    .asRuntimeException();
            }
            throw Status.FAILED_PRECONDITION
                .withDescription("El ticket no está en estado TEMPORAL. Estado actual: " + ticket.getEstadoTicket())
                .asRuntimeException();
        }

        // Validar que el ticket no tenga un pago ya asociado
        if (ticket.getPago() != null) {
            throw Status.FAILED_PRECONDITION
                .withDescription("No se pueden añadir productos a un ticket que ya tiene un pago procesado")
                .asRuntimeException();
        }
        
        // Traducir el código de barras a SKU usando el servicio de catálogo
        String sku;
        try {
            sku = catalogoGrpcClient.traducirCodigoBarrasASku(dto.codigoBarras());
            if (sku == null || sku.trim().isEmpty()) {
                throw Status.NOT_FOUND
                    .withDescription("No se encontró producto con código de barras '" + dto.codigoBarras() + "'")
                    .asRuntimeException();
            }
        } catch (StatusRuntimeException e) {
            log.error("Error al traducir código de barras: {}", e.getMessage());
            throw e;
        }
        
        // Consultar el producto por SKU usando el servicio de catálogo
        ProductoProto producto;
        try {
            producto = catalogoGrpcClient.consultarProducto(sku);
        } catch (StatusRuntimeException e) {
            log.error("Error al consultar producto: {}", e.getMessage());
            // throw Status.NOT_FOUND
            //     .withDescription("Producto no encontrado con SKU '" + sku + "'")
            //     .asRuntimeException();
            throw e;
        }
        
        // Verificar si el producto ya existe en el ticket
        ItemTicket itemExistente = ticket.getItems().stream()
            .filter(item -> item.getSku().equals(sku))
            .findFirst()
            .orElse(null);
        
        if (itemExistente != null) {
            // Si ya existe, incrementar la cantidad
            BigDecimal nuevaCantidad = itemExistente.getCantidad().add(BigDecimal.ONE);
            itemExistente.setCantidad(nuevaCantidad);
            itemExistente.calcularSubtotal();
            log.debug("Producto existente en ticket, cantidad actualizada a: {}", nuevaCantidad);
        } else {
            // Si no existe, crear un nuevo item
            ItemTicket nuevoItem = new ItemTicket();
            nuevoItem.setTicket(ticket);
            nuevoItem.setNumeroLinea(ticket.getItems().size() + 1);
            nuevoItem.setSku(sku);
            nuevoItem.setNombreProducto(producto.getNombre());
            nuevoItem.setCantidad(BigDecimal.ONE);
            nuevoItem.setPrecioUnitario(new BigDecimal(producto.getPrecioVenta()));
            nuevoItem.setDescuento(BigDecimal.ZERO);
            nuevoItem.calcularSubtotal();
            
            // Añadir el item al ticket
            ticket.addItem(nuevoItem);
            log.debug("Nuevo producto añadido al ticket: SKU='{}', Nombre='{}'", sku, producto.getNombre());
        }
        
        // Recalcular el subtotal del ticket
        ticket.calcularSubtotal();
        
        // Guardar el ticket actualizado
        ticket = ticketRepository.save(ticket);
        
        // Obtener el item añadido/actualizado
        ItemTicket itemFinal = ticket.getItems().stream()
            .filter(item -> item.getSku().equals(sku))
            .findFirst()
            .orElseThrow(() -> Status.INTERNAL
                .withDescription("Error al recuperar el item añadido")
                .asRuntimeException());
        
        // Construir y retornar la respuesta
        return new AnadirProductoTicketResponseDto(
            ticket.getIdTicket(),
            itemFinal.getSku(),
            itemFinal.getIdItemTicket(),
            itemFinal.getNumeroLinea(),
            itemFinal.getNombreProducto(),
            itemFinal.getCantidad(),
            itemFinal.getPrecioUnitario(),
            itemFinal.getSubtotal(),
            ticket.getSubtotal() != null ? ticket.getSubtotal() : BigDecimal.ZERO
        );
    }
    
    /**
     * Procesa el pago de un ticket temporal
     * @param dto DTO con el ID del ticket temporal, método de pago y monto recibido
     * @return Respuesta con los detalles del pago procesado
     */
    @Transactional
    public ProcesarPagoResponseDto procesarPago(ProcesarPagoRequestDto dto) {
        log.info("Procesando pago para ticket: idTicket='{}', metodoPago='{}', montoRecibido={}",
            dto.idTicketTemporal(), dto.metodoPago(), dto.montoRecibido());
        
        // Validar que el ID del ticket no sea nulo o vacío
        if (!isNotNullOrEmpty(dto.idTicketTemporal())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El ID del ticket temporal es obligatorio")
                .asRuntimeException();
        }
        
        // Validar que el método de pago no sea nulo
        if (dto.metodoPago() == null) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El método de pago es obligatorio")
                .asRuntimeException();
        }
        
        // Validar que el monto recibido no sea nulo
        if (dto.montoRecibido() == null) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El monto recibido es obligatorio")
                .asRuntimeException();
        }
        
        // Validar que el monto recibido sea mayor que 0
        if (dto.montoRecibido().compareTo(BigDecimal.ZERO) <= 0) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El monto recibido debe ser mayor que 0")
                .asRuntimeException();
        }
        
        // Buscar el ticket en la base de datos
        Ticket ticket = ticketRepository.findById(dto.idTicketTemporal())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Ticket temporal no encontrado con ID '" + dto.idTicketTemporal() + "'")
                .asRuntimeException());
        
        // Validar que el ticket esté en estado TEMPORAL
        if (ticket.getEstadoTicket() != EstadoTicket.TEMPORAL) {
            if (ticket.getEstadoTicket() == EstadoTicket.PAGADO) {
                throw Status.FAILED_PRECONDITION
                    .withDescription("El ticket ya está PAGADO. No se puede volver a cobrar. En el futuro se implementará la posibilidad de pagar mezclando varios metodos a la vez.")
                    .asRuntimeException();
            }
            throw Status.FAILED_PRECONDITION
                .withDescription("El ticket no está en estado TEMPORAL. Estado actual: " + ticket.getEstadoTicket())
                .asRuntimeException();
        }

        // Validar que el ticket no tenga un pago ya asociado
        if (ticket.getPago() != null) {
            throw Status.FAILED_PRECONDITION
                .withDescription("El ticket ya tiene un pago asociado. De momento no soportamos el pago de los productos con 2 medio de pago distintos a la vez. P.e. No se puede pagar una parte del ticket con tarjeta y complementarlo con un pago en efectivo.)")
                .asRuntimeException();
        }
        
        // Validar que el ticket tenga items
        if (ticket.getItems() == null || ticket.getItems().isEmpty()) {
            throw Status.FAILED_PRECONDITION
                .withDescription("El ticket no tiene productos. Debe añadir al menos un producto antes de procesar el pago")
                .asRuntimeException();
        }
        
        // Validar que el ticket ya tenga un pago asociado
        if (ticket.getPago() != null) {
            throw Status.FAILED_PRECONDITION
                .withDescription("El ticket ya tiene un pago procesado")
                .asRuntimeException();
        }
        
        // Calcular el total del ticket (por ahora sin impuestos)
        // TODO: Implementar cálculo de impuestos cuando esté disponible
        ticket.calcularSubtotal();
        BigDecimal totalTicket = ticket.getSubtotal();
        
        // Validar que el monto recibido sea suficiente
        if (dto.montoRecibido().compareTo(totalTicket) < 0) {
            throw Status.FAILED_PRECONDITION
                .withDescription(String.format(
                    "El monto recibido (%.2f) es insuficiente. Total del ticket: %.2f",
                    dto.montoRecibido(), totalTicket))
                .asRuntimeException();
        }
        
        // Crear el pago
        Pago pago = new Pago();
        pago.setTicket(ticket);
        pago.setMetodoPago(dto.metodoPago());
        pago.setMontoRecibido(dto.montoRecibido());
        pago.setFechaHora(LocalDateTime.now());
        
        // Calcular el cambio
        pago.calcularCambio(totalTicket);
        
        // Guardar el pago
        pago = pagoRepository.save(pago);
        
        // Asociar el pago al ticket
        ticket.setPago(pago);
        ticket.setIdPago(pago.getIdPago());
        ticket.setEstadoTicket(EstadoTicket.PAGADO);
        ticketRepository.save(ticket);
        
        log.info("Pago procesado exitosamente: idPago='{}', montoCambio={}",
            pago.getIdPago(), pago.getMontoCambio());
        
        // Construir y retornar la respuesta
        return new ProcesarPagoResponseDto(
            pago.getIdPago(),
            ticket.getIdTicket(),
            pago.getMetodoPago(),
            pago.getMontoRecibido(),
            pago.getMontoCambio()
        );
    }
    
    /**
     * Cierra un ticket de venta y notifica al servicio de inventario
     * @param dto DTO con el ID del ticket temporal a cerrar
     * @return Respuesta con los detalles del ticket cerrado
     */
    @Transactional
    public CerrarTicketResponseDto cerrarTicket(CerrarTicketRequestDto dto) {
        log.info("Cerrando ticket: idTicket='{}'", dto.idTicketTemporal());
        
        // Validar que el ID del ticket no sea nulo o vacío
        if (!isNotNullOrEmpty(dto.idTicketTemporal())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El ID del ticket temporal es obligatorio")
                .asRuntimeException();
        }
        
        // Buscar el ticket en la base de datos
        Ticket ticket = ticketRepository.findById(dto.idTicketTemporal())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Ticket temporal no encontrado con ID '" + dto.idTicketTemporal() + "'")
                .asRuntimeException());
        
        // Validar que el ticket esté en estado PAGADO
        if (ticket.getEstadoTicket() != EstadoTicket.PAGADO) {
            // Si sigue en TEMPORAL, es que se saltaron el pago
            if (ticket.getEstadoTicket() == EstadoTicket.TEMPORAL) {
                throw Status.FAILED_PRECONDITION
                    .withDescription("El ticket está en estado TEMPORAL. Debe procesar el pago antes de cerrarlo.")
                    .asRuntimeException();
            }
            // Si está CERRADO o CANCELADO
            throw Status.FAILED_PRECONDITION
                .withDescription("El ticket no está en estado PAGADO válido para cierre. Estado actual: " + ticket.getEstadoTicket())
                .asRuntimeException();
        }

        // Validar que el ticket no tenga un pago ya asociado
        if (ticket.getPago() == null) {
            throw Status.FAILED_PRECONDITION
                .withDescription("El ticket no tiene un pago procesado. Debe procesar el pago antes de cerrar el ticket")
                .asRuntimeException();
        }
        
        // Validar que el ticket tenga items
        if (ticket.getItems() == null || ticket.getItems().isEmpty()) {
            throw Status.FAILED_PRECONDITION
                .withDescription("El ticket no tiene productos. Debe añadir al menos un producto antes de cerrar el ticket")
                .asRuntimeException();
        }
        

        // Creamos un numero de ticket de maximo 50 caracteres
        Long siguienteNumero = ticketRepository.getNextTicketNumber();
        String anio = String.valueOf(LocalDate.now().getYear());
        String numeroTicket = String.format("T-%s-%07d", anio, siguienteNumero);
        ticket.setNumeroTicket(numeroTicket);
        
        // Calcular totales
        ticket.calcularSubtotal();
        // TODO: Implementar cálculo de impuestos cuando esté disponible
        ticket.setTotalImpuestos(BigDecimal.ZERO);
        ticket.setTotal(ticket.getSubtotal());
        
        // Cambiar estado del ticket a CERRADO
        ticket.setEstadoTicket(EstadoTicket.CERRADO);
        
        // Guardar el ticket actualizado
        ticket = ticketRepository.save(ticket);
        
        // Registrar movimientos de venta en inventario
        registrarMovimientosVentaEnInventario(ticket);
        
        // Construir lista de líneas de venta
        List<LineaVentaDto> lineasVenta = new ArrayList<>();
        for (ItemTicket item : ticket.getItems()) {
            LineaVentaDto lineaVenta = new LineaVentaDto(
                item.getNumeroLinea(),
                item.getSku(),
                item.getNombreProducto(),
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.getDescuento() != null ? item.getDescuento() : BigDecimal.ZERO,
                null, // promocionAplicada - TODO: implementar cuando esté disponible
                item.getSubtotal(),
                item.getImpuesto() != null ? item.getImpuesto() : BigDecimal.ZERO
            );
            lineasVenta.add(lineaVenta);
        }
        
        // TODO: Obtener nombre del cajero del contexto de autenticación
        String nombreCajero = "Cajero Temporal";
        
        log.info("Ticket cerrado exitosamente: numeroTicket='{}', total={}",
            ticket.getNumeroTicket(), ticket.getTotal());
        
        // Construir y retornar la respuesta
        return new CerrarTicketResponseDto(
            ticket.getNumeroTicket(),
            ticket.getFechaHora().toString(),
            nombreCajero,
            lineasVenta,
            ticket.getSubtotal(),
            ticket.getTotalImpuestos(),
            ticket.getTotal(),
            ticket.getPago().getMetodoPago(),
            ticket.getPago().getMontoRecibido(),
            ticket.getPago().getMontoCambio()
        );
    }
    
    /**
     * Registra los movimientos de venta en el servicio de inventario
     * Este método notifica al servicio de inventario sobre las salidas de productos por venta
     * @param ticket Ticket con los items vendidos
     */
    private void registrarMovimientosVentaEnInventario(Ticket ticket) {
        log.info("Registrando movimientos de venta en inventario para ticket: {}", ticket.getNumeroTicket());
        
        try {
            // Construir lista de items para el servicio de inventario
            List<ItemVenta> itemsVenta = new ArrayList<>();
            
            for (ItemTicket item : ticket.getItems()) {
                ItemVenta itemVenta = ItemVenta.newBuilder()
                    .setSku(item.getSku())
                    .setCantidad(item.getCantidad().toPlainString())
                    .build();
                itemsVenta.add(itemVenta);
            }
            
            // Registrar la venta completa en inventario
            inventarioGrpcClient.registrarVenta(ticket.getNumeroTicket(), itemsVenta);
            
            log.info("Venta registrada exitosamente en inventario: Ticket='{}'", ticket.getNumeroTicket());
            
        } catch (StatusRuntimeException e) {
            // Log del error pero no interrumpir el proceso de cierre del ticket
            // El ticket ya está cerrado, solo estamos notificando a inventario
            log.error("Error al registrar venta en inventario: Ticket='{}', Error: {}",
                ticket.getNumeroTicket(), e.getMessage());
            
            // TODO: Implementar mecanismo de reintento o cola de mensajes para casos de fallo
            // Por ahora solo logueamos el error
        }
    }
    
    /**
     * Consulta el contenido de un ticket (temporal o cerrado)
     * @param dto DTO con el ID del ticket o número de ticket
     * @return Respuesta con los detalles completos del ticket
     */
    @Transactional(readOnly = true)
    public com.isam.dto.ConsultarTicketResponseDto consultarTicket(com.isam.dto.ConsultarTicketRequestDto dto) {
        
        log.info("Consultando ticket: idTicket='{}', numeroTicket='{}'",
            dto.idTicket(), dto.numeroTicket());
        
        // Buscar el ticket por ID o número
        Ticket ticket;
        if (dto.idTicket() != null && !dto.idTicket().trim().isEmpty()) {
            // Buscar por ID
            ticket = ticketRepository.findById(dto.idTicket())
                .orElseThrow(() -> Status.NOT_FOUND
                    .withDescription("Ticket no encontrado con ID '" + dto.idTicket() + "'")
                    .asRuntimeException());
        } else {
            // Buscar por número de ticket
            ticket = ticketRepository.findByNumeroTicket(dto.numeroTicket())
                .orElseThrow(() -> Status.NOT_FOUND
                    .withDescription("Ticket no encontrado con número '" + dto.numeroTicket() + "'")
                    .asRuntimeException());
        }
        
        // Construir lista de líneas de venta
        List<LineaVentaDto> lineasVenta = new ArrayList<>();
        for (ItemTicket item : ticket.getItems()) {
            LineaVentaDto lineaVenta = new LineaVentaDto(
                item.getNumeroLinea(),
                item.getSku(),
                item.getNombreProducto(),
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.getDescuento() != null ? item.getDescuento() : BigDecimal.ZERO,
                null, // promocionAplicada - TODO: implementar cuando esté disponible
                item.getSubtotal(),
                item.getImpuesto() != null ? item.getImpuesto() : BigDecimal.ZERO
            );
            lineasVenta.add(lineaVenta);
        }
        
        // Obtener información del pago si existe
        MetodoPago metodoPago = null;
        BigDecimal montoRecibido = null;
        BigDecimal montoCambio = null;
        
        if (ticket.getPago() != null) {
            metodoPago = ticket.getPago().getMetodoPago();
            montoRecibido = ticket.getPago().getMontoRecibido();
            montoCambio = ticket.getPago().getMontoCambio();
        }
        
        // TODO: Obtener nombre del cajero del contexto de autenticación o de la base de datos
        String nombreCajero = "Cajero " + ticket.getIdUsuario();
        
        log.info("Ticket consultado exitosamente: numeroTicket='{}', estado={}",
            ticket.getNumeroTicket(), ticket.getEstadoTicket());
        
        // Construir y retornar la respuesta
        return new com.isam.dto.ConsultarTicketResponseDto(
            ticket.getIdTicket(),
            ticket.getNumeroTicket(),
            ticket.getFechaHora().toString(),
            nombreCajero,
            lineasVenta,
            ticket.getSubtotal() != null ? ticket.getSubtotal() : BigDecimal.ZERO,
            ticket.getTotalImpuestos() != null ? ticket.getTotalImpuestos() : BigDecimal.ZERO,
            ticket.getTotal() != null ? ticket.getTotal() : BigDecimal.ZERO,
            metodoPago,
            montoRecibido,
            montoCambio,
            ticket.getEstadoTicket()
        );
    }
    
    private boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Formatea un BigDecimal a String asegurando que el separador decimal sea punto (.).
     * Esto garantiza consistencia independientemente de la configuración regional del sistema.
     * @param precio BigDecimal a formatear
     * @return String con formato "0.00" usando punto como separador decimal
     */
    private String formatPrecio(BigDecimal precio) {
        if (precio == null) {
            return "0.00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#0.00", symbols);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(precio);
    }
    
    /**
     * Cancela un ticket temporal o pagado
     * @param dto DTO con el ID del ticket a cancelar
     * @return Respuesta con los detalles de la cancelación
     */
    @Transactional
    public CancelarTicketResponseDto cancelarTicket(CancelarTicketRequestDto dto) {
        log.info("Cancelando ticket: idTicket='{}'", dto.idTicket());
        
        // Validar que el ID del ticket no sea nulo o vacío
        if (!isNotNullOrEmpty(dto.idTicket())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El ID del ticket es obligatorio")
                .asRuntimeException();
        }
        
        // Buscar el ticket en la base de datos
        Ticket ticket = ticketRepository.findById(dto.idTicket())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Ticket no encontrado con ID '" + dto.idTicket() + "'")
                .asRuntimeException());
        
        // Validar que el ticket esté en estado TEMPORAL o PAGADO
        if (ticket.getEstadoTicket() != EstadoTicket.TEMPORAL && ticket.getEstadoTicket() != EstadoTicket.PAGADO) {
            if (ticket.getEstadoTicket() == EstadoTicket.CERRADO) {
                throw Status.FAILED_PRECONDITION
                    .withDescription("El ticket está CERRADO. No se puede cancelar. Para revertir una venta cerrada, debe usar el proceso de devoluciones.")
                    .asRuntimeException();
            }
            if (ticket.getEstadoTicket() == EstadoTicket.CANCELADO) {
                throw Status.FAILED_PRECONDITION
                    .withDescription("El ticket ya está CANCELADO.")
                    .asRuntimeException();
            }
            throw Status.FAILED_PRECONDITION
                .withDescription("El ticket no está en un estado válido para cancelación. Estado actual: " + ticket.getEstadoTicket())
                .asRuntimeException();
        }
        
        // TODO: Validar que el usuario sea el creador del ticket cuando se implemente autenticación
        // Por ahora, cualquier usuario puede cancelar cualquier ticket
        
        // Preparar respuesta según si tiene pago o no
        java.util.Optional<BigDecimal> montoADevolver = java.util.Optional.empty();
        java.util.Optional<MetodoPago> metodoPagoOriginal = java.util.Optional.empty();
        
        if (ticket.getPago() != null) {
            // Ticket con pago asociado - se debe devolver dinero
            montoADevolver = java.util.Optional.of(ticket.getPago().getMontoRecibido());
            metodoPagoOriginal = java.util.Optional.of(ticket.getPago().getMetodoPago());
            
            log.info("Ticket con pago cancelado. Monto a devolver: {}, Método: {}",
                ticket.getPago().getMontoRecibido(), ticket.getPago().getMetodoPago());
        } else {
            // Ticket sin pago - cancelación simple            
            log.info("Ticket sin pago cancelado exitosamente.");
        }
        
        // Cambiar estado del ticket a CANCELADO
        ticket.setEstadoTicket(EstadoTicket.CANCELADO);
        
        // Guardar el ticket actualizado
        ticketRepository.save(ticket);
        
        log.info("Ticket cancelado exitosamente: idTicket='{}', estadoFinal={}",
            ticket.getIdTicket(), ticket.getEstadoTicket());
        
        // Construir y retornar la respuesta
        return new CancelarTicketResponseDto(
            ticket.getIdTicket(),
            montoADevolver,
            metodoPagoOriginal
        );
    }
}