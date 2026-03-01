package com.isam.simulador.client;

import com.isam.grpc.catalogo.CatalogoServiceGrpc;
import com.isam.grpc.inventario.InventarioServiceGrpc;
import com.isam.grpc.usuarios.UsuarioServiceGrpc;
import com.isam.grpc.ventas.VentasServiceGrpc;
import com.isam.grpc.usuarios.IniciarSesionRequest;
import com.isam.grpc.usuarios.IniciarSesionRequest.Response;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Metadata;
import io.grpc.stub.AbstractStub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cliente gRPC con manejo de autenticación por token.
 * Crea y cachea stubs autenticados para diferentes tokens.
 */
@Component
@Slf4j
public class GrpcClientWithAuth {

    @Value("${simulador.admin.usuario}")
    private String adminUsuario;

    @Value("${simulador.admin.password}")
    private String adminPassword;

    private final Map<String, CatalogoServiceGrpc.CatalogoServiceBlockingStub> catalogoStubsByToken = new ConcurrentHashMap<>();
    private final Map<String, InventarioServiceGrpc.InventarioServiceBlockingStub> inventarioStubsByToken = new ConcurrentHashMap<>();
    private final Map<String, VentasServiceGrpc.VentasServiceBlockingStub> ventasStubsByToken = new ConcurrentHashMap<>();

    private final UsuarioServiceGrpc.UsuarioServiceBlockingStub usuariosStub;
    private final ManagedChannel catalogoChannel;
    private final ManagedChannel inventarioChannel;
    private final ManagedChannel ventasChannel;

    private String adminToken;

    public GrpcClientWithAuth(UsuarioServiceGrpc.UsuarioServiceBlockingStub usuariosStub,
                            ManagedChannel catalogoChannel,
                            ManagedChannel inventarioChannel,
                            ManagedChannel ventasChannel) {
        this.usuariosStub = usuariosStub;
        this.catalogoChannel = catalogoChannel;
        this.inventarioChannel = inventarioChannel;
        this.ventasChannel = ventasChannel;
    }

    /**
     * Inicializa el token de administrador al iniciar el componente.
     */
    @PostConstruct
    public void inicializarTokenAdmin() {
        try {
            this.adminToken = obtenerTokenAdmin();
            log.info("🔐 Token de administrador obtenido correctamente");
        } catch (Exception e) {
            log.error("❌ Error obteniendo token de administrador: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo obtener token de administrador", e);
        }
    }

    /**
     * Obtiene el token JWT del administrador mediante login.
     */
    private String obtenerTokenAdmin() {
        try {
            IniciarSesionRequest request = IniciarSesionRequest.newBuilder()
                .setNombreUsuario(adminUsuario)
                .setPassword(adminPassword)
                .build();

            Response response = usuariosStub.iniciarSesion(request);
            String token = response.getTokenJwt();
            
            log.info("✅ Login exitoso como admin. Token: {}...", 
                token.substring(0, Math.min(20, token.length())));
            
            return token;
        } catch (Exception e) {
            log.error("❌ Error en login de administrador: {}", e.getMessage(), e);
            throw new RuntimeException("Falló el login de administrador", e);
        }
    }

    /**
     * Obtiene el token de administrador para usar en todos los empleados.
     */
    public String getAdminToken() {
        if (adminToken == null) {
            throw new IllegalStateException("El token de admin no ha sido inicializado");
        }
        return adminToken;
    }

    /**
     * Obtiene un stub de catálogo con el token especificado.
     */
    public CatalogoServiceGrpc.CatalogoServiceBlockingStub getCatalogoStubConToken(String token) {
        return catalogoStubsByToken.computeIfAbsent(token, this::crearCatalogoStubConToken);
    }

    /**
     * Obtiene un stub de inventario con el token especificado.
     */
    public InventarioServiceGrpc.InventarioServiceBlockingStub getInventarioStubConToken(String token) {
        return inventarioStubsByToken.computeIfAbsent(token, this::crearInventarioStubConToken);
    }

    /**
     * Obtiene un stub de ventas con el token especificado.
     */
    public VentasServiceGrpc.VentasServiceBlockingStub getVentasStubConToken(String token) {
        return ventasStubsByToken.computeIfAbsent(token, this::crearVentasStubConToken);
    }

    /**
     * Crea un stub de catálogo con autenticación por token.
     */
    private CatalogoServiceGrpc.CatalogoServiceBlockingStub crearCatalogoStubConToken(String token) {
        ClientInterceptor interceptor = crearInterceptorConToken(token);
        return CatalogoServiceGrpc.newBlockingStub(catalogoChannel).withInterceptors(interceptor);
    }

    /**
     * Crea un stub de inventario con autenticación por token.
     */
    private InventarioServiceGrpc.InventarioServiceBlockingStub crearInventarioStubConToken(String token) {
        ClientInterceptor interceptor = crearInterceptorConToken(token);
        return InventarioServiceGrpc.newBlockingStub(inventarioChannel).withInterceptors(interceptor);
    }

    /**
     * Crea un stub de ventas con autenticación por token.
     */
    private VentasServiceGrpc.VentasServiceBlockingStub crearVentasStubConToken(String token) {
        ClientInterceptor interceptor = crearInterceptorConToken(token);
        return VentasServiceGrpc.newBlockingStub(ventasChannel).withInterceptors(interceptor);
    }

    /**
     * Crea un interceptor que añade el token JWT a los metadatos.
     */
    private ClientInterceptor crearInterceptorConToken(String token) {
        Metadata headers = new Metadata();
        Metadata.Key<String> tokenKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(tokenKey, "Bearer " + token);
        
        return new MetadataUtils.FixedHeadersInterceptor(headers);
    }

    /**
     * Refresca el token de administrador si es necesario.
     */
    public void refrescarTokenAdmin() {
        log.info("🔄 Refrescando token de administrador...");
        this.adminToken = obtenerTokenAdmin();
        
        // Limpiar cache de stubs para que se regeneren con el nuevo token
        catalogoStubsByToken.clear();
        inventarioStubsByToken.clear();
        ventasStubsByToken.clear();
        
        log.info("✅ Token de administrador refrescado correctamente");
    }
}