package com.isam.service;

import com.isam.dto.AnadirProductoTicketRequestDto;
import com.isam.dto.AnadirProductoTicketResponseDto;
import com.isam.dto.CrearNuevoTicketResponseDto;
import com.isam.grpc.catalogo.ProductoProto;
import com.isam.grpc.client.CatalogoGrpcClient;
import com.isam.model.EstadoTicket;
import com.isam.model.ItemTicket;
import com.isam.model.Ticket;
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
import java.util.Locale;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VentasService {

    private final TicketRepository ticketRepository;
    private final CatalogoGrpcClient catalogoGrpcClient;

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

        Long siguienteNumero = ticketRepository.getNextTicketNumber();
        
        // Creamos un id de ticket de maximo 50 caracteres
        String anio = String.valueOf(LocalDate.now().getYear());
        String numeroTicket = String.format("T-%s-%07d", anio, siguienteNumero);
        

        // Crear nueva entidad Ticket
        Ticket ticket = new Ticket();
        ticket.setIdUsuario(idUsuario.trim());
        ticket.setNumeroTicket(numeroTicket);
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
            numeroTicket,
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
            throw Status.FAILED_PRECONDITION
                .withDescription("El ticket no está en estado TEMPORAL. Estado actual: " + ticket.getEstadoTicket())
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
}