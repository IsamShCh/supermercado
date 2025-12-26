package com.isam.infrastructure.adapter.out.grpc;

import com.isam.dto.producto.ConsultarProductoDto;
import com.isam.grpc.client.CatalogoGrpcClient;
import com.isam.service.ports.IProveedorCatalogo;
import lombok.RequiredArgsConstructor;
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
