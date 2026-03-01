package com.isam.simulador.service;

import com.isam.grpc.catalogo.CategoriaProto;
import com.isam.grpc.catalogo.ListarCategoriasRequest;
import com.isam.grpc.catalogo.ProductoProto;
import com.isam.grpc.common.UnidadMedida;
import com.isam.grpc.inventario.*;
import com.isam.simulador.client.CatalogoClient;
import com.isam.simulador.client.InventarioClient;
import com.isam.simulador.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class InicializacionDatosService {

    private final CatalogoClient catalogoClient;
    private final InventarioClient inventarioClient;
    private final EstadoSimulacion estadoSimulacion;
    private final EmpleadosSimuladosService empleadosService;

    @Value("${simulador.categorias.cantidad}") private int cantidadCategorias;
    @Value("${simulador.productos.cantidad}") private int cantidadProductos;
    @Value("${simulador.stock.inicial.minimo}") private int stockInicialMinimo;
    @Value("${simulador.stock.inicial.maximo}") private int stockInicialMaximo;
    @Value("${simulador.estanteria.capacidad.minima}") private int capacidadEstanteriaMinima;
    @Value("${simulador.estanteria.capacidad.maxima}") private int capacidadEstanteriaMaxima;

    private final Random random = new Random();

    // Datos estáticos
    private final List<String[]> datosCategorias = List.of(
        new String[]{"1", "Lácteos", "Productos lácteos"},
        new String[]{"2", "Carnes", "Carnes frescas"},
        new String[]{"3", "Frutas y Verduras", "Productos frescos"}
    );

    private final List<String[]> datosProductos = List.of(
        new String[]{"LEC-001", "Leche Entera", "1", "1.20", "false", "false"},
        new String[]{"LEC-002", "Yogur Natural", "1", "0.85", "true", "false"},
        new String[]{"LEC-003", "Queso Fresco", "1", "2.50", "true", "false"},
        new String[]{"CAR-001", "Pollo Fresco", "2", "3.80", "true", "false"},
        new String[]{"CAR-002", "Carne Molida", "2", "5.20", "true", "false"},
        new String[]{"CAR-003", "Chorizo", "2", "4.10", "false", "false"},
        new String[]{"FRV-001", "Manzanas", "3", "2.30", "true", "false"},
        new String[]{"FRV-002", "Plátanos", "3", "1.80", "true", "false"},
        new String[]{"FRV-003", "Tomates", "3", "2.10", "true", "false"},
        new String[]{"FRV-004", "Lechuga", "3", "1.50", "true", "false"},
        new String[]{"FRV-005", "Naranjas", "3", "2.80", "true", "false"}
    );
    
    private final List<String[]> datosProveedores = List.of(
        new String[]{"Distribuidora Central S.L.", "Juan Garcia", "Calle Mayor 1", "600111222", "central@dist.com"},
        new String[]{"Frutas del Campo S.A.", "Maria Lopez", "Avda Huerta 5", "600333444", "ventas@frutas.com"},
        new String[]{"Carnes Selectas", "Pedro Sanz", "Poligono Ind 8", "600555666", "pedro@carnes.com"}
    );

    public InicializacionDatosService(CatalogoClient catalogoClient, InventarioClient inventarioClient,
                                   EstadoSimulacion estadoSimulacion, EmpleadosSimuladosService empleadosService) {
        this.catalogoClient = catalogoClient;
        this.inventarioClient = inventarioClient;
        this.estadoSimulacion = estadoSimulacion;
        this.empleadosService = empleadosService;
    }

    @PostConstruct
    public void inicializarDatos() {
        try {
            log.info("🏗️ Iniciando inicialización de datos...");
            estadoSimulacion.inicializar();
            
            // 1. Proveedores (CRÍTICO: Obtener IDs reales)
            crearProveedores();
            
            // 2. Categorías
            crearCategorias();
            
            // 3. Productos (Guardar EAN/PLU en memoria)
            crearProductos();
            
            // 4. Inventarios (Usar EAN/PLU de memoria)
            crearInventarios();
            
            // 5. Estanterías
            crearEstanterias();
            
            // 6. Lotes (Usar IDs reales de proveedores)
            crearLotesIniciales();
            
            // 7. Mover Stock
            moverStockInicialAEstanterias();
            
            log.info("✅ Inicialización completada");
            mostrarResumen();
            
        } catch (Exception e) {
            log.error("❌ Error fatal en inicialización", e);
        }
    }

    private void crearProveedores() {
        log.info("? Creando proveedores...");
        String suffix = "-" + System.currentTimeMillis() % 10000;
        for (String[] datos : datosProveedores) {
            String nombreUnico = datos[0] + suffix;
            String emailUnico = datos[4].replace("@", suffix + "@");
            try {
                ProveedorProto prov = inventarioClient.agregarProveedor(
                    nombreUnico, datos[1], datos[2], datos[3], emailUnico,
                    empleadosService.obtenerEmpleadoAleatorio()
                );
                estadoSimulacion.añadirIdProveedor(prov.getIdProveedor());
                log.info("✅ Proveedor creado/recuperado: {} (ID: {})", nombreUnico, prov.getIdProveedor());
            } catch (Exception e) {
                 // Si ya existe, deberíamos buscarlo, pero para simplificar, si falla no lo añadimos
                 // y el simulador usará los que sí se hayan creado.
                 log.warn("⚠️ Error proveedor {}: {}", nombreUnico, e.getMessage());
            }
        }
    }

    private void crearCategorias() {
        boolean categoriasYaExisten = false;
        for (int i = 0; i < Math.min(cantidadCategorias, datosCategorias.size()); i++) {
            String[] datos = datosCategorias.get(i);
            try {
                CategoriaProto cat = catalogoClient.crearCategoria(datos[1], datos[2], empleadosService.obtenerEmpleadoAleatorio());
                estadoSimulacion.añadirCategoria(new CategoriaSimulada(cat.getIdCategoria(), datos[1], datos[2]));
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                if (cause.getMessage() != null && cause.getMessage().contains("ALREADY_EXISTS")) {
                    categoriasYaExisten = true;
                } else {
                    log.error("❌ Error categoría {}: {}", datos[1], e.getMessage());
                }
            }
        }
        if (categoriasYaExisten) cargarCategoriasExistentes();
    }
    
    private void cargarCategoriasExistentes() {
        try {
            ListarCategoriasRequest.Response res = catalogoClient.listarCategorias(empleadosService.obtenerEmpleadoAleatorio());
            for (CategoriaProto c : res.getCategoriasList()) {
                estadoSimulacion.añadirCategoria(new CategoriaSimulada(c.getIdCategoria(), c.getNombreCategoria(), c.getDescripcion()));
            }
        } catch (Exception e) {
            log.error("❌ Error cargando categorías");
        }
    }

    private void crearProductos() {
        log.info("📦 Creando productos...");
        for (int i = 0; i < Math.min(cantidadProductos, datosProductos.size()); i++) {
            String[] d = datosProductos.get(i);
            try {
                ProductoProto p = catalogoClient.crearProducto(
                    d[0], d[1], d[3], Long.parseLong(d[2]), 
                    Boolean.parseBoolean(d[5]), Boolean.parseBoolean(d[4]),
                    empleadosService.obtenerEmpleadoAleatorio()
                );
                guardarProductoEnSimulacion(p, d[2]);
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                if (cause.getMessage() != null && cause.getMessage().contains("ALREADY_EXISTS")) {
                    try {
                        ProductoProto p = catalogoClient.consultarProducto(d[0], empleadosService.obtenerEmpleadoAleatorio());
                        if (p != null) guardarProductoEnSimulacion(p, d[2]);
                    } catch (Exception ex) {}
                }
            }
        }
    }

    private void guardarProductoEnSimulacion(ProductoProto p, String idCategoria) {
        CategoriaSimulada cat = estadoSimulacion.getCategorias().get(Long.parseLong(idCategoria));
        if (cat == null) return;

        ProductoSimulado ps = new ProductoSimulado(
            p.getSku(), p.getNombre(), cat.getNombreCategoria(),
            new BigDecimal(p.getPrecioVenta()), p.getCaduca(), p.getEsGranel()
        );
        
        // CRÍTICO: Guardar EAN/PLU que viene del servidor
        if (p.hasEan() && !p.getEan().isEmpty()) ps.setEan(p.getEan());
        if (p.hasPlu() && !p.getPlu().isEmpty()) ps.setPlu(p.getPlu());
        
        estadoSimulacion.añadirProducto(ps);
    }

    private void crearInventarios() {
        log.info("📋 Creando inventarios...");
        for (ProductoSimulado p : estadoSimulacion.getProductos().values()) {
            try {
                // CORRECCION: Enviar EAN o PLU
                inventarioClient.crearInventario(
                    p.getSku(), 
                    p.getEan(), 
                    p.getPlu(), 
                    UnidadMedida.UNIDAD, 
                    empleadosService.obtenerEmpleadoAleatorio()
                );
            } catch (Exception e) {
                 // Ignorar si ya existe
            }
        }
    }

    private void crearEstanterias() {
        log.info("🏪 Creando estanterías...");
        for (ProductoSimulado p : estadoSimulacion.getProductos().values()) {
            int cap = random.nextInt(capacidadEstanteriaMaxima - capacidadEstanteriaMinima + 1) + capacidadEstanteriaMinima;
            Estanteria est = new Estanteria("EST-" + p.getSku(), p.getSku(), new BigDecimal(cap));
            estadoSimulacion.añadirEstanteria(est);
            p.setEstanteria(est);
        }
    }

    private void crearLotesIniciales() {
        log.info("📦 Creando lotes iniciales...");
        String idProveedorReal = estadoSimulacion.obtenerIdProveedorAleatorio();
        
        if (idProveedorReal == null) {
            log.error("❌ ERROR CRÍTICO: No hay proveedores reales disponibles. No se crearán lotes.");
            return;
        }

        for (ProductoSimulado p : estadoSimulacion.getProductos().values()) {
            int cantLotes = random.nextInt(3) + 2;
            for (int i = 0; i < cantLotes; i++) {
                crearLoteParaProducto(p, i, idProveedorReal);
            }
        }
    }

    private void crearLoteParaProducto(ProductoSimulado p, int idx, String idProveedor) {
        try {
            String numeroLote = "L" + String.format("%06d", System.currentTimeMillis() % 1000000) + idx;
            int cantidad = random.nextInt(stockInicialMaximo - stockInicialMinimo + 1) + stockInicialMinimo;
            String fechaCad = p.isCaduca() ? LocalDate.now().plusDays(random.nextInt(76) + 15).toString() : null;

            // CORRECCION: Usar el ID de proveedor real pasado como argumento
            LoteProto proto = inventarioClient.registrarNuevasExistencias(
                p.getSku(), String.valueOf(cantidad), numeroLote, idProveedor,
                UnidadMedida.UNIDAD, fechaCad, empleadosService.obtenerEmpleadoAleatorio()
            );

            LoteSimulado lote = new LoteSimulado(
                proto.getIdLote(), p.getSku(), numeroLote, new BigDecimal(cantidad),
                fechaCad, idProveedor
            );
            estadoSimulacion.añadirLote(p.getSku(), lote);
        } catch (Exception e) {
            log.error("❌ Error creando lote SKU {}: {}", p.getSku(), e.getMessage());
        }
    }

    private void moverStockInicialAEstanterias() {
        log.info("📦 Moviendo stock inicial...");
        for (Estanteria est : estadoSimulacion.getAllEstanterias()) {
            double porcentaje = 0.30 + (random.nextDouble() * 0.30);
            BigDecimal cant = est.getCapacidadMaxima().multiply(new BigDecimal(porcentaje)).setScale(0, BigDecimal.ROUND_HALF_UP);
            
            LoteSimulado lote = estadoSimulacion.seleccionarLoteFIFO(est.getSku(), cant);
            if (lote != null) {
                if (estadoSimulacion.moverAlmacenAEstanteria(est.getSku(), lote.getIdLote(), cant)) {
                    try {
                        inventarioClient.moverStockEstanteria(
                            est.getSku(), lote.getIdLote(), cant.toString(),
                            UnidadMedida.UNIDAD, empleadosService.obtenerEmpleadoAleatorio()
                        );
                    } catch (Exception e) {
                        log.error("❌ Fallo movimiento stock SKU {}", est.getSku());
                    }
                }
            }
        }
    }

    private void mostrarResumen() {
        log.info("📊 Inicialización: {} Cats, {} Prods, {} Lotes, Empleados: {}",
            estadoSimulacion.getCategorias().size(),
            estadoSimulacion.getProductos().size(),
            estadoSimulacion.getProductos().values().stream().mapToInt(p -> p.getLotes().size()).sum(),
            empleadosService.obtenerTodosLosEmpleados().size());
    }
}