package com.isam.dto.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AsignarEtiquetasDto(
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku,

    @NotNull(message = "Las etiquetas no pueden ser nulas")
    @Size(max = 10, message = "No puede haber más de 10 etiquetas")
    List<@NotBlank(message = "Las etiquetas no pueden estar vacías") @Size(max = 50, message = "Cada etiqueta no puede exceder 50 caracteres") String> etiquetas
) {}