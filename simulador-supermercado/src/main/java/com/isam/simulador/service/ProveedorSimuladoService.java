package com.isam.simulador.service;

import com.isam.grpc.common.UnidadMedida;
import com.isam.grpc.inventario.LoteProto;
import com.isam.simulador.client.InventarioClient;
import com.isam.simulador.model.EmpleadoSimulado;
import com.isam.simulador.model.LoteSimulado;
import com.isam.simulador.model.ProductoSimulado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servicio que simula el reaprovisionamiento de stock por parte de proveedores.
 * Revisa periódicamente el stock en almacén y compra mercancía cuando baja del umbral.
 * También limpia periódicamente los lotes agotados para evitar acumulación en memoria.
 */
@Service
@Slf4j
public class ProveedorSimuladoService {

    private final InventarioClient inventarioClient;
    private final EstadoSimulacion estadoSimulacion;
    private final EmpleadosSimuladosService empleadosService;

    @Value("${simulador.stock.reaprovisionamiento.umbral:15}")
    private int umbralReaprovisionamiento;

    @Value("${simulador.stock.reaprovisionamiento.cantidad:50}")
    private int cantidadReaprovisionamiento;

    public ProveedorSimuladoService(InventarioClient inventarioClient,
                                    EstadoSimulacion estadoSimulacion,
                                    EmpleadosSimuladosService empleadosService) {
        this.inventarioClient = inventarioClient;
        this.estadoSimulacion = estadoSimulacion;
        this.empleadosService = empleadosService;
    }

    @Scheduled(fixedRate = 10000, initialDelay = 20000)
    public void revisarYReaprovisionarAlmacen() {
        try {
            EmpleadoSimulado supervisor = empleadosService.obtenerSupervisorAleatorio();
            if (supervisor == null) return;

            Map<String, ProductoSimulado> productos = estadoSimulacion.getProductos();

            for (ProductoSimulado producto : productos.values()) {
                // Limpiar lotes agotados del producto para evitar acumulación en memoria
                producto.limpiarLotesAgotados();

                BigDecimal stockAlmacen = producto.getStockTotalAlmacen();

                if (stockAlmacen.compareTo(BigDecimal.valueOf(umbralReaprovisionamiento)) < 0) {
                    log.info("📉 SKU: {} tiene stock bajo en almacén: {} (umbral: {})", 
                        producto.getSku(), stockAlmacen, umbralReaprovisionamiento);
                    comprarMercancia(producto, supervisor);
                }
            }

            // Limpiar lotes agotados del estado global periódicamente
            int lotesLimpiados = estadoSimulacion.limpiarLotesAgotados();
            if (lotesLimpiados > 0) {
                log.info("🧹 Limpiados {} lotes agotados del estado de simulación", lotesLimpiados);
            }
        } catch (Exception e) {
            log.error("❌ Error en ciclo de reaprovisionamiento: {}", e.getMessage());
        }
    }

    private void comprarMercancia(ProductoSimulado producto, EmpleadoSimulado supervisor) {
        try {
            String idProveedor = estadoSimulacion.obtenerIdProveedorAleatorio();
            if (idProveedor == null) {
                log.warn("⚠️ No hay proveedores disponibles para reaprovisionar SKU: {}. "
                    + "La simulación no podrá reponer stock hasta que haya proveedores.", producto.getSku());
                return;
            }

            String numeroLote = "L-AUTO-" + System.currentTimeMillis() % 100000;
            int cantidad = cantidadReaprovisionamiento + ThreadLocalRandom.current().nextInt(20);
            
            String fechaCaducidad = null;
            if (producto.isCaduca()) {
                fechaCaducidad = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(30) + 10).toString();
            }

            log.info("🚚 Comprando para {}: {} unidades de {} (Proveedor: {})", 
                producto.getSku(), cantidad, producto.getNombre(), idProveedor);

            LoteProto loteProto = inventarioClient.registrarNuevasExistencias(
                producto.getSku(), String.valueOf(cantidad), numeroLote, idProveedor,
                UnidadMedida.UNIDAD, fechaCaducidad, supervisor
            );

            // Solo añadir al estado local si el backend respondió exitosamente
            LoteSimulado nuevoLote = new LoteSimulado(
                loteProto.getIdLote(), producto.getSku(), numeroLote,
                new BigDecimal(cantidad), fechaCaducidad, idProveedor
            );
            
            estadoSimulacion.añadirLote(producto.getSku(), nuevoLote);
            producto.añadirLote(nuevoLote);

            log.info("✅ Reaprovisionamiento exitoso para SKU: {} - Lote: {}, Cantidad: {}", 
                producto.getSku(), loteProto.getIdLote(), cantidad);

        } catch (Exception e) {
            log.error("❌ Error comprando mercancía para SKU {}: {}", producto.getSku(), e.getMessage());
        }
    }
}
