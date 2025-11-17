package com.isam.dto.producto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

import com.isam.dto.comun.PaginacionResponseDto;
import com.isam.dto.oferta.OfertaDto;

public record ListaProductosDto(

    @NotNull(message = "La lista de productos no puede ser nula")
    @Valid
    List<DetallesProductoCompletoDto> productos,
    
    @Valid
    PaginacionResponseDto paginacion

) {

    public record DetallesProductoCompletoDto(
        @NotNull(message = "El producto no puede ser nulo")
        @Valid
        ProductoDto producto,
        
        @Valid
        List<OfertaDto> ofertas
    ) {}
    


}
