package com.isam.service.ports;

import com.isam.dto.producto.ConsultarProductoDto;

public interface IProveedorCatalogo {
    ConsultarProductoDto consultarProducto(String sku);
}
