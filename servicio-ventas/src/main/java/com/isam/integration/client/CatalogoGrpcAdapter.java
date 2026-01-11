package com.isam.integration.client;

import com.isam.dto.CatalogoClient.ProductoDto;
import com.isam.service.ports.IProveedorCatalogo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogoGrpcAdapter implements IProveedorCatalogo {

    private final CatalogoGrpcClient catalogoGrpcClient;

    @Override
    public String traducirCodigoBarrasASku(String codigoBarras) {
        return catalogoGrpcClient.traducirCodigoBarrasASku(codigoBarras);
    }

    @Override
    public ProductoDto consultarProducto(String sku) {
        return catalogoGrpcClient.consultarProducto(sku);
    }
}
