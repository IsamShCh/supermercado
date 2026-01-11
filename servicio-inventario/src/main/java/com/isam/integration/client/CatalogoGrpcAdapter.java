package com.isam.integration.client;

import com.isam.dto.producto.ConsultarProductoDto;
import com.isam.service.ports.IProveedorCatalogo;
import lombok.RequiredArgsConstructor;
import com.isam.integration.client.CatalogoGrpcClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogoGrpcAdapter implements IProveedorCatalogo {

    private final CatalogoGrpcClient catalogoGrpcClient;

    @Override
    public ConsultarProductoDto consultarProducto(String sku) {
        return catalogoGrpcClient.consultarProducto(sku);
    }
}
