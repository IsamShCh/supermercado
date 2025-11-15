package com.isam.dto.producto;

import com.isam.model.PoliticaRotacion;
import com.isam.model.UnidadMedida;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.*;

@EanOrPlu
public record CrearProductoDto(
    
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku,
    
    // EAN y PLU son opcionales individualmente, pero al menos uno debe estar presente
    // Esta validación se hace a nivel de clase (ver más abajo)
    @Size(max = 13, message = "El EAN no puede exceder 13 caracteres")
    String ean,
    
    @Size(max = 5, message = "El PLU no puede exceder 5 caracteres")
    String plu,
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    String nombre,
    
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    String descripcion,
    
    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    BigDecimal precioVenta,
    
    @NotNull(message = "Debe indicar si el producto caduca")
    Boolean caduca,
    
    @NotNull(message = "Debe indicar si el producto es a granel")
    Boolean esGranel,
    
    @NotNull(message = "La categoría es obligatoria")
    @Positive(message = "El ID de categoría debe ser positivo")
    Long idCategoria,
    
    @NotNull(message = "La política de rotación es obligatoria")
    PoliticaRotacion politicaRotacion,
    
    @NotNull(message = "La unidad de medida es obligatoria")
    UnidadMedida unidadMedida,
    
    @Size(max = 20, message = "No puede haber más de 20 etiquetas")
    List<@NotBlank(message = "Las etiquetas no pueden estar vacías") String> etiquetas
) {}