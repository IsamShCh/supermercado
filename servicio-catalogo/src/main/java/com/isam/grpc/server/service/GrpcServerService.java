package com.isam.grpc.server.service;

import com.isam.dto.categoria.CrearCategoriaDto;
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
public class GrpcServerService extends CatalogoServiceGrpc.CatalogoServiceImplBase {

    //private static Log log = LogFactory.getLog(GrpcServerService.class);
    @Autowired
    private CatalogoService catalogoService;
    @Autowired
    private CatalogoMapper productoMapper;

    @Override
    public void crearProducto(CrearProductoRequest request, StreamObserver<CrearProductoRequest.Response> responseObserver) {
        System.out.println("DEBUG: gRPC request received: " + request);
        
        // Paso 1: Convertir la solicitud gRPC a DTO (responsabilidad del mapeador)
        CrearProductoDto productoDto = productoMapper.toDto(request);
        System.out.println("DEBUG: DTO created: " + productoDto);

        // Paso 2: El servicio gestiona la lógica de negocio y la creación de entidades.
        Producto productoEntityCreated = catalogoService.crearProducto(productoDto);
        System.out.println("DEBUG: Product created: " + productoEntityCreated);

        // Paso 3: Convertir la entidad de nuevo a una respuesta gRPC (responsabilidad del mapeador)
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
            CrearCategoriaDto categoriaDto = productoMapper.toDto(request);

            Categoria categoriaResp = catalogoService.crearCategoria(categoriaDto);

            CategoriaProto categoriaProtoResp = productoMapper.toProto(categoriaResp);
            CrearCategoriaRequest.Response response = CrearCategoriaRequest.Response.newBuilder()
                    .setCategoria(categoriaProtoResp)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
    }
}