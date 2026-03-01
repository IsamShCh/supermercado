package com.isam.simulador.client;

import com.isam.grpc.ventas.*;
import com.isam.grpc.ventas.MetodoPago;
import com.isam.simulador.model.EmpleadoSimulado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Cliente gRPC para el servicio de ventas.
 * Maneja la creación y procesamiento de tickets de venta.
 */
@Component
@Slf4j
public class VentasClient {

    private final GrpcClientWithAuth grpcClient;

    public VentasClient(GrpcClientWithAuth grpcClient) {
        this.grpcClient = grpcClient;
    }

    /**
     * Crea un nuevo ticket temporal para iniciar una venta.
     */
    public String crearTicketTemporal(EmpleadoSimulado empleado) {
        try {
            CrearNuevoTicketRequest request = CrearNuevoTicketRequest.newBuilder().build();

            VentasServiceGrpc.VentasServiceBlockingStub stub = 
                grpcClient.getVentasStubConToken(empleado.getToken());

            CrearNuevoTicketRequest.Response response = stub.crearNuevoTicket(request);
            String idTicketTemporal = response.getIdTicketTemporal();

            log.info("🎫 {} creó ticket temporal: {} - Cajero: {}", 
                empleado.getNombre(), idTicketTemporal, response.getNombreCajero());

            return idTicketTemporal;
        } catch (Exception e) {
            log.error("❌ Error creando ticket temporal: {}", e.getMessage(), e);
            throw new RuntimeException("Error creando ticket temporal", e);
        }
    }

    /**
     * Añade un producto al ticket temporal.
     */
    public void añadirProductoTicket(String idTicket, String codigoBarras, EmpleadoSimulado empleado) {
        try {
            AnadirProductoTicketRequest request = AnadirProductoTicketRequest.newBuilder()
                .setIdTicketTemporal(idTicket)
                .setCodigoBarras(codigoBarras)
                .build();

            VentasServiceGrpc.VentasServiceBlockingStub stub = 
                grpcClient.getVentasStubConToken(empleado.getToken());

            AnadirProductoTicketRequest.Response response = stub.anadirProductoTicket(request);

            log.info("🛒 {} añadió producto al ticket - SKU: {}, Cantidad: {}, Subtotal: {}", 
                empleado.getNombre(), response.getSku(), response.getCantidad(), response.getSubtotalTicketActual());

        } catch (Exception e) {
            log.error("❌ Error añadiendo producto '{}' al ticket: {}", codigoBarras, e.getMessage(), e);
            throw new RuntimeException("Error añadiendo producto al ticket", e);
        }
    }

    /**
     * Elimina un producto del ticket temporal.
     */
    public void eliminarProductoTicket(String idTicket, String sku, String cantidad, EmpleadoSimulado empleado) {
        try {
            EliminarProductoTicketRequest.Builder requestBuilder = EliminarProductoTicketRequest.newBuilder()
                .setIdTicket(idTicket)
                .setSku(sku);

            // Añadir cantidad solo si se especifica
            if (cantidad != null && !cantidad.isEmpty()) {
                requestBuilder.setCantidadAEliminar(cantidad);
            }

            EliminarProductoTicketRequest request = requestBuilder.build();

            VentasServiceGrpc.VentasServiceBlockingStub stub = 
                grpcClient.getVentasStubConToken(empleado.getToken());

            EliminarProductoTicketRequest.Response response = stub.eliminarProductoTicket(request);

            log.info("🗑️ {} eliminó producto del ticket - SKU: {}, Cantidad: {}", 
                empleado.getNombre(), response.getSku(), response.getCantidadEliminada());

        } catch (Exception e) {
            log.error("❌ Error eliminando producto '{}' del ticket: {}", sku, e.getMessage(), e);
            throw new RuntimeException("Error eliminando producto del ticket", e);
        }
    }

    /**
     * Procesa el pago de un ticket.
     */
    public void procesarPago(String idTicket, MetodoPago metodoPago, String montoRecibido, EmpleadoSimulado empleado) {
        try {
            ProcesarPagoRequest request = ProcesarPagoRequest.newBuilder()
                .setIdTicketTemporal(idTicket)
                .setMetodoPago(metodoPago)
                .setMontoRecibido(montoRecibido)
                .build();

            VentasServiceGrpc.VentasServiceBlockingStub stub = 
                grpcClient.getVentasStubConToken(empleado.getToken());

            ProcesarPagoRequest.Response response = stub.procesarPago(request);

            log.info("💳 {} procesó pago - Ticket: {}, Método: {}, Recibido: {}, Cambio: {}", 
                empleado.getNombre(), idTicket, metodoPago, montoRecibido, response.getMontoCambio());

        } catch (Exception e) {
            log.error("❌ Error procesando pago para ticket '{}': {}", idTicket, e.getMessage(), e);
            throw new RuntimeException("Error procesando pago", e);
        }
    }

    /**
     * Cierra un ticket temporal y genera el ticket final.
     */
    public String cerrarTicket(String idTicket, EmpleadoSimulado empleado) {
        try {
            CerrarTicketRequest request = CerrarTicketRequest.newBuilder()
                .setIdTicketTemporal(idTicket)
                .build();

            VentasServiceGrpc.VentasServiceBlockingStub stub = 
                grpcClient.getVentasStubConToken(empleado.getToken());

            CerrarTicketRequest.Response response = stub.cerrarTicket(request);

            log.info("✅ {} cerró ticket - Temporal: {}, Final: {}, Total: {}, Items: {}", 
                empleado.getNombre(), idTicket, response.getNumeroTicket(), 
                response.getTotal(), response.getLineasVentaCount());

            return response.getNumeroTicket();
        } catch (Exception e) {
            log.error("❌ Error cerrando ticket '{}': {}", idTicket, e.getMessage(), e);
            throw new RuntimeException("Error cerrando ticket", e);
        }
    }

    /**
     * Consulta el contenido de un ticket.
     */
    public ConsultarTicketRequest.Response consultarTicket(String idTicket, EmpleadoSimulado empleado) {
        try {
            ConsultarTicketRequest request = ConsultarTicketRequest.newBuilder()
                .setIdTicket(idTicket)
                .build();

            VentasServiceGrpc.VentasServiceBlockingStub stub = 
                grpcClient.getVentasStubConToken(empleado.getToken());

            ConsultarTicketRequest.Response response = stub.consultarTicket(request);

            log.debug("🔍 {} consultó ticket - ID: {}, Total: {}", 
                empleado.getNombre(), idTicket, response.getTotal());

            return response;
        } catch (Exception e) {
            log.error("❌ Error consultando ticket '{}': {}", idTicket, e.getMessage(), e);
            throw new RuntimeException("Error consultando ticket", e);
        }
    }

    /**
     * Cancela un ticket temporal.
     */
    public void cancelarTicket(String idTicket, EmpleadoSimulado empleado) {
        try {
            CancelarTicketRequest request = CancelarTicketRequest.newBuilder()
                .setIdTicket(idTicket)
                .build();

            VentasServiceGrpc.VentasServiceBlockingStub stub = 
                grpcClient.getVentasStubConToken(empleado.getToken());

            CancelarTicketRequest.Response response = stub.cancelarTicket(request);

            log.info("❌ {} canceló ticket - ID: {}", 
                empleado.getNombre(), idTicket);

        } catch (Exception e) {
            log.error("❌ Error cancelando ticket '{}': {}", idTicket, e.getMessage(), e);
            throw new RuntimeException("Error cancelando ticket", e);
        }
    }

    /**
     * Simula una venta completa desde inicio hasta fin.
     */
    public String simularVentaCompleta(List<String> skus, MetodoPago metodoPago, EmpleadoSimulado empleado) {
        try {
            // 1. Crear ticket temporal
            String idTicket = crearTicketTemporal(empleado);

            // 2. Añadir productos
            for (String sku : skus) {
                añadirProductoTicket(idTicket, sku, empleado);
            }

            // 3. Procesar pago (simular monto suficiente)
            procesarPago(idTicket, metodoPago, "1000.00", empleado);

            // 4. Cerrar ticket
            String numeroTicket = cerrarTicket(idTicket, empleado);

            log.info("🛒 Venta simulada completada - Ticket: {}, Productos: {}, Cajero: {}", 
                numeroTicket, skus.size(), empleado.getNombre());

            return numeroTicket;
        } catch (Exception e) {
            log.error("❌ Error en simulación de venta: {}", e.getMessage(), e);
            throw new RuntimeException("Error en simulación de venta", e);
        }
    }

    /**
     * Obtiene una lista de métodos de pago aleatorios para simulación.
     */
    public static MetodoPago obtenerMetodoPagoAleatorio() {
        MetodoPago[] metodos = {
            MetodoPago.EFECTIVO,
            MetodoPago.TARJETA_DEBITO,
            MetodoPago.TARJETA_CREDITO,
            MetodoPago.TRANSFERENCIA
        };
        
        return metodos[(int) (Math.random() * metodos.length)];
    }
}