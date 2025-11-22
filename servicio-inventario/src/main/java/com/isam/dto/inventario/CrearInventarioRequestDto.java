package com.isam.dto.inventario;

import com.isam.dto.EanOrPlu;
import com.isam.model.UnidadMedida;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@EanOrPlu
public record CrearInventarioRequestDto(
    @Size(max = 50)
    @NotBlank(message = "El SKU es obligatorio")
    String sku,
    
    @Size(max = 13, message = "El EAN no puede exceder 13 caracteres")
    String ean,
    
    @Size(max = 5, message = "El PLU no puede exceder 5 caracteres")
    String plu,
    
    @NotNull(message = "La unidad de medida es obligatoria")
    UnidadMedida unidadMedida
) {
}
