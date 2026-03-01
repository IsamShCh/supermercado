package com.isam.simulador.config;

import com.isam.grpc.catalogo.CatalogoServiceGrpc;
import com.isam.grpc.inventario.InventarioServiceGrpc;
import com.isam.grpc.usuarios.UsuarioServiceGrpc;
import com.isam.grpc.ventas.VentasServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de clientes gRPC para los servicios del supermercado.
 */
@Configuration
@Slf4j
public class GrpcConfig {

    @Value("${servicios.catalogo.host}")
    private String catalogoHost;

    @Value("${servicios.catalogo.puerto}")
    private int catalogoPuerto;

    @Value("${servicios.inventario.host}")
    private String inventarioHost;

    @Value("${servicios.inventario.puerto}")
    private int inventarioPuerto;

    @Value("${servicios.ventas.host}")
    private String ventasHost;

    @Value("${servicios.ventas.puerto}")
    private int ventasPuerto;

    @Value("${servicios.usuarios.host}")
    private String usuariosHost;

    @Value("${servicios.usuarios.puerto}")
    private int usuariosPuerto;

    /**
     * Crea un canal gRPC para el servicio de catálogo.
     */
    @Bean
    public ManagedChannel catalogoChannel() {
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress(catalogoHost, catalogoPuerto)
            .usePlaintext()
            .keepAliveTime(5, TimeUnit.MINUTES)
            .keepAliveTimeout(5, TimeUnit.SECONDS)
            // .keepAliveWithoutCalls(true)
            .build();
        
        log.info("🔌 Canal gRPC creado para Catálogo: {}:{}", catalogoHost, catalogoPuerto);
        return channel;
    }

    /**
     * Crea un canal gRPC para el servicio de inventario.
     */
    @Bean
    public ManagedChannel inventarioChannel() {
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress(inventarioHost, inventarioPuerto)
            .usePlaintext()
            .keepAliveTime(5, TimeUnit.MINUTES)
            .keepAliveTimeout(5, TimeUnit.SECONDS)
            // .keepAliveWithoutCalls(true)
            .build();
        
        log.info("🔌 Canal gRPC creado para Inventario: {}:{}", inventarioHost, inventarioPuerto);
        return channel;
    }

    /**
     * Crea un canal gRPC para el servicio de ventas.
     */
    @Bean
    public ManagedChannel ventasChannel() {
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress(ventasHost, ventasPuerto)
            .usePlaintext()
            .keepAliveTime(5, TimeUnit.MINUTES)
            .keepAliveTimeout(5, TimeUnit.SECONDS)
            // .keepAliveWithoutCalls(true)
            .build();
        
        log.info("🔌 Canal gRPC creado para Ventas: {}:{}", ventasHost, ventasPuerto);
        return channel;
    }

    /**
     * Crea un canal gRPC para el servicio de usuarios (para login).
     */
    @Bean
    public ManagedChannel usuariosChannel() {
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress(usuariosHost, usuariosPuerto)
            .usePlaintext()
            .keepAliveTime(5, TimeUnit.MINUTES)
            .keepAliveTimeout(5, TimeUnit.SECONDS)
            // .keepAliveWithoutCalls(true)
            .build();
        
        log.info("🔌 Canal gRPC creado para Usuarios: {}:{}", usuariosHost, usuariosPuerto);
        return channel;
    }

    /**
     * Crea el stub blocking para el servicio de catálogo.
     */
    @Bean
    public CatalogoServiceGrpc.CatalogoServiceBlockingStub catalogoStub(ManagedChannel catalogoChannel) {
        return CatalogoServiceGrpc.newBlockingStub(catalogoChannel);
    }

    /**
     * Crea el stub blocking para el servicio de inventario.
     */
    @Bean
    public InventarioServiceGrpc.InventarioServiceBlockingStub inventarioStub(ManagedChannel inventarioChannel) {
        return InventarioServiceGrpc.newBlockingStub(inventarioChannel);
    }

    /**
     * Crea el stub blocking para el servicio de ventas.
     */
    @Bean
    public VentasServiceGrpc.VentasServiceBlockingStub ventasStub(ManagedChannel ventasChannel) {
        return VentasServiceGrpc.newBlockingStub(ventasChannel);
    }

    /**
     * Crea el stub blocking para el servicio de usuarios.
     */
    @Bean
    public UsuarioServiceGrpc.UsuarioServiceBlockingStub usuariosStub(ManagedChannel usuariosChannel) {
        return UsuarioServiceGrpc.newBlockingStub(usuariosChannel);
    }
}