package com.isam.dto.inventario;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record StockFisicoLoteDto(
    @NotBlank(message = "El ID del lote es obligatorio")
    String idLote,

    @NotNull(message = "El stock físico de almacén es obligatorio")
    @PositiveOrZero(message = "El stock físico de almacén debe ser mayor o igual a cero")
    @Digits(integer = 10, fraction = 3, message = "El stock físico debe tener máximo 10 dígitos enteros y 3 decimales")
    BigDecimal stockFisicoAlmacen
) {}