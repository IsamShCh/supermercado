package com.isam.dto.producto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

import com.isam.dto.comun.PaginacionDto;

public record BuscarProductosDto(
    @Valid
    CriteriosBusquedaDto criterios,

    @Valid
    PaginacionDto paginacion
) {

    public record CriteriosBusquedaDto(
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
        String nombre,

        @Positive(message = "El ID de categoría debe ser positivo")
        Long idCategoria,

        @DecimalMin(value = "0.0", inclusive = true, message = "El precio mínimo no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
        BigDecimal precioMin,

        @DecimalMin(value = "0.0", inclusive = true, message = "El precio máximo no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
        BigDecimal precioMax,

        Boolean esGranel,
        
        @Size(max = 20, message = "No puede haber más de 20 etiquetas")
        List<@NotBlank(message = "Las etiquetas no pueden estar vacías") String> etiquetas
    ) {}
    
}