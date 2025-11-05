package com.isam.dto.producto;

import com.isam.grpc.catalogo.ProductoProto;
import com.isam.model.PoliticaRotacion;
import com.isam.model.UnidadMedida;

public record CrearProductoReq(
    String sku,
    String ean,
    String plu,
    String nombre,
    String descripcion,
    double precioVenta,
    boolean caduca,
    boolean esGranel,
    String idCategoria,
    PoliticaRotacion politicaRotacion,
    UnidadMedida unidadMedida
) {
    public record Response(
        ProductoProto producto
    ) {}
}

