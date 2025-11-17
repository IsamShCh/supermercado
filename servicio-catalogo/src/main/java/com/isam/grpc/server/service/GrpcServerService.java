package com.isam.grpc.server.service;

import com.isam.dto.categoria.CrearCategoriaDto;
import com.isam.dto.producto.BuscarProductosDto;
import com.isam.dto.producto.ConsultarProductoDto;
import com.isam.dto.producto.CrearProductoDto;
import com.isam.dto.producto.ListaProductosDto;
import com.isam.grpc.catalogo.*;
import com.isam.mapper.CatalogoMapper;
import com.isam.model.Categoria;
import com.isam.model.Producto;
import com.isam.service.CatalogoService;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


@Service
public class GrpcServerService extends CatalogoServiceGrpc.CatalogoServiceImplBase {

    //private static Log log = LogFactory.getLog(GrpcServerService.class);
    @Autowired
    private CatalogoService catalogoService;
    @Autowired
    private CatalogoMapper productoMapper;
    @Autowired
    private Validator validator;

    @Override
    public void crearProducto(CrearProductoRequest request, StreamObserver<CrearProductoRequest.Response> responseObserver) {
        System.out.println("DEBUG: gRPC request received: " + request);
        System.out.println(">>" + request.hasEan());
        System.out.println(">>" + request.hasPlu());

        // Paso 1: Convertir la solicitud gRPC a DTO (responsabilidad del mapeador)
        CrearProductoDto productoDto = productoMapper.toDto(request);
        System.out.println("DEBUG: DTO created: " + productoDto);

        // Validamos
        Set<ConstraintViolation<CrearProductoDto>> violations = validator.validate(productoDto);
        if (!violations.isEmpty()) {
            String errores = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(errores)
                    .asRuntimeException()
            );
            return;
        }
        

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
    @Transactional
    public void consultarProducto(ConsultarProductoRequest request, StreamObserver<ConsultarProductoRequest.Response> responseObserver) {

        ConsultarProductoDto consultarProductoDto = productoMapper.toDto(request);
        
        // Validamos
        Set<ConstraintViolation<ConsultarProductoDto>> violations = validator.validate(consultarProductoDto);
        if (!violations.isEmpty()) {
            String errores = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(errores)
                    .asRuntimeException()
            );
            return;
        }

        Producto productoEntity = catalogoService.consultarProducto(consultarProductoDto);

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

        // Validamos
        Set<ConstraintViolation<CrearCategoriaDto>> violations = validator.validate(categoriaDto);
        if (!violations.isEmpty()) {
            String errores = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(errores)
                    .asRuntimeException()
            );
            return;
        }

        Categoria categoriaResp = catalogoService.crearCategoria(categoriaDto);

        CategoriaProto categoriaProtoResp = productoMapper.toProto(categoriaResp);
        CrearCategoriaRequest.Response response = CrearCategoriaRequest.Response.newBuilder()
                .setCategoria(categoriaProtoResp)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional(readOnly = true)
    public void buscarProductos(BuscarProductosRequest request, StreamObserver<BuscarProductosRequest.Response> responseObserver) {
        // Convert gRPC request to DTO
        BuscarProductosDto buscarProductosDto = productoMapper.toDto(request);
        
        // Validate
        Set<ConstraintViolation<BuscarProductosDto>> violations = validator.validate(buscarProductosDto);
        if (!violations.isEmpty()) {
            String errores = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(errores)
                    .asRuntimeException()
            );
            return;
        }
        
        // Call service layer
        ListaProductosDto listaProductosDto = catalogoService.buscarProductos(buscarProductosDto);
        
        // Convert to proto
        ListaProductos listaProductosProto = productoMapper.toProto(listaProductosDto);
        
        // Build response
        BuscarProductosRequest.Response response = BuscarProductosRequest.Response.newBuilder()
            .setListaProductos(listaProductosProto)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Utils
    private boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}