package com.isam.dto.proveedor;

import jakarta.validation.constraints.NotNull;

public record ProveedorDto(

    String idProveedor,

    @NotNull(message = "El nombre del proveedor es obligatorio")
    String nombreProveedor,

    String contacto,

    String direccion,

    String telefono,

    String email

) {}