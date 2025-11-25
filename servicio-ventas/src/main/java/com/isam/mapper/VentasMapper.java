package com.isam.mapper;

import com.isam.dto.CrearNuevoTicketResponseDto;
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
            .build();
    }
}