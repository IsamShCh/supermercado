package com.isam.mapper;

import com.isam.dto.rol.CrearRolRequestDto;
import com.isam.dto.rol.CrearRolResponseDto;
import com.isam.dto.rol.RolDto;
import com.isam.grpc.usuarios.CrearRolRequest;
import com.isam.grpc.usuarios.RolProto;
import com.isam.model.Rol;
import org.springframework.stereotype.Component;

@Component
public class UsuariosMapper {

    /**
     * Convierte gRPC CrearRolRequest a DTO.
     */
    public CrearRolRequestDto toDto(CrearRolRequest request) {
        return new CrearRolRequestDto(
            request.getNombreRol(),
            request.hasDescripcion() ? request.getDescripcion() : null
        );
    }

    /**
     * Convierte entidad Rol a RolDto.
     */
    public RolDto toDto(Rol rol) {
        return new RolDto(
            rol.getIdRol(),
            rol.getNombreRol(),
            rol.getDescripcionRol()
        );
    }

    /**
     * Convierte RolDto a proto RolProto.
     */
    public RolProto toProto(RolDto rolDto) {
        RolProto.Builder builder = RolProto.newBuilder()
            .setIdRol(rolDto.idRol())
            .setNombreRol(rolDto.nombreRol());
        
        if (rolDto.descripcion() != null) {
            builder.setDescripcion(rolDto.descripcion());
        }
        
        return builder.build();
    }

    /**
     * Convierte CrearRolResponseDto a proto CrearRolRequestResponse.
     */
    public CrearRolRequest.Response toProto(CrearRolResponseDto response) {
        return CrearRolRequest.Response.newBuilder()
            .setRol(toProto(response.rol()))
            .build();
    }
}