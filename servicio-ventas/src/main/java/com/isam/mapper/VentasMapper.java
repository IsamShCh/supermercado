package com.isam.mapper;

import com.isam.dto.AnadirProductoTicketRequestDto;
import com.isam.dto.AnadirProductoTicketResponseDto;
import com.isam.dto.CerrarTicketRequestDto;
import com.isam.dto.CerrarTicketResponseDto;
import com.isam.dto.ConsultarTicketRequestDto;
import com.isam.dto.ConsultarTicketResponseDto;
import com.isam.dto.CrearNuevoTicketResponseDto;
import com.isam.dto.LineaVentaDto;
import com.isam.dto.ProcesarPagoRequestDto;
import com.isam.dto.ProcesarPagoResponseDto;
import com.isam.grpc.ventas.AnadirProductoTicketRequest;
import com.isam.grpc.ventas.CerrarTicketRequest;
import com.isam.grpc.ventas.ConsultarTicketRequest;
import com.isam.grpc.ventas.CrearNuevoTicketRequest;
import com.isam.grpc.ventas.EstadoTicket;
import com.isam.grpc.ventas.LineaVentaProto;
import com.isam.grpc.ventas.MetodoPago;
import com.isam.grpc.ventas.ProcesarPagoRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Component
public class VentasMapper {

    public CrearNuevoTicketRequest.Response toProto(CrearNuevoTicketResponseDto dto) {
        if (dto == null) {
            return null;
        }

        return CrearNuevoTicketRequest.Response.newBuilder()
            .setIdTicketTemporal(dto.idTicketTemporal())
            .setFechaHoraCreacion(dto.fechaHoraCreacion())
            .setNombreCajero(dto.nombreCajero())
            .build();
    }
    
    /**
     * Convierte un proto request de añadir producto a DTO
     * @param request Proto request de añadir producto
     * @return DTO con los datos del request
     */
    public AnadirProductoTicketRequestDto toDto(AnadirProductoTicketRequest request) {
        if (request == null) {
            return null;
        }
        
        return new AnadirProductoTicketRequestDto(
            request.getIdTicketTemporal(),
            request.getCodigoBarras()
        );
    }
    
    /**
     * Convierte un DTO de respuesta de añadir producto a proto
     * @param dto DTO de respuesta
     * @return Proto response
     */
    public AnadirProductoTicketRequest.Response toProto(AnadirProductoTicketResponseDto dto) {
        if (dto == null) {
            return null;
        }
        
        return AnadirProductoTicketRequest.Response.newBuilder()
            .setIdTicketTemporal(dto.idTicketTemporal())
            .setSku(dto.sku())
            .setIdItemTicket(dto.idItemTicket())
            .setNumeroLinea(dto.numeroLinea())
            .setNombreProducto(dto.nombreProducto())
            .setCantidad(dto.cantidad().toPlainString())
            .setPrecioUnitario(dto.precioUnitario().toPlainString())
            .setSubtotal(dto.subtotal().toPlainString())
            .setSubtotalTicketActual(dto.subtotalTicketActual().toPlainString())
            .build();
    }
    
    /**
     * Convierte un proto request de procesar pago a DTO
     * @param request Proto request de procesar pago
     * @return DTO con los datos del request
     */
    public ProcesarPagoRequestDto toDto(ProcesarPagoRequest request) {
        if (request == null) {
            return null;
        }
        
        // Convertir el enum de proto a enum de modelo
        com.isam.model.MetodoPago metodoPago = convertMetodoPago(request.getMetodoPago());
        
        // Convertir el monto recibido de String a BigDecimal
        BigDecimal montoRecibido;
        try {
            montoRecibido = new BigDecimal(request.getMontoRecibido());
        } catch (NumberFormatException e) {
            throw io.grpc.Status.INVALID_ARGUMENT
                .withDescription("Formato de monto recibido inválido: '" + request.getMontoRecibido() + "' no es un número válido.")
                .asRuntimeException();
        }
        
        return new ProcesarPagoRequestDto(
            request.getIdTicketTemporal(),
            metodoPago,
            montoRecibido
        );
    }
    
    /**
     * Convierte un DTO de respuesta de procesar pago a proto
     * @param dto DTO de respuesta
     * @return Proto response
     */
    public ProcesarPagoRequest.Response toProto(ProcesarPagoResponseDto dto) {
        if (dto == null) {
            return null;
        }
        
        // Convertir el enum de modelo a enum de proto
        MetodoPago metodoPagoProto = convertMetodoPagoToProto(dto.metodoPago());
        
        return ProcesarPagoRequest.Response.newBuilder()
            .setIdPago(dto.idPago())
            .setIdTicketTemporal(dto.idTicketTemporal())
            .setMetodoPago(metodoPagoProto)
            .setMontoRecibido(dto.montoRecibido().toPlainString())
            .setMontoCambio(dto.montoCambio().toPlainString())
            .build();
    }
    
    /**
     * Convierte el enum MetodoPago de proto a modelo
     * @param metodoPagoProto Enum de proto
     * @return Enum de modelo
     */
    private com.isam.model.MetodoPago convertMetodoPago(MetodoPago metodoPagoProto) {
        return switch (metodoPagoProto) {
            case EFECTIVO -> com.isam.model.MetodoPago.EFECTIVO;
            case TARJETA_DEBITO -> com.isam.model.MetodoPago.TARJETA_DEBITO;
            case TARJETA_CREDITO -> com.isam.model.MetodoPago.TARJETA_CREDITO;
            case TRANSFERENCIA -> com.isam.model.MetodoPago.TRANSFERENCIA;
            default -> throw new IllegalArgumentException("Método de pago no válido: " + metodoPagoProto);
        };
    }
    
    /**
     * Convierte el enum MetodoPago de modelo a proto
     * @param metodoPago Enum de modelo
     * @return Enum de proto
     */
    private MetodoPago convertMetodoPagoToProto(com.isam.model.MetodoPago metodoPago) {
        return switch (metodoPago) {
            case EFECTIVO -> MetodoPago.EFECTIVO;
            case TARJETA_DEBITO -> MetodoPago.TARJETA_DEBITO;
            case TARJETA_CREDITO -> MetodoPago.TARJETA_CREDITO;
            case TRANSFERENCIA -> MetodoPago.TRANSFERENCIA;
        };
    }
    
    /**
     * Convierte un proto request de cerrar ticket a DTO
     * @param request Proto request de cerrar ticket
     * @return DTO con los datos del request
     */
    public CerrarTicketRequestDto toDto(CerrarTicketRequest request) {
        if (request == null) {
            return null;
        }
        
        return new CerrarTicketRequestDto(
            request.getIdTicketTemporal()
        );
    }
    
    /**
     * Convierte un DTO de respuesta de cerrar ticket a proto
     * @param dto DTO de respuesta
     * @return Proto response
     */
    public CerrarTicketRequest.Response toProto(CerrarTicketResponseDto dto) {
        if (dto == null) {
            return null;
        }
        
        // Convertir el enum de modelo a enum de proto
        MetodoPago metodoPagoProto = convertMetodoPagoToProto(dto.metodoPago());
        
        // Convertir las líneas de venta
        var lineasVentaProto = dto.lineasVenta().stream()
            .map(this::convertLineaVentaToProto)
            .collect(Collectors.toList());
        
        return CerrarTicketRequest.Response.newBuilder()
            .setNumeroTicket(dto.numeroTicket())
            .setFechaHora(dto.fechaHora())
            .setNombreCajero(dto.nombreCajero())
            .addAllLineasVenta(lineasVentaProto)
            .setSubtotal(dto.subtotal().toPlainString())
            .setTotalImpuestos(dto.totalImpuestos().toPlainString())
            .setTotal(dto.total().toPlainString())
            .setMetodoPago(metodoPagoProto)
            .setMontoRecibido(dto.montoRecibido().toPlainString())
            .setMontoCambio(dto.montoCambio().toPlainString())
            .build();
    }
    
    /**
     * Convierte un LineaVentaDto a LineaVentaProto
     * @param dto DTO de línea de venta
     * @return Proto de línea de venta
     */
    private LineaVentaProto convertLineaVentaToProto(LineaVentaDto dto) {
        LineaVentaProto.Builder builder = LineaVentaProto.newBuilder()
            .setNumeroLinea(dto.numeroLinea())
            .setSku(dto.sku())
            .setDescripcion(dto.descripcion())
            .setCantidad(dto.cantidad().toPlainString())
            .setPrecioUnitario(dto.precioUnitario().toPlainString())
            .setSubtotal(dto.subtotal().toPlainString())
            .setImpuesto(dto.impuesto().toPlainString());
        
        // Campos opcionales
        if (dto.descuento() != null && dto.descuento().compareTo(BigDecimal.ZERO) > 0) {
            builder.setDescuento(dto.descuento().toPlainString());
        }
        
        if (dto.promocionAplicada() != null && !dto.promocionAplicada().isEmpty()) {
            builder.setPromocionAplicada(dto.promocionAplicada());
        }
        
        return builder.build();
    }
    
    /**
     * Convierte un proto request de consultar ticket a DTO
     * @param request Proto request de consultar ticket
     * @return DTO con los datos del request
     */
    public ConsultarTicketRequestDto toDto(ConsultarTicketRequest request) {
        if (request == null) {
            return null;
        }
        
        String idTicket = null;
        String numeroTicket = null;
        
        // Manejar el oneof identificador_ticket
        switch (request.getIdentificadorTicketCase()) {
            case ID_TICKET:
                idTicket = request.getIdTicket();
                break;
            case NUMERO_TICKET:
                numeroTicket = request.getNumeroTicket();
                break;
            case IDENTIFICADORTICKET_NOT_SET:
                // No se ha establecido ningún identificador
                break;
        }
        
        return new ConsultarTicketRequestDto(
            idTicket,
            numeroTicket
        );
    }
    
    /**
     * Convierte un DTO de respuesta de consultar ticket a proto
     * @param dto DTO de respuesta
     * @return Proto response
     */
    public ConsultarTicketRequest.Response toProto(ConsultarTicketResponseDto dto) {
        if (dto == null) {
            return null;
        }
        
        // Convertir las líneas de venta
        var lineasVentaProto = dto.lineasVenta().stream()
            .map(this::convertLineaVentaToProto)
            .collect(Collectors.toList());
        
        // Construir el builder
        ConsultarTicketRequest.Response.Builder builder = ConsultarTicketRequest.Response.newBuilder()
            .setIdTicket(dto.idTicket())
            .setFechaHora(dto.fechaHora())
            .setNombreCajero(dto.nombreCajero())
            .addAllLineasVenta(lineasVentaProto)
            .setSubtotal(dto.subtotal().toPlainString())
            .setTotalImpuestos(dto.totalImpuestos().toPlainString())
            .setTotal(dto.total().toPlainString())
            .setEstado(convertEstadoTicketToProto(dto.estado()));
        
        // Campos opcionales
        if (dto.numeroTicket() != null && !dto.numeroTicket().isEmpty()) {
            builder.setNumeroTicket(dto.numeroTicket());
        }
        
        if (dto.metodoPago() != null) {
            builder.setMetodoPago(convertMetodoPagoToProto(dto.metodoPago()));
        }
        
        if (dto.montoRecibido() != null) {
            builder.setMontoRecibido(dto.montoRecibido().toPlainString());
        }
        
        if (dto.montoCambio() != null) {
            builder.setMontoCambio(dto.montoCambio().toPlainString());
        }
        
        return builder.build();
    }
    
    /**
     * Convierte el enum EstadoTicket de modelo a proto
     * @param estado Enum de modelo
     * @return Enum de proto
     */
    private EstadoTicket convertEstadoTicketToProto(com.isam.model.EstadoTicket estado) {
        return switch (estado) {
            case TEMPORAL -> EstadoTicket.TEMPORAL;
            case CERRADO -> EstadoTicket.CERRADO;
            case CANCELADO -> EstadoTicket.CANCELADO;
            case PAGADO -> EstadoTicket.PAGADO;
        };
    }
}