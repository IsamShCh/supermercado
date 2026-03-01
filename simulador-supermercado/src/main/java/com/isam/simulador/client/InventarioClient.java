package com.isam.simulador.client;

import com.isam.grpc.inventario.*;
import com.isam.grpc.inventario.TipoAjuste;
import com.isam.grpc.common.UnidadMedida;
import com.isam.simulador.model.EmpleadoSimulado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Cliente gRPC para el servicio de inventario.
 * Maneja operaciones de stock, lotes y movimientos.
 */
@Component
@Slf4j
public class InventarioClient {

    private final GrpcClientWithAuth grpcClient;

    public InventarioClient(GrpcClientWithAuth grpcClient) {
        this.grpcClient = grpcClient;
    }

    /**
     * Crea un inventario para un producto.
     */
    public void crearInventario(String sku, String ean, String plu, UnidadMedida unidadMedida, EmpleadoSimulado empleado) {
        try {
            CrearInventarioRequest.Builder builder = CrearInventarioRequest.newBuilder()
                .setSku(sku)
                .setUnidadMedida(unidadMedida);

            if (ean != null && !ean.isEmpty()) {
                builder.setEan(ean);
            } else if (plu != null && !plu.isEmpty()) {
                builder.setPlu(plu);
            }

            InventarioServiceGrpc.InventarioServiceBlockingStub stub = 
                grpcClient.getInventarioStubConToken(empleado.getToken());

            stub.crearInventario(builder.build());

            log.info("📦 {} creó inventario para SKU: {}", empleado.getNombre(), sku);
        } catch (Exception e) {
            log.error("❌ Error creando inventario para SKU '{}': {}", sku, e.getMessage(), e);
            throw new RuntimeException("Error creando inventario", e);
        }
    }

    /**
     * Registra nuevas existencias en almacén (crea un lote).
     */
    public LoteProto registrarNuevasExistencias(String sku, String cantidad, String numeroLote, 
                                               String idProveedor, UnidadMedida unidadMedida, 
                                               String fechaCaducidad, EmpleadoSimulado empleado) {
        try {
            RegistrarNuevasExistenciasRequest.Builder requestBuilder = 
                RegistrarNuevasExistenciasRequest.newBuilder()
                    .setSku(sku)
                    .setCantidad(cantidad)
                    .setNumeroLote(numeroLote)
                    .setIdProveedor(idProveedor)
                    .setUnidadMedida(unidadMedida);

            // Añadir fecha de caducidad solo si aplica
            if (fechaCaducidad != null && !fechaCaducidad.isEmpty()) {
                requestBuilder.setFechaCaducidad(fechaCaducidad);
            }

            RegistrarNuevasExistenciasRequest request = requestBuilder.build();

            InventarioServiceGrpc.InventarioServiceBlockingStub stub = 
                grpcClient.getInventarioStubConToken(empleado.getToken());

            RegistrarNuevasExistenciasRequest.Response response = stub.registrarNuevasExistencias(request);

            log.info("📥 {} registró existencias - SKU: {}, Lote: {}, Cantidad: {}", 
                empleado.getNombre(), sku, numeroLote, cantidad);

            return response.getLote();
        } catch (Exception e) {
            log.error("❌ Error registrando existencias para SKU '{}': {}", sku, e.getMessage(), e);
            throw new RuntimeException("Error registrando existencias", e);
        }
    }

    /**
     * Mueve stock de almacén a estantería.
     */
    public void moverStockEstanteria(String sku, String idLote, String cantidad, 
                                  UnidadMedida unidadMedida, EmpleadoSimulado empleado) {
        try {
            MoverStockEstanteriaRequest request = MoverStockEstanteriaRequest.newBuilder()
                .setSku(sku)
                .setIdLote(idLote)
                .setCantidadTransladar(cantidad)
                .setUnidadMedida(unidadMedida)
                .build();

            InventarioServiceGrpc.InventarioServiceBlockingStub stub = 
                grpcClient.getInventarioStubConToken(empleado.getToken());

            stub.moverStockEstanteria(request);

            log.info("📦 {} movió stock a estantería - SKU: {}, Lote: {}, Cantidad: {}", 
                empleado.getNombre(), sku, idLote, cantidad);
        } catch (Exception e) {
            log.error("❌ Error moviendo stock a estantería para SKU '{}': {}", sku, e.getMessage(), e);
            throw new RuntimeException("Error moviendo stock a estantería", e);
        }
    }

    /**
     * Registra un robo de producto.
     * Si idLote es null, el backend seleccionará el lote automáticamente.
     */
    public void registrarRobo(String sku, String cantidad, String idLote, EmpleadoSimulado empleado) {
        try {
            AjustarEstanteria.Builder estanteriaBuilder = AjustarEstanteria.newBuilder();
            
            // Solo establecemos el ID si se proporciona explícitamente
            if (idLote != null && !idLote.isEmpty()) {
                estanteriaBuilder.setIdLote(idLote);
            }

            AjustarInventarioManualRequest request = AjustarInventarioManualRequest.newBuilder()
                .setSku(sku)
                .setCantidadAjuste(cantidad) // Debe ser negativo
                .setTipoAjuste(TipoAjuste.ROBO)
                .setMotivoDetallado("Robo simulado registrado por: " + empleado.getNombre())
                .setAjustarEstanteria(estanteriaBuilder.build())
                .build();

            InventarioServiceGrpc.InventarioServiceBlockingStub stub = 
                grpcClient.getInventarioStubConToken(empleado.getToken());

            stub.ajustarInventarioManual(request);

            log.info("🚨 {} registró robo - SKU: {}, Cantidad: {}, Lote: {}", 
                empleado.getNombre(), sku, cantidad, (idLote != null ? idLote : "AUTO (FIFO)"));
        } catch (Exception e) {
            log.error("❌ Error registrando robo para SKU '{}': {}", sku, e.getMessage(), e);
            // No lanzamos RuntimeException para no detener el hilo de eventos aleatorios
        }
    }

    /**
     * Registra una merma de producto.
     * Si idLote es null, el backend seleccionará el lote automáticamente.
     */
    public void registrarMerma(String sku, String cantidad, String idLote, EmpleadoSimulado empleado) {
        try {
            AjustarEstanteria.Builder estanteriaBuilder = AjustarEstanteria.newBuilder();
            
            // Solo establecemos el ID si se proporciona explícitamente
            if (idLote != null && !idLote.isEmpty()) {
                estanteriaBuilder.setIdLote(idLote);
            }

            AjustarInventarioManualRequest request = AjustarInventarioManualRequest.newBuilder()
                .setSku(sku)
                .setCantidadAjuste(cantidad) // Debe ser negativo
                .setTipoAjuste(TipoAjuste.AJUSTE_MERMA)
                .setMotivoDetallado("Merma simulada detectada por: " + empleado.getNombre())
                .setAjustarEstanteria(estanteriaBuilder.build())
                .build();

            InventarioServiceGrpc.InventarioServiceBlockingStub stub = 
                grpcClient.getInventarioStubConToken(empleado.getToken());

            stub.ajustarInventarioManual(request);

            log.info("⚠️ {} registró merma - SKU: {}, Cantidad: {}, Lote: {}", 
                empleado.getNombre(), sku, cantidad, (idLote != null ? idLote : "AUTO (FIFO)"));
        } catch (Exception e) {
            log.error("❌ Error registrando merma para SKU '{}': {}", sku, e.getMessage(), e);
        }
    }

    /**
     * Consulta el inventario de un producto.
     */
    public ConsultarInventarioRequest.Response consultarInventario(String sku, EmpleadoSimulado empleado) {
        try {
            ConsultarInventarioRequest request = ConsultarInventarioRequest.newBuilder()
                .setSku(sku)
                .build();

            InventarioServiceGrpc.InventarioServiceBlockingStub stub = 
                grpcClient.getInventarioStubConToken(empleado.getToken());

            return stub.consultarInventario(request);
        } catch (Exception e) {
            log.error("❌ Error consultando inventario para SKU '{}': {}", sku, e.getMessage(), e);
            throw new RuntimeException("Error consultando inventario", e);
        }
    }

    /**
     * Agrega un proveedor al sistema.
     */
    public ProveedorProto agregarProveedor(String nombreProveedor, String contacto, String direccion, 
                                       String telefono, String email, EmpleadoSimulado empleado) {
        try {
            AgregarProveedorRequest request = AgregarProveedorRequest.newBuilder()
                .setNombreProveedor(nombreProveedor)
                .setContacto(contacto)
                .setDireccion(direccion)
                .setTelefono(telefono)
                .setEmail(email)
                .build();

            InventarioServiceGrpc.InventarioServiceBlockingStub stub = 
                grpcClient.getInventarioStubConToken(empleado.getToken());

            AgregarProveedorRequest.Response response = stub.agregarProveedor(request);

            log.info("🏢 {} agregó proveedor: {} (ID: {})", 
                empleado.getNombre(), nombreProveedor, response.getProveedor().getIdProveedor());

            return response.getProveedor();
        } catch (Exception e) {
            log.error("❌ Error agregando proveedor '{}': {}", nombreProveedor, e.getMessage(), e);
            throw new RuntimeException("Error agregando proveedor", e);
        }
    }
}