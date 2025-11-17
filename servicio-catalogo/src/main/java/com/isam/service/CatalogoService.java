package com.isam.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.isam.dto.categoria.CategoriaDto;
import com.isam.dto.categoria.CrearCategoriaDto;
import com.isam.dto.comun.PaginacionResponseDto;
import com.isam.dto.oferta.OfertaDto;
import com.isam.dto.producto.BuscarProductosDto;
import com.isam.dto.producto.ConsultarProductoDto;
import com.isam.dto.producto.CrearProductoDto;
import com.isam.dto.producto.DescatalogarProductoDto;
import com.isam.dto.producto.ListaProductosDto;
import com.isam.dto.producto.ProductoDto;
import com.isam.dto.producto.RecatalogarProductoDto;
import com.isam.model.Categoria;
import com.isam.model.EstadoOferta;
import com.isam.model.EstadoProducto;
import com.isam.model.Oferta;
import com.isam.model.Producto;
import com.isam.repository.CategoriaRepository;
import com.isam.repository.OfertaRepository;
import com.isam.repository.ProductoRepository;

import io.grpc.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class CatalogoService {

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private OfertaRepository ofertaRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    public CatalogoService() {
    }

    /**
     * Crea un producto a partir de DTO, gestionando la búsqueda de categorías y la creación de entidades.
     * Este método contiene la lógica empresarial para la creación de productos.
     */
    @Transactional
    public Producto crearProducto(CrearProductoDto dto) {
        System.out.println("DEBUG: Service creating product from DTO: " + dto);
        System.out.println("DEBUG: DTO PoliticaRotacion: " + dto.politicaRotacion());
        System.out.println("DEBUG: DTO UnidadMedida: " + dto.unidadMedida());

        // Comprobamos que solo tengamos ean o plu, y no los 2 a la vez.
        // NOTE - Realmente no puede darse esto porque en el contrato esta definido como "one of", asi que siempre llega solo uno aunque se hayan enviado 2 a la vez, pero como metodo defensivo dejaremos la comprobación activa.
        if (isNotNullOrEmpty(dto.ean()) && isNotNullOrEmpty(dto.plu()) ) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Un producto solo puede tener o EAN o PLU, pero no los 2 a la vez")
                .asRuntimeException();
        }

        // Creamos el producto
        Producto producto = new Producto();
        
        // Mapeo de propiedades del producto (dto -> entity)
        producto.setSku(dto.sku());
        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setPrecioVenta(dto.precioVenta());
        producto.setCaduca(dto.caduca());
        producto.setEsGranel(dto.esGranel());
        producto.setPoliticaRotacion(dto.politicaRotacion());
        producto.setUnidadMedida(dto.unidadMedida());
        producto.setEan(dto.ean());
        producto.setPlu(dto.plu());
        
        System.out.println("DEBUG: After mapping - Entity PoliticaRotacion: " + producto.getPoliticaRotacion());
        System.out.println("DEBUG: After mapping - Entity UnidadMedida: " + producto.getUnidadMedida());
        
        // Manejar etiquetas
        if (dto.etiquetas() != null && !dto.etiquetas().isEmpty()) {
            String etiquetasString = String.join(",", dto.etiquetas());
            producto.setEtiquetas(etiquetasString);
            System.out.println("DEBUG: Etiquetas set: " + etiquetasString);
        }
        
        // Gestionar la búsqueda de categorías: esta es la responsabilidad de la capa de servicio.
        if (dto.idCategoria() != null && dto.idCategoria() > 0) {
            System.out.println("DEBUG: Looking up category with ID: " + dto.idCategoria());
            Optional<Categoria> categoriaOpt = categoriaRepository.findById(dto.idCategoria());
            if (categoriaOpt.isPresent()) {
                producto.setCategoria(categoriaOpt.get());
                System.out.println("DEBUG: Category found and set: " + categoriaOpt.get().getNombreCategoria());
            } else {
                System.err.println("ERROR: Category not found for ID: " + dto.idCategoria());
                throw new RuntimeException("Category not found with ID: " + dto.idCategoria());
            }
        } else {
            System.out.println("DEBUG: No category ID provided");
        }

        // Guardar producto
        System.out.println("DEBUG: Before saving - Entity PoliticaRotacion: " + producto.getPoliticaRotacion());
        System.out.println("DEBUG: Before saving - Entity UnidadMedida: " + producto.getUnidadMedida());
        
        Producto savedProduct = productoRepository.save(producto);
        
        // Asegurar que la cetegoría está cargada antes de usarla (prevenir LazyInitializationException)
        if (savedProduct.getCategoria() != null) {
            System.out.println("DEBUG: Is categoria proxy initialized before access: " + Hibernate.isInitialized(savedProduct.getCategoria()));
            // Access categoria properties to force loading within the transaction
            String categoriaNombre = savedProduct.getCategoria().getNombreCategoria();
            System.out.println("DEBUG: Category eagerly loaded: " + categoriaNombre);
        }
        
        System.out.println("DEBUG: After saving - Entity PoliticaRotacion: " + savedProduct.getPoliticaRotacion());
        System.out.println("DEBUG: After saving - Entity UnidadMedida: " + savedProduct.getUnidadMedida());
        System.out.println("DEBUG: Product saved successfully with category: " +
                          (savedProduct.getCategoria() != null ? savedProduct.getCategoria().getNombreCategoria() : "null"));

        return savedProduct;
    }

    @Transactional
    public Producto consultarProducto(ConsultarProductoDto consultarProductoDto){
        Producto productoEntity = productoRepository.findBySku(consultarProductoDto.sku())
        .orElseThrow(() -> new EntityNotFoundException(
            "Producto no encontrado con SKU '" + consultarProductoDto.sku() + "'"
        ));
        return productoEntity;
    }

    @Transactional
    public Categoria crearCategoria(CrearCategoriaDto dto){
        // Validar el DTO
        if (dto.nombreCategoria() == null || dto.nombreCategoria().isBlank()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El nombre de la categoría es requerido")
                .asRuntimeException();
        }

        // Verificar duplicado ANTES de crear la entidad
        if (categoriaRepository.existsByNombreCategoria(dto.nombreCategoria())) {
            throw Status.ALREADY_EXISTS
                .withDescription("Ya existe una categoría con el nombre: " + dto.nombreCategoria())
                .asRuntimeException();
        }

        Categoria categoria = new Categoria();
        categoria.setNombreCategoria(dto.nombreCategoria());
        categoria.setDescripcion(dto.descripcion());

        Categoria categoriaRespEntity = categoriaRepository.save(categoria);
        return categoriaRespEntity;
    }

    @Transactional(readOnly = true)
    public ListaProductosDto buscarProductos(BuscarProductosDto dto) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Producto> cqSQL = cb.createQuery(Producto.class); // equivalente a una query SQL
        Root<Producto> productoFrom = cqSQL.from(Producto.class); // el equivalente a la tabla principal del FROM PRODUCTO
        
        List<Predicate> predicatesWhere = buildPredicates(cb, productoFrom, dto.criterios()); // equivalente a una colección de condiciones WHERE que vas acumulando dinámicamente según los filtros del DTO
        
        cqSQL.where(predicatesWhere.toArray(new Predicate[0])); // Convertimos la lista de predicados en un array de predicados y se lo pasamos a la query para que lo consuma
        
        TypedQuery<Producto> PREquery = entityManager.createQuery(cqSQL); // es un objeto que representa una consulta ya preparada pero aún no ejecutada, y que además garantiza el tipo del resultado
        
        // Manejamos la paginación
        Integer page = 1;
        Integer pageSize = 10;
        
        if (dto.paginacion() != null) {
            if (dto.paginacion().page() != null) {
                page = dto.paginacion().page();
            }
            if (dto.paginacion().pageSize() != null) {
                pageSize = dto.paginacion().pageSize();
            }
        }
        
        // Contamos el total de elementos - crear nuevos predicados "when" para contar cuantos productos en total tenemos al filtrar
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Producto> countRoot = countQuery.from(Producto.class);
        countQuery.select(cb.count(countRoot));
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, dto.criterios());
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();
        
        // Calculamos la paginacion
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        int offset = (page - 1) * pageSize;
        
        PREquery.setFirstResult(offset);
        PREquery.setMaxResults(pageSize);
        
        List<Producto> productos = PREquery.getResultList();
        
        // Asegúrate de que todas las categorías estén cargadas antes accederlas (evitamos asi un LazyInitializationException).
        productos.forEach(p -> {
            if (p.getCategoria() != null) {
                Hibernate.initialize(p.getCategoria());
            }
        });
        
        // Convertimos la lista de productos en formato entidad en una lista de productosCompletos (= producto + ofertas del producto) en formato DTO. 
        List<ListaProductosDto.DetallesProductoCompletoDto> detalles = productos.stream()
            .map(p -> {
                // Obtenemos las ofertas de cara producto
                List<Oferta> ofertas = ofertaRepository.findByProducto_Sku(p.getSku());
                
                // Convertimos la lista de ofertas en DTOs
                List<OfertaDto> ofertasDto = ofertas.stream()
                    .map(o -> new OfertaDto(
                        o.getIdOferta(),
                        o.getProducto().getSku(),
                        o.getPrecioPromocional().doubleValue(),
                        o.getTipoPromocion(),
                        o.getFechaInicio().toString(),
                        o.getFechaFin().toString(),
                        o.getEstado().name()
                    ))
                    .collect(Collectors.toList());
                
                // Convertimos el producto en DTO
                ProductoDto productoDto = new ProductoDto(
                    p.getSku(),
                    p.getEan(),
                    p.getPlu(),
                    p.getNombre(),
                    p.getDescripcion(),
                    p.getPrecioVenta().doubleValue(),
                    p.getCaduca(),
                    p.getEsGranel(),
                    p.getCategoria() != null ? new CategoriaDto(
                        p.getCategoria().getIdCategoria(),
                        p.getCategoria().getNombreCategoria(),
                        p.getCategoria().getDescripcion()
                    ) : null,
                    p.getPoliticaRotacion() != null ? p.getPoliticaRotacion().name() : null,
                    p.getUnidadMedida() != null ? p.getUnidadMedida().name() : null,
                    p.getEtiquetas() != null ? Arrays.asList(p.getEtiquetas().split(",")) : null,
                    p.getEstado().name()
                );
                
                // retornamos un un producto con sus datos y su correspondiente oferta.
                return new ListaProductosDto.DetallesProductoCompletoDto(productoDto, ofertasDto);
            })
            .collect(Collectors.toList());
        
        PaginacionResponseDto paginacionDto = new PaginacionResponseDto(
            page,
            pageSize,
            totalPages,
            totalElements
        );
        
        return new ListaProductosDto(detalles, paginacionDto);
    }

    // Utils
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Producto> productoFrom,
                                           BuscarProductosDto.CriteriosBusquedaDto criterios) {
        List<Predicate> predicatesWhere = new ArrayList<>();
        
        if (criterios == null) {
            return predicatesWhere;
        }
        
        if (isNotNullOrEmpty(criterios.nombre())) {
            predicatesWhere.add(cb.like(cb.lower(productoFrom.get("nombre")),
                "%" + criterios.nombre().toLowerCase() + "%"));
        }
        
        if (criterios.idCategoria() != null) {
            predicatesWhere.add(cb.equal(productoFrom.get("categoria").get("idCategoria"),
                criterios.idCategoria()));
        }
        
        if (criterios.precioMin() != null) {
            predicatesWhere.add(cb.greaterThanOrEqualTo(productoFrom.get("precioVenta"),
                criterios.precioMin()));
        }
        
        if (criterios.precioMax() != null) {
            predicatesWhere.add(cb.lessThanOrEqualTo(productoFrom.get("precioVenta"),
                criterios.precioMax()));
        }
        
        if (criterios.esGranel() != null) {
            predicatesWhere.add(cb.equal(productoFrom.get("esGranel"), criterios.esGranel()));
        }
        
        if (criterios.etiquetas() != null && !criterios.etiquetas().isEmpty()) {
            List<Predicate> etiquetaPredicates = new ArrayList<>();
            for (String etiqueta : criterios.etiquetas()) {
                etiquetaPredicates.add(cb.like(productoFrom.get("etiquetas"),
                    "%" + etiqueta + "%"));
            }
            predicatesWhere.add(cb.or(etiquetaPredicates.toArray(new Predicate[0])));
        }
        
        return predicatesWhere;
    }
    
    /**
     * Descataloga un producto cambiando su estado a DESCATALOGADO.
     * @param dto DTO con el SKU del producto a descatalogar
     * @return El producto descatalogado
     */
    @Transactional
    public Producto descatalogarProducto(DescatalogarProductoDto dto) {
        Producto producto = productoRepository.findBySku(dto.sku())
            .orElseThrow(() -> new EntityNotFoundException(
                "Producto no encontrado con SKU '" + dto.sku() + "'"
            ));
        
        // Verificar que el producto no esté ya descatalogado
        if (producto.getEstado() == EstadoProducto.DESCATALOGADO) {
            throw Status.FAILED_PRECONDITION
                .withDescription("El producto con SKU '" + dto.sku() + "' ya está descatalogado")
                .asRuntimeException();
        }
        
        // Cambiar el estado a DESCATALOGADO
        producto.setEstado(EstadoProducto.DESCATALOGADO);
        
        Producto productoDescatalogado = productoRepository.save(producto);
        
        // Asegurar que la categoría está cargada
        if (productoDescatalogado.getCategoria() != null) {
            Hibernate.initialize(productoDescatalogado.getCategoria());
        }
        
        return productoDescatalogado;
    }
    
    /**
     * Recataloga un producto cambiando su estado a ACTIVO.
     * @param dto DTO con el SKU del producto a recatalogar
     * @return El producto recatalogado
     */
    @Transactional
    public Producto recatalogarProducto(RecatalogarProductoDto dto) {
        Producto producto = productoRepository.findBySku(dto.sku())
            .orElseThrow(() -> new EntityNotFoundException(
                "Producto no encontrado con SKU '" + dto.sku() + "'"
            ));
        
        // Verificar que el producto esté descatalogado
        if (producto.getEstado() == EstadoProducto.ACTIVO) {
            throw Status.FAILED_PRECONDITION
                .withDescription("El producto con SKU '" + dto.sku() + "' ya está activo")
                .asRuntimeException();
        }
        
        // Cambiar el estado a ACTIVO
        producto.setEstado(EstadoProducto.ACTIVO);
        
        Producto productoRecatalogado = productoRepository.save(producto);
        
        // Asegurar que la categoría está cargada
        if (productoRecatalogado.getCategoria() != null) {
            Hibernate.initialize(productoRecatalogado.getCategoria());
        }
        
        return productoRecatalogado;
    }
    
    private boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
