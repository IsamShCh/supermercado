package com.isam.dto.inventario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ContabilizacionPorLotesDto(
    @NotEmpty(message = "La lista de lotes no puede estar vacía")
    @Valid
    List<StockFisicoLoteDto> lotes
) {}