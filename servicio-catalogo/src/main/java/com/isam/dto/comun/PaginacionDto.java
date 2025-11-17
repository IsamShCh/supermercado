package com.isam.dto.comun;

import jakarta.validation.constraints.*;
import java.util.List;
    
public record PaginacionDto(
    @Positive(message = "El número de página debe ser positivo")
    Integer page,
    
    @Positive(message = "El tamaño de página debe ser positivo")
    @Max(value = 100, message = "El tamaño de página no puede exceder 100") //NOTE - Esto es un poco arbitrario, reflexionarlo.
    Integer pageSize
) {}