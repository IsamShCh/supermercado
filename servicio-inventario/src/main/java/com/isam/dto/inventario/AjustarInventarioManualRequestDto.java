package com.isam.dto.inventario;

import com.isam.dto.EanOrPlu;
import com.isam.model.TipoAjusteInventario;
import com.isam.model.UnidadMedida;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AjustarInventarioManualRequestDto(
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku,
    
    @NotNull(message = "La cantidad de ajuste es obligatoria")
    @Digits(integer = 10, fraction = 3, message = "La cantidad debe tener máximo 10 dígitos enteros y 3 decimales")
    BigDecimal cantidadAjuste,
    
    @NotNull(message = "El tipo de ajuste es obligatorio")
    TipoAjusteInventario tipoAjuste,
    
    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 200, message = "El motivo no puede exceder 200 caracteres")
    String motivoDetallado,
    
    @NotNull(message = "La ubicación del ajuste es obligatoria")
    UbicacionAjusteDto ubicacionAjuste
) {}