package com.isam.service.ports;

import com.isam.dto.CatalogoClient.ProductoDto;

public interface IProveedorCatalogo {
    String traducirCodigoBarrasASku(String codigoBarras);
    ProductoDto consultarProducto(String sku);
}
