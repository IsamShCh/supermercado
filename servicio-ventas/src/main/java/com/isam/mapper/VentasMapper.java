package com.isam.mapper;

import com.isam.dto.AnadirProductoTicketRequestDto;
import com.isam.dto.AnadirProductoTicketResponseDto;
import com.isam.dto.CrearNuevoTicketResponseDto;
import com.isam.grpc.ventas.AnadirProductoTicketRequest;
import com.isam.grpc.ventas.CrearNuevoTicketRequest;
import org.springframework.stereotype.Component;

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
            .setCantidad(dto.cantidad())
            .setPrecioUnitario(dto.precioUnitario())
            .setSubtotal(dto.subtotal())
            .setSubtotalTicketActual(dto.subtotalTicketActual())
            .build();
    }
}