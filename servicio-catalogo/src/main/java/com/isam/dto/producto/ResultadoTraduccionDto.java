package com.isam.dto.producto;

public record ResultadoTraduccionDto(
    String codigoEntrada,
    String codigoSalida,
    TipoIdentificador tipoEntrada,
    TipoIdentificador tipoSalida
) {
    public enum TipoIdentificador {
        SKU,
        EAN,
        PLU
    }
}