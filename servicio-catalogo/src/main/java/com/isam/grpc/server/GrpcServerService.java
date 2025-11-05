package com.isam.grpc.server;


import com.isam.grpc.catalogo.*;
import com.isam.grpc.interceptor.ExceptionUtils;
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
    public void crearProducto(CrearProductoReq request, StreamObserver<CrearProductoReq.Response> responseObserver) {
        Producto productoEntity = productoMapper.toEntity(request);
        System.out.println("\n\n\n"+productoEntity.toString()+"\n\n\n");

        Producto productoEntityResEntidad = catalogoService.crearProducto(productoEntity);

        com.isam.grpc.catalogo.ProductoProto productoResGrpc = productoMapper.toProto(productoEntityResEntidad);

        com.isam.grpc.catalogo.CrearProductoReq.Response crearProductoRespuesta = com.isam.grpc.catalogo.CrearProductoReq.Response.newBuilder()
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
                        .setSuccess(productoProto).build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void crearCategoria(CrearCategoriaRequest request, StreamObserver<CrearCategoriaRequest.Response> responseObserver) {

//        try {
            Categoria categoriaEntity = productoMapper.toEntity(request);

            Categoria categoriaResp = catalogoService.crearCategoria(categoriaEntity);

            CategoriaProto categoriaProtoResp = productoMapper.toProto(categoriaResp);

            CrearCategoriaRequest.Response response = CrearCategoriaRequest.Response.newBuilder()
                    .setCategoria(categoriaProtoResp)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
//        } catch (RuntimeException exception){
//            throw ExceptionUtils.observarError(responseObserver, exception, CrearCategoriaRequest.Response.getDefaultInstance());
//        }
    }
}