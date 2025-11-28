package com.isam.mapper;

import com.isam.dto.AnadirProductoTicketRequestDto;
import com.isam.dto.AnadirProductoTicketResponseDto;
import com.isam.dto.CerrarTicketRequestDto;
import com.isam.dto.CerrarTicketResponseDto;
import com.isam.dto.CrearNuevoTicketResponseDto;
import com.isam.dto.LineaVentaDto;
import com.isam.dto.ProcesarPagoRequestDto;
import com.isam.dto.ProcesarPagoResponseDto;
import com.isam.grpc.ventas.AnadirProductoTicketRequest;
import com.isam.grpc.ventas.CerrarTicketRequest;
import com.isam.grpc.ventas.CrearNuevoTicketRequest;
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
            .setNumeroTicket(dto.numeroTicket())
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
        BigDecimal montoRecibido = new BigDecimal(request.getMontoRecibido());
        
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
}