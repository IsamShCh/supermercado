package com.isam.grpc.server;

import com.isam.dto.producto.CrearProductoDto;
import com.isam.grpc.catalogo.*;
import com.isam.mapper.CatalogoMapper;
import com.isam.model.Categoria;
import com.isam.model.Producto;
import com.isam.service.CatalogoService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class GrpcServerService extends CatalogoServiceGrpc.CatalogoServiceImplBase {

    //private static Log log = LogFactory.getLog(GrpcServerService.class);
    @Autowired
    private CatalogoService catalogoService;
    @Autowired
    private CatalogoMapper productoMapper;

    @Override
    public void crearProducto(CrearProductoRequest request, StreamObserver<CrearProductoRequest.Response> responseObserver) {
        System.out.println("DEBUG: gRPC request received: " + request);
        
        // Step 1: Convert gRPC request to DTO (mapper responsibility)
        CrearProductoDto productoDto = productoMapper.toDto(request);
        System.out.println("DEBUG: DTO created: " + productoDto);

        // Step 2: Service handles business logic and entity creation
        Producto productoEntityCreated = catalogoService.crearProducto(productoDto);
        System.out.println("DEBUG: Product created: " + productoEntityCreated);

        // Step 3: Convert entity back to gRPC response (mapper responsibility)
        com.isam.grpc.catalogo.ProductoProto productoResGrpc = productoMapper.toProto(productoEntityCreated);

        com.isam.grpc.catalogo.CrearProductoRequest.Response crearProductoRespuesta = com.isam.grpc.catalogo.CrearProductoRequest.Response.newBuilder()
                        .setProducto(productoResGrpc).build();

        responseObserver.onNext(crearProductoRespuesta);
        responseObserver.onCompleted();
    }


    @Override
    public void consultarProducto(ConsultarProductoRequest request, StreamObserver<ConsultarProductoRequest.Response> responseObserver) {

        Producto productoEntity = catalogoService.consultarProducto(request.getSku());

        ProductoProto productoProto = productoMapper.toProto(productoEntity);

        responseObserver.onNext(
                ConsultarProductoRequest.Response.newBuilder()
                        .setProducto(productoProto).build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void crearCategoria(CrearCategoriaRequest request, StreamObserver<CrearCategoriaRequest.Response> responseObserver) {
            Categoria categoriaEntity = productoMapper.toEntity(request);

            Categoria categoriaResp = catalogoService.crearCategoria(categoriaEntity);

            CategoriaProto categoriaProtoResp = productoMapper.toProto(categoriaResp);
            CrearCategoriaRequest.Response response = CrearCategoriaRequest.Response.newBuilder()
                    .setCategoria(categoriaProtoResp)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
    }
}