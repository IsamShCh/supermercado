package com.isam.dto.proveedor;

import jakarta.validation.constraints.*;

public record AgregarProveedorRequestDto(

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 200, message = "El nombre del proveedor no puede exceder 200 caracteres")
    String nombreProveedor,

    @Size(max = 100, message = "El contacto no puede exceder 100 caracteres")
    String contacto,

    @Size(max = 300, message = "La dirección no puede exceder 300 caracteres")
    String direccion,

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    String telefono,

    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Email(message = "El email debe tener un formato válido")
    String email

) {}