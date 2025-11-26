package com.isam.service;

import com.isam.dto.comun.PaginacionDto;
import com.isam.dto.oferta.OfertaDto;
import com.isam.dto.producto.BuscarProductosDto;
import com.isam.dto.producto.ListaProductosDto;
import com.isam.model.*;
import com.isam.repository.CategoriaRepository;
import com.isam.repository.OfertaRepository;
import com.isam.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración para la funcionalidad BuscarProductos en CatalogoService.
 * Prueba la búsqueda con varios criterios y paginación.
 */
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CatalogoService.class))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CatalogoServiceBuscarProductosTest {

    @Autowired
    private CatalogoService catalogoService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private OfertaRepository ofertaRepository;

    private Categoria categoriaLacteos;
    private Categoria categoriaFrutas;
    private Producto leche;
    private Producto yogurt;
    private Producto manzanas;
    private Producto platanos;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        ofertaRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();

        // Create test categories
        categoriaLacteos = new Categoria();
        categoriaLacteos.setNombreCategoria("Lácteos");
        categoriaLacteos.setDescripcion("Productos lácteos");
        categoriaLacteos = categoriaRepository.save(categoriaLacteos);

        categoriaFrutas = new Categoria();
        categoriaFrutas.setNombreCategoria("Frutas");
        categoriaFrutas.setDescripcion("Frutas frescas");
        categoriaFrutas = categoriaRepository.save(categoriaFrutas);

        // Create test products - Lácteos
        leche = new Producto();
        leche.setSku("LECHE-001");
        leche.setEan("1234567890123");
        leche.setNombre("Leche Entera");
        leche.setDescripcion("Leche entera 1L");
        leche.setPrecioVenta(new BigDecimal("1.50"));
        leche.setCaduca(true);
        leche.setEsGranel(false);
        leche.setCategoria(categoriaLacteos);
        leche.setPoliticaRotacion(PoliticaRotacion.FIFO);
        leche.setUnidadMedida(UnidadMedida.LITRO);
        leche.setEtiquetas("lacteo,bebida,refrigerado");
        leche.setEstado(EstadoProducto.ACTIVO);
        leche = productoRepository.save(leche);

        yogurt = new Producto();
        yogurt.setSku("YOGURT-001");
        yogurt.setEan("1234567890124");
        yogurt.setNombre("Yogurt Natural");
        yogurt.setDescripcion("Yogurt natural sin azúcar");
        yogurt.setPrecioVenta(new BigDecimal("0.89"));
        yogurt.setCaduca(true);
        yogurt.setEsGranel(false);
        yogurt.setCategoria(categoriaLacteos);
        yogurt.setPoliticaRotacion(PoliticaRotacion.FIFO);
        yogurt.setUnidadMedida(UnidadMedida.GRAMO);
        yogurt.setEtiquetas("lacteo,postre,refrigerado");
        yogurt.setEstado(EstadoProducto.ACTIVO);
        yogurt = productoRepository.save(yogurt);

        // Create test products - Frutas
        manzanas = new Producto();
        manzanas.setSku("MANZANA-001");
        manzanas.setPlu("12345");
        manzanas.setNombre("Manzanas Golden");
        manzanas.setDescripcion("Manzanas golden a granel");
        manzanas.setPrecioVenta(new BigDecimal("2.50"));
        manzanas.setCaduca(true);
        manzanas.setEsGranel(true);
        manzanas.setCategoria(categoriaFrutas);
        manzanas.setPoliticaRotacion(PoliticaRotacion.FEFO);
        manzanas.setUnidadMedida(UnidadMedida.KILOGRAMO);
        manzanas.setEtiquetas("fruta,fresca,organico");
        manzanas.setEstado(EstadoProducto.ACTIVO);
        manzanas = productoRepository.save(manzanas);

        platanos = new Producto();
        platanos.setSku("PLATANO-001");
        platanos.setPlu("12346");
        platanos.setNombre("Plátanos de Canarias");
        platanos.setDescripcion("Plátanos frescos de Canarias");
        platanos.setPrecioVenta(new BigDecimal("1.80"));
        platanos.setCaduca(true);
        platanos.setEsGranel(true);
        platanos.setCategoria(categoriaFrutas);
        platanos.setPoliticaRotacion(PoliticaRotacion.FEFO);
        platanos.setUnidadMedida(UnidadMedida.KILOGRAMO);
        platanos.setEtiquetas("fruta,fresca,tropical");
        platanos.setEstado(EstadoProducto.ACTIVO);
        platanos = productoRepository.save(platanos);

        // Create test offers
        Oferta ofertaLeche = new Oferta();
        ofertaLeche.setProducto(leche);
        ofertaLeche.setPrecioPromocional(new BigDecimal("1.20"));
        ofertaLeche.setTipoPromocion("Descuento 20%");
        ofertaLeche.setFechaInicio(LocalDate.now());
        ofertaLeche.setFechaFin(LocalDate.now().plusDays(7));
        ofertaLeche.setEstado(EstadoOferta.ACTIVA);
        ofertaRepository.save(ofertaLeche);
    }

    @Test
    void testBuscarProductos_SinCriterios_RetornaTodosLosProductos() {
        // Given
        BuscarProductosDto dto = new BuscarProductosDto(null, null);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.productos());
        assertEquals(4, resultado.productos().size(), "Debe retornar los 4 productos creados");
        assertNotNull(resultado.paginacion());
        assertEquals(1, resultado.paginacion().page());
        assertEquals(10, resultado.paginacion().pageSize());
        assertEquals(1, resultado.paginacion().totalPages());
        assertEquals(4L, resultado.paginacion().totalElements());
    }

    @Test
    void testBuscarProductos_PorNombre_RetornaProductosCoincidentes() {
        // Given
        BuscarProductosDto.CriteriosBusquedaDto criterios = 
            new BuscarProductosDto.CriteriosBusquedaDto("Leche", null, null, null, null, null);
        BuscarProductosDto dto = new BuscarProductosDto(criterios, null);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.productos().size());
        assertEquals("Leche Entera", resultado.productos().get(0).producto().nombre());
        assertEquals("LECHE-001", resultado.productos().get(0).producto().sku());
    }

    @Test
    void testBuscarProductos_PorCategoria_RetornaProductosDeCategoria() {
        // Given
        BuscarProductosDto.CriteriosBusquedaDto criterios = 
            new BuscarProductosDto.CriteriosBusquedaDto(null, categoriaLacteos.getIdCategoria(), null, null, null, null);
        BuscarProductosDto dto = new BuscarProductosDto(criterios, null);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.productos().size());
        assertTrue(resultado.productos().stream()
            .allMatch(p -> p.producto().categoria().idCategoria().equals(categoriaLacteos.getIdCategoria())));
    }

    @Test
    void testBuscarProductos_PorRangoPrecio_RetornaProductosEnRango() {
        // Given
        BuscarProductosDto.CriteriosBusquedaDto criterios = 
            new BuscarProductosDto.CriteriosBusquedaDto(null, null, 1.0, 2.0, null, null);
        BuscarProductosDto dto = new BuscarProductosDto(criterios, null);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.productos().size());
        assertTrue(resultado.productos().stream()
            .allMatch(p -> p.producto().precioVenta() >= 1.0 && p.producto().precioVenta() <= 2.0));
    }

    @Test
    void testBuscarProductos_PorEsGranel_RetornaSoloProductosGranel() {
        // Given
        BuscarProductosDto.CriteriosBusquedaDto criterios = 
            new BuscarProductosDto.CriteriosBusquedaDto(null, null, null, null, true, null);
        BuscarProductosDto dto = new BuscarProductosDto(criterios, null);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.productos().size());
        assertTrue(resultado.productos().stream()
            .allMatch(p -> p.producto().esGranel()));
    }

    @Test
    void testBuscarProductos_PorEtiqueta_RetornaProductosConEtiqueta() {
        // Given
        BuscarProductosDto.CriteriosBusquedaDto criterios = 
            new BuscarProductosDto.CriteriosBusquedaDto(null, null, null, null, null, List.of("fresca"));
        BuscarProductosDto dto = new BuscarProductosDto(criterios, null);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.productos().size());
        assertTrue(resultado.productos().stream()
            .allMatch(p -> p.producto().etiquetas().contains("fresca")));
    }

    @Test
    void testBuscarProductos_ConPaginacion_RetornaPaginaCorrecta() {
        // Given
        PaginacionDto paginacion = new PaginacionDto(1, 2);
        BuscarProductosDto dto = new BuscarProductosDto(null, paginacion);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.productos().size());
        assertEquals(1, resultado.paginacion().page());
        assertEquals(2, resultado.paginacion().pageSize());
        assertEquals(2, resultado.paginacion().totalPages());
        assertEquals(4L, resultado.paginacion().totalElements());
    }

    @Test
    void testBuscarProductos_SegundaPagina_RetornaProductosRestantes() {
        // Given
        PaginacionDto paginacion = new PaginacionDto(2, 2);
        BuscarProductosDto dto = new BuscarProductosDto(null, paginacion);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.productos().size());
        assertEquals(2, resultado.paginacion().page());
        assertEquals(2, resultado.paginacion().pageSize());
    }

    @Test
    void testBuscarProductos_CriteriosMultiples_RetornaProductosQueCoinciden() {
        // Given
        BuscarProductosDto.CriteriosBusquedaDto criterios = 
            new BuscarProductosDto.CriteriosBusquedaDto(
                null, 
                categoriaFrutas.getIdCategoria(), 
                null, 
                null, 
                true, 
                List.of("fresca")
            );
        BuscarProductosDto dto = new BuscarProductosDto(criterios, null);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.productos().size());
        assertTrue(resultado.productos().stream()
            .allMatch(p -> p.producto().categoria().idCategoria().equals(categoriaFrutas.getIdCategoria()) 
                && p.producto().esGranel() 
                && p.producto().etiquetas().contains("fresca")));
    }

    @Test
    void testBuscarProductos_VerificaOfertasAsociadas() {
        // Given
        BuscarProductosDto.CriteriosBusquedaDto criterios =
            new BuscarProductosDto.CriteriosBusquedaDto("Leche", null, null, null, null, null);
        BuscarProductosDto dto = new BuscarProductosDto(criterios, null);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.productos().size());
        
        ListaProductosDto.DetallesProductoCompletoDto detalles = resultado.productos().get(0);
        assertNotNull(detalles.ofertas());
        assertEquals(1, detalles.ofertas().size());
        
        OfertaDto oferta = detalles.ofertas().get(0);
        assertNotNull(oferta.idOferta());
        assertEquals(1.20, oferta.precioPromocional());
        assertEquals("Descuento 20%", oferta.tipoPromocion());
    }

    @Test
    void testBuscarProductos_VerificaCategoriaCompleta() {
        // Given
        BuscarProductosDto dto = new BuscarProductosDto(null, null);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        resultado.productos().forEach(detalles -> {
            assertNotNull(detalles.producto().categoria());
            assertNotNull(detalles.producto().categoria().idCategoria());
            assertNotNull(detalles.producto().categoria().nombreCategoria());
        });
    }

    @Test
    void testBuscarProductos_SinResultados_RetornaListaVacia() {
        // Given
        BuscarProductosDto.CriteriosBusquedaDto criterios = 
            new BuscarProductosDto.CriteriosBusquedaDto("ProductoInexistente", null, null, null, null, null);
        BuscarProductosDto dto = new BuscarProductosDto(criterios, null);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.productos());
        assertEquals(0, resultado.productos().size());
        assertEquals(0, resultado.paginacion().totalPages());
        assertEquals(0L, resultado.paginacion().totalElements());
    }

    @Test
    void testBuscarProductos_PaginaFueraDeRango_RetornaListaVacia() {
        // Given
        PaginacionDto paginacion = new PaginacionDto(10, 10);
        BuscarProductosDto dto = new BuscarProductosDto(null, paginacion);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(0, resultado.productos().size());
    }

    @Test
    void testBuscarProductos_VerificaEstructuraCompleta() {
        // Given
        BuscarProductosDto dto = new BuscarProductosDto(null, null);

        // When
        ListaProductosDto resultado = catalogoService.buscarProductos(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.productos());
        assertNotNull(resultado.paginacion());

        resultado.productos().forEach(detalles -> {
            // Verify producto
            assertNotNull(detalles.producto());
            assertNotNull(detalles.producto().sku());
            assertNotNull(detalles.producto().nombre());
            assertNotNull(detalles.producto().precioVenta());
            assertNotNull(detalles.producto().caduca());
            assertNotNull(detalles.producto().esGranel());
            
            // Verify categoria
            assertNotNull(detalles.producto().categoria());
            
            // Verify ofertas (can be empty)
            assertNotNull(detalles.ofertas());
        });
    }
}