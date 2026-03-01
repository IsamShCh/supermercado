package com.isam.simulador.client;

import com.isam.grpc.catalogo.*;
import com.isam.grpc.common.PoliticaRotacion;
import com.isam.grpc.common.UnidadMedida;
import com.isam.grpc.common.PaginationRequest;
import com.isam.simulador.model.EmpleadoSimulado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Cliente gRPC para el servicio de catálogo.
 * Maneja la creación y consulta de productos y categorías.
 */
@Component
@Slf4j
public class CatalogoClient {

    private final GrpcClientWithAuth grpcClient;

    public CatalogoClient(GrpcClientWithAuth grpcClient) {
        this.grpcClient = grpcClient;
    }

    /**
     * Crea una nueva categoría en el catálogo.
     */
    public CategoriaProto crearCategoria(String nombre, String descripcion, EmpleadoSimulado empleado) {
        try {
            CrearCategoriaRequest request = CrearCategoriaRequest.newBuilder()
                .setNombreCategoria(nombre)
                .setDescripcion(descripcion)
                .build();

            CatalogoServiceGrpc.CatalogoServiceBlockingStub stub = 
                grpcClient.getCatalogoStubConToken(empleado.getToken());

            CrearCategoriaRequest.Response response = stub.crearCategoria(request);
            CategoriaProto categoria = response.getCategoria();

            log.info("📂 {} creó categoría: {} (ID: {})", 
                empleado.getNombre(), nombre, categoria.getIdCategoria());

            return categoria;
        } catch (Exception e) {
            log.error("❌ Error creando categoría '{}': {}", nombre, e.getMessage(), e);
            throw new RuntimeException("Error creando categoría", e);
        }
    }

    /**
     * Crea un nuevo producto en el catálogo.
     */
    public ProductoProto crearProducto(String sku, String nombre, String precio, long idCategoria, 
                                  boolean esGranel, boolean caduca, EmpleadoSimulado empleado) {
        try {
            CrearProductoRequest request = CrearProductoRequest.newBuilder()
                .setSku(sku)
                .setNombre(nombre)
                .setPrecioVenta(precio)
                .setIdCategoria(idCategoria)
                .setPoliticaRotacion(PoliticaRotacion.FIFO)
                .setUnidadMedida(UnidadMedida.UNIDAD)
                .setEsGranel(esGranel)
                .setCaduca(caduca)
                // Generar EAN aleatorio para productos sin EAN real
                .setEan(esGranel ? "" : generarEanAleatorio())
                .build();

            CatalogoServiceGrpc.CatalogoServiceBlockingStub stub = 
                grpcClient.getCatalogoStubConToken(empleado.getToken());

            CrearProductoRequest.Response response = stub.crearProducto(request);
            ProductoProto producto = response.getProducto();

            log.info("📦 {} creó producto: {} - {} (SKU: {})", 
                empleado.getNombre(), sku, nombre, producto.getSku());

            return producto;
        } catch (Exception e) {
            log.error("❌ Error creando producto '{}': {}", sku, e.getMessage(), e);
            throw new RuntimeException("Error creando producto", e);
        }
    }

    /**
     * Consulta un producto por su SKU.
     */
    public ProductoProto consultarProducto(String sku, EmpleadoSimulado empleado) {
        try {
            ConsultarProductoRequest request = ConsultarProductoRequest.newBuilder()
                .setSku(sku)
                .build();

            CatalogoServiceGrpc.CatalogoServiceBlockingStub stub = 
                grpcClient.getCatalogoStubConToken(empleado.getToken());

            ConsultarProductoRequest.Response response = stub.consultarProducto(request);
            ProductoProto producto = response.getProducto();

            log.debug("🔍 {} consultó producto: {} - {}", 
                empleado.getNombre(), sku, producto.getNombre());

            return producto;
        } catch (Exception e) {
            log.error("❌ Error consultando producto '{}': {}", sku, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lista todas las categorías disponibles.
     */
    public ListarCategoriasRequest.Response listarCategorias(EmpleadoSimulado empleado) {
        try {
            ListarCategoriasRequest request = ListarCategoriasRequest.newBuilder().build();

            CatalogoServiceGrpc.CatalogoServiceBlockingStub stub = 
                grpcClient.getCatalogoStubConToken(empleado.getToken());

            ListarCategoriasRequest.Response response = stub.listarCategorias(request);

            log.debug("📋 {} listó {} categorías", 
                empleado.getNombre(), response.getCategoriasCount());

            return response;
        } catch (Exception e) {
            log.error("❌ Error listando categorías: {}", e.getMessage(), e);
            throw new RuntimeException("Error listando categorías", e);
        }
    }

    /**
     * Lista todos los productos con paginación.
     */
    public ListarProductosRequest.Response listarProductos(int pagina, int tamañoPagina, EmpleadoSimulado empleado) {
        try {
            PaginationRequest paginacion = PaginationRequest.newBuilder()
                .setPage(pagina)
                .setPageSize(tamañoPagina)
                .build();

            ListarProductosRequest request = ListarProductosRequest.newBuilder()
                .setPaginacion(paginacion)
                .build();

            CatalogoServiceGrpc.CatalogoServiceBlockingStub stub = 
                grpcClient.getCatalogoStubConToken(empleado.getToken());

            ListarProductosRequest.Response response = stub.listarProductos(request);

            log.debug("📋 {} listó productos (página {}): {} productos", 
                empleado.getNombre(), pagina, response.getListaProductos().getProductosCount());

            return response;
        } catch (Exception e) {
            log.error("❌ Error listando productos: {}", e.getMessage(), e);
            throw new RuntimeException("Error listando productos", e);
        }
    }

    /**
     * Genera un EAN aleatorio de 13 dígitos para productos simulados.
     */
    private String generarEanAleatorio() {
        // EAN-13: 13 dígitos, el último es dígito de control
        StringBuilder ean = new StringBuilder();
        
        // Primeros 12 dígitos aleatorios
        for (int i = 0; i < 12; i++) {
            ean.append((int) (Math.random() * 10));
        }
        
        // Calcular dígito de control (simplificado)
        int suma = 0;
        for (int i = 0; i < 12; i++) {
            int digito = Character.getNumericValue(ean.charAt(i));
            suma += (i % 2 == 0) ? digito : digito * 3;
        }
        int digitoControl = (10 - (suma % 10)) % 10;
        ean.append(digitoControl);
        
        return ean.toString();
    }
}