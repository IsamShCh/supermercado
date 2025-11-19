package com.isam.dto.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ValidCodigoSize
public record TraducirIdentificadorRequestDto(
    @NotBlank(message = "El código es obligatorio")
    String codigo,

    @NotNull(message = "El tipo de identificador es obligatorio")
    TipoIdentificador tipoIdentificador
) {
    public enum TipoIdentificador {
        SKU,
        EAN,
        PLU
    }
}