package com.isam.grpc.server.service;

import com.isam.dto.categoria.CrearCategoriaDto;
import com.isam.dto.producto.BuscarProductosDto;
import com.isam.dto.producto.ConsultarProductoDto;
import com.isam.dto.producto.CrearProductoDto;
import com.isam.dto.producto.DescatalogarProductoDto;
import com.isam.dto.oferta.CrearOfertaDto;
import com.isam.dto.producto.ListaProductosDto;
import com.isam.dto.producto.ListarProductosRequestDto;
import com.isam.dto.producto.RecatalogarProductoDto;
import com.isam.grpc.catalogo.*;
import com.isam.mapper.CatalogoMapper;
import com.isam.model.Oferta;
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

    /**
     * Lista todos los productos con paginación opcional.
     */
    @Override
    @Transactional(readOnly = true)
    public void listarProductos(ListarProductosRequest request, StreamObserver<ListarProductosRequest.Response> responseObserver) {
        // Convertir la solicitud gRPC a DTO
        ListarProductosRequestDto dto = productoMapper.toDto(request);
        
        // Validar
        Set<ConstraintViolation<ListarProductosRequestDto>> violations = validator.validate(dto);
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
        
        // Llamar al servicio
        ListaProductosDto listaProductosDto = catalogoService.listarProductos(dto);
        
        // Convertir a proto
        ListaProductos listaProductosProto = productoMapper.toProto(listaProductosDto);
        
        // Construir respuesta
        ListarProductosRequest.Response response = ListarProductosRequest.Response.newBuilder()
            .setListaProductos(listaProductosProto)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Descataloga un producto cambiando su estado a DESCATALOGADO.
     */
    @Override
    @Transactional
    public void descatalogarProducto(DescatalogarProductoRequest request, StreamObserver<DescatalogarProductoRequest.Response> responseObserver) {
        // Convertir la solicitud gRPC a DTO
        DescatalogarProductoDto dto = productoMapper.toDto(request);
        
        // Validar
        Set<ConstraintViolation<DescatalogarProductoDto>> violations = validator.validate(dto);
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
        
        // Llamar al servicio
        Producto productoDescatalogado = catalogoService.descatalogarProducto(dto);
        
        // Convertir a proto
        ProductoProto productoProto = productoMapper.toProto(productoDescatalogado);
        
        // Construir respuesta
        DescatalogarProductoRequest.Response response = DescatalogarProductoRequest.Response.newBuilder()
            .setProducto(productoProto)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Recataloga un producto cambiando su estado a ACTIVO.
     */
    @Override
    @Transactional
    public void recatalogarProducto(RecatalogarProductoRequest request, StreamObserver<RecatalogarProductoRequest.Response> responseObserver) {
        // Convertir la solicitud gRPC a DTO
        RecatalogarProductoDto dto = productoMapper.toDto(request);
        
        // Validar
        Set<ConstraintViolation<RecatalogarProductoDto>> violations = validator.validate(dto);
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
        
        // Llamar al servicio
        Producto productoRecatalogado = catalogoService.recatalogarProducto(dto);
        
        // Convertir a proto
        ProductoProto productoProto = productoMapper.toProto(productoRecatalogado);
        
        // Construir respuesta
        RecatalogarProductoRequest.Response response = RecatalogarProductoRequest.Response.newBuilder()
            .setProducto(productoProto)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Crea una oferta para un producto.
     */
    @Override
    @Transactional
    public void crearOferta(CrearOfertaRequest request, StreamObserver<CrearOfertaRequest.Response> responseObserver) {
        // Convertir la solicitud gRPC a DTO
        CrearOfertaDto dto = productoMapper.toDto(request);
        
        // Validar
        Set<ConstraintViolation<CrearOfertaDto>> violations = validator.validate(dto);
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
        
        // Llamar al servicio
        Oferta ofertaCreada = catalogoService.crearOferta(dto);
        
        // Convertir a proto
        OfertaProto ofertaProto = productoMapper.toProto(ofertaCreada);
        
        // Construir respuesta
        CrearOfertaRequest.Response response = CrearOfertaRequest.Response.newBuilder()
            .setOferta(ofertaProto)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Utils
    private boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}