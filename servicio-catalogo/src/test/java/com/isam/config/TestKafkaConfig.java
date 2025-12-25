package com.isam.config;

import com.isam.service.ports.IProductoEventPublisher;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Configuración de prueba para evitar que los tests intenten conectar con un
 * Kafka real.
 * Proporciona un Mock de IProductoEventPublisher.
 */
@TestConfiguration
public class TestKafkaConfig {

    @Bean
    @Primary
    public IProductoEventPublisher productoEventPublisher() {
        return Mockito.mock(IProductoEventPublisher.class);
    }
}
