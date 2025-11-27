package com.isam.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.isam.dto.categoria.CategoriaDto;
import com.isam.dto.categoria.CrearCategoriaDto;
import com.isam.dto.categoria.ModificarCategoriaDto;
import com.isam.dto.comun.PaginacionResponseDto;
import com.isam.dto.oferta.OfertaDto;
import com.isam.dto.producto.BuscarProductosDto;
import com.isam.dto.producto.ConsultarProductoDto;
import com.isam.dto.producto.CrearProductoDto;
import com.isam.dto.producto.ModificarProductoDto;
import com.isam.dto.producto.DescatalogarProductoDto;
import com.isam.dto.producto.ListaProductosDto;
import com.isam.dto.producto.ListarProductosRequestDto;
import com.isam.dto.producto.ProductoDto;
import com.isam.dto.producto.RecatalogarProductoDto;
import com.isam.model.Categoria;
import com.isam.model.EstadoOferta;
import com.isam.model.EstadoProducto;
import com.isam.model.Oferta;
import com.isam.model.Producto;
import com.isam.repository.CategoriaRepository;
import com.isam.repository.OfertaRepository;
import com.isam.dto.oferta.CrearOfertaDto;
import com.isam.repository.ProductoRepository;

import io.grpc.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final ProductoRepository productoRepository;
    private final OfertaRepository ofertaRepository;
    private final CategoriaRepository categoriaRepository;
    private final EntityManager entityManager; // NOTE - Ya ni recuerdo para que servia esto. Recuerdo usarlo para hacer consultas personalizadas.

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

    @Transactional(readOnly = true)
    public ListaProductosDto listarProductos(ListarProductosRequestDto dto) {
        // Paginación por defecto
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
        
        // Crear el Pageable (Spring usa índices basados en 0)
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        // Obtener los productos paginados
        Page<Producto> productosPage = productoRepository.findAll(pageable);
        List<Producto> productos = productosPage.getContent();
        
        // Asegurar que todas las categorías estén cargadas antes de accederlas
        productos.forEach(p -> {
            if (p.getCategoria() != null) {
                Hibernate.initialize(p.getCategoria());
            }
        });
        
        // Convertir la lista de productos en formato entidad en una lista de productosCompletos (= producto + ofertas del producto) en formato DTO
        List<ListaProductosDto.DetallesProductoCompletoDto> detalles = productos.stream()
            .map(p -> {
                // Obtenemos las ofertas de cada producto
                List<Oferta> ofertas = ofertaRepository.findByProducto_Sku(p.getSku());
                
                // Convertimos la lista de ofertas en DTOs
                List<OfertaDto> ofertasDto = ofertas.stream()
                    .map(o -> new OfertaDto(
                        o.getIdOferta(),
                        o.getProducto().getSku(),
                        o.getPrecioPromocional(),
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
                    p.getPrecioVenta(),
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
                
                // retornamos un producto con sus datos y su correspondiente oferta
                return new ListaProductosDto.DetallesProductoCompletoDto(productoDto, ofertasDto);
            })
            .collect(Collectors.toList());
        
        PaginacionResponseDto paginacionDto = new PaginacionResponseDto(
            page,
            pageSize,
            productosPage.getTotalPages(),
            productosPage.getTotalElements()
        );
        
        return new ListaProductosDto(detalles, paginacionDto);
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
        // Manejamos la paginación (Igual que en listarProductos)
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
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        // Creamos la especificación con los filtros (equivalente al WHERE dinámico)
        Specification<Producto> spec = buildSpecification(dto.criterios());
        
        // Ejecutamos la consulta: Spring Data se encarga del Query de datos, el Count, el Offset y el Limit automáticamente
        Page<Producto> productosPage = productoRepository.findAll(spec, pageable);
        List<Producto> productos = productosPage.getContent();
        
        // Asegúrate de que todas las categorías estén cargadas antes accederlas (evitamos asi un LazyInitializationException).
        productos.forEach(p -> {
            if (p.getCategoria() != null) {
                Hibernate.initialize(p.getCategoria());
            }
        });
        
        // Convertimos la lista de productos en formato entidad en una lista de productosCompletos (= producto + ofertas del producto) en formato DTO. 
        List<ListaProductosDto.DetallesProductoCompletoDto> listaDetallesProductosCompletoDto = productos.stream()
            .map(p -> {
                // Obtenemos las ofertas de cara producto
                List<Oferta> ofertas = ofertaRepository.findByProducto_Sku(p.getSku());
                
                // Convertimos la lista de ofertas en DTOs
                List<OfertaDto> ofertasDto = ofertas.stream()
                    .map(o -> new OfertaDto(
                        o.getIdOferta(),
                        o.getProducto().getSku(),
                        o.getPrecioPromocional(),
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
                    p.getPrecioVenta(),
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
            productosPage.getTotalPages(),
            productosPage.getTotalElements()
        );
        
        return new ListaProductosDto(listaDetallesProductosCompletoDto, paginacionDto);
    }

    // Utils - Specification for Spring Data
    private Specification<Producto> buildSpecification(BuscarProductosDto.CriteriosBusquedaDto criterios) {
        return (root, query, cb) -> {
            List<Predicate> predicadosWhere = new ArrayList<>();
            
            if (criterios == null) {
                return cb.and(predicadosWhere.toArray(new Predicate[0]));
            }
            
            if (isNotNullOrEmpty(criterios.nombre())) {
                predicadosWhere.add(cb.like(cb.lower(root.get("nombre")),
                    "%" + criterios.nombre().toLowerCase() + "%"));
            }
            
            if (criterios.idCategoria() != null) {
                predicadosWhere.add(cb.equal(root.get("categoria").get("idCategoria"),
                    criterios.idCategoria()));
            }
            
            if (criterios.precioMin() != null) {
                predicadosWhere.add(cb.greaterThanOrEqualTo(root.get("precioVenta"),
                    criterios.precioMin()));
            }
            
            if (criterios.precioMax() != null) {
                predicadosWhere.add(cb.lessThanOrEqualTo(root.get("precioVenta"),
                    criterios.precioMax()));
            }
            
            if (criterios.esGranel() != null) {
                predicadosWhere.add(cb.equal(root.get("esGranel"), criterios.esGranel()));
            }
            
            if (criterios.etiquetas() != null && !criterios.etiquetas().isEmpty()) {
                List<Predicate> etiquetaPredicates = new ArrayList<>();
                for (String etiqueta : criterios.etiquetas()) {
                    etiquetaPredicates.add(cb.like(root.get("etiquetas"),
                        "%" + etiqueta + "%"));
                }
                predicadosWhere.add(cb.or(etiquetaPredicates.toArray(new Predicate[0])));
            }
            
            return cb.and(predicadosWhere.toArray(new Predicate[0]));
        };
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
        
        //NOTE - No es necesario hacer esta comprobación. La operación puede ser idempotente
        // // Verificar que el producto no esté ya descatalogado
        // if (producto.getEstado() == EstadoProducto.DESCATALOGADO) {
        //     throw Status.FAILED_PRECONDITION
        //         .withDescription("El producto con SKU '" + dto.sku() + "' ya está descatalogado")
        //         .asRuntimeException();
        // }
        
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
        
        //NOTE - No es necesario hacer esta comprobación. La operación puede ser idempotente.
        // // Verificar que el producto esté descatalogado
        // if (producto.getEstado() == EstadoProducto.ACTIVO) {
        //     throw Status.FAILED_PRECONDITION
        //         .withDescription("El producto con SKU '" + dto.sku() + "' ya está activo")
        //         .asRuntimeException();
        // }
        
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


    
    /**
     * Crea una oferta a partir de DTO, gestionando la búsqueda del producto y la creación de la entidad oferta.
     * Este método contiene la lógica empresarial para la creación de ofertas.
     */
    @Transactional
    public Oferta crearOferta(CrearOfertaDto dto) {
        // Verificar que el producto existe
        Producto producto = productoRepository.findBySku(dto.sku())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Producto no encontrado con SKU '" + dto.sku() + "'")
                .asRuntimeException());
        
        // Verificar que el producto está activo (lógica de negocio)
        if (producto.getEstado() == EstadoProducto.DESCATALOGADO) {
            throw Status.FAILED_PRECONDITION
                .withDescription("No se puede crear una oferta para un producto descatalogado")
                .asRuntimeException();
        }
        
        // Parsear las fechas
        LocalDate fechaInicio;
        LocalDate fechaFin;
        try {
            fechaInicio = LocalDate.parse(dto.fechaInicio());
            fechaFin = LocalDate.parse(dto.fechaFin());
        } catch (Exception e) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Error al parsear las fechas: " + e.getMessage())
                .asRuntimeException();
        }
        
        // Verificar que la fecha de fin es posterior a la fecha de inicio (lógica de negocio)
        if (fechaFin.isBefore(fechaInicio)) {
            throw Status.INVALID_ARGUMENT
                .withDescription("La fecha de fin debe ser posterior a la fecha de inicio")
                .asRuntimeException();
        }
        
        // Verificar que la fecha de inicio no es anterior a hoy (lógica de negocio)
        if (fechaInicio.isBefore(LocalDate.now())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("La fecha de inicio no puede ser anterior a hoy")
                .asRuntimeException();
        }
        
        // Verificar que el precio promocional es menor que el precio de venta (lógica de negocio)
        if (dto.precioPromocional().compareTo(producto.getPrecioVenta()) >= 0) {
            throw Status.INVALID_ARGUMENT
                .withDescription("El precio promocional debe ser menor que el precio de venta del producto")
                .asRuntimeException();
        }
        
        
        // Crear la oferta
        Oferta oferta = new Oferta();
        oferta.setProducto(producto);
        oferta.setPrecioPromocional(dto.precioPromocional());
        oferta.setTipoPromocion(dto.tipoPromocion());
        oferta.setFechaInicio(fechaInicio);
        oferta.setFechaFin(fechaFin);
        oferta.setEstado(EstadoOferta.ACTIVA);
        
        // Guardar la oferta
        Oferta ofertaGuardada = ofertaRepository.save(oferta);
        
        // Asegurar que el producto está cargado antes de usarlo (prevenir LazyInitializationException)
        if (ofertaGuardada.getProducto() != null) {
            Hibernate.initialize(ofertaGuardada.getProducto());
        }
        
        return ofertaGuardada;
    }

    /**
     * Modifica una categoría existente.
     * @param dto DTO con los datos a modificar
     * @return La categoría modificada
     */
    @Transactional
    public Categoria modificarCategoria(ModificarCategoriaDto dto) {
        // Buscar la categoría por ID
        Categoria categoria = categoriaRepository.findById(dto.idCategoria())
            .orElseThrow(() -> new EntityNotFoundException(
                "Categoría no encontrada con ID '" + dto.idCategoria() + "'"
            ));

        // Actualizar nombre si se proporciona
        if (dto.nombreCategoria() != null && !dto.nombreCategoria().isBlank()) {
            // Verificar unicidad si el nombre ha cambiado
            if (!dto.nombreCategoria().equals(categoria.getNombreCategoria()) &&
                categoriaRepository.existsByNombreCategoria(dto.nombreCategoria())) {
                throw Status.ALREADY_EXISTS
                    .withDescription("Ya existe una categoría con el nombre: " + dto.nombreCategoria())
                    .asRuntimeException();
            }
            categoria.setNombreCategoria(dto.nombreCategoria());
        }

        // Actualizar descripción si se proporciona
        if (dto.descripcion() != null) {
            categoria.setDescripcion(dto.descripcion());
        }

        // Guardar cambios
        Categoria categoriaModificada = categoriaRepository.save(categoria);
        return categoriaModificada;
    }

    /**
     * Modifica un producto existente.
     * @param dto DTO con los datos a modificar
     * @return El producto modificado
     */
    @Transactional
    public Producto modificarProducto(ModificarProductoDto dto) {
        // Buscar el producto por SKU
        Producto producto = productoRepository.findBySku(dto.sku())
            .orElseThrow(() -> new EntityNotFoundException(
                "Producto no encontrado con SKU '" + dto.sku() + "'"
            ));

        // Actualizar nombre si se proporciona
        if (dto.nombre() != null && !dto.nombre().isBlank()) {
            producto.setNombre(dto.nombre());
        }

        // Actualizar descripción si se proporciona
        if (dto.descripcion() != null) {
            producto.setDescripcion(dto.descripcion());
        }

        // Actualizar precio de venta si se proporciona
        if (dto.precioVenta() != null) {
            producto.setPrecioVenta(dto.precioVenta());
        }

        // Actualizar categoría si se proporciona
        if (dto.idCategoria() != null) {
            Optional<Categoria> categoriaOpt = categoriaRepository.findById(dto.idCategoria());
            if (categoriaOpt.isPresent()) {
                producto.setCategoria(categoriaOpt.get());
            } else {
                throw new RuntimeException("Categoría no encontrada con ID: " + dto.idCategoria());
            }
        }

        // Actualizar política de rotación si se proporciona
        if (dto.politicaRotacion() != null) {
            producto.setPoliticaRotacion(dto.politicaRotacion());
        }

        // Actualizar etiquetas si se proporcionan
        if (dto.etiquetas() != null && !dto.etiquetas().isEmpty()) {
            String etiquetasString = String.join(",", dto.etiquetas());
            producto.setEtiquetas(etiquetasString);
        }

        // Guardar cambios
        Producto productoModificado = productoRepository.save(producto);

        // Asegurar que la categoría está cargada
        if (productoModificado.getCategoria() != null) {
            Hibernate.initialize(productoModificado.getCategoria());
        }

        return productoModificado;
    }

        /**
     * Asigna etiquetas a un producto existente.
     * Las nuevas etiquetas se concatenan al final de las etiquetas existentes.
     * @param dto DTO con el SKU del producto y las nuevas etiquetas
     * @return El producto con las etiquetas actualizadas
     */
    @Transactional
    public Producto asignarEtiquetas(com.isam.dto.producto.AsignarEtiquetasDto dto) {
        // Buscar el producto por SKU
        Producto producto = productoRepository.findBySku(dto.sku())
            .orElseThrow(() -> new EntityNotFoundException(
                "Producto no encontrado con SKU '" + dto.sku() + "'"
            ));

        // Concatenar las nuevas etiquetas al final de las existentes
        String etiquetasExistentes = producto.getEtiquetas();
        String nuevasEtiquetas = String.join(",", dto.etiquetas());
        
        String etiquetasCombinadas;
        if (etiquetasExistentes != null && !etiquetasExistentes.trim().isEmpty()) {
            etiquetasCombinadas = etiquetasExistentes + "," + nuevasEtiquetas;
        } else {
            etiquetasCombinadas = nuevasEtiquetas;
        }
        
        // Validar que no se exceda el límite de 500 caracteres
        if (etiquetasCombinadas.length() > 500) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Las etiquetas exceden el límite de 500 caracteres")
                .asRuntimeException();
        }
        
        producto.setEtiquetas(etiquetasCombinadas);

        // Guardar cambios
        Producto productoActualizado = productoRepository.save(producto);

        // Asegurar que la categoría está cargada
        if (productoActualizado.getCategoria() != null) {
            Hibernate.initialize(productoActualizado.getCategoria());
        }

        return productoActualizado;
    }

    /**
     * Traduce entre identificadores de productos (SKU, EAN, PLU).
     * Dado un identificador y su tipo, devuelve los otros identificadores del mismo producto.
     * @param dto DTO con el código a traducir y su tipo
     * @return ResultadoTraduccionDto con el código de entrada y los códigos de salida
     */
    @Transactional(readOnly = true)
    public com.isam.dto.producto.ResultadoTraduccionDto traducirIdentificador(com.isam.dto.producto.TraducirIdentificadorRequestDto dto) {
        String codigo = dto.codigo();
        com.isam.dto.producto.TraducirIdentificadorRequestDto.TipoIdentificador tipoEntrada = dto.tipoIdentificador();
        Producto producto = null;
        
        // Buscar el producto según el tipo de identificador especificado
        Optional<Producto> productoOpt;
        switch (tipoEntrada) {
            case SKU:
                productoOpt = productoRepository.findBySku(codigo);
                break;
            case EAN:
                productoOpt = productoRepository.findByEan(codigo);
                break;
            case PLU:
                productoOpt = productoRepository.findByPlu(codigo);
                break;
            default:
                throw Status.INVALID_ARGUMENT
                    .withDescription("Tipo de identificador no válido: " + tipoEntrada)
                    .asRuntimeException();
        }
        
        // Si no se encontró el producto
        if (!productoOpt.isPresent()) {
            throw new EntityNotFoundException(
                "No se encontró ningún producto con el " + tipoEntrada + " '" + codigo + "'"
            );
        }
        
        producto = productoOpt.get();
        
        // Pasamos de TraducirIdentificadorRequestDto.TipoIdentificador a ResultadoTraduccionDto.TipoIdentificador
        com.isam.dto.producto.ResultadoTraduccionDto.TipoIdentificador tipoEntradaResultado =
            com.isam.dto.producto.ResultadoTraduccionDto.TipoIdentificador.valueOf(tipoEntrada.name());
        
        // Determinar el código de salida
        String codigoSalida = null;
        com.isam.dto.producto.ResultadoTraduccionDto.TipoIdentificador tipoSalida = null;
        
        switch (tipoEntrada) {
            case SKU:
                // Si entró SKU, devolvemos EAN o PLU
                if (producto.getEan() != null && !producto.getEan().trim().isEmpty()) {
                    codigoSalida = producto.getEan();
                    tipoSalida = com.isam.dto.producto.ResultadoTraduccionDto.TipoIdentificador.EAN;
                } else if (producto.getPlu() != null && !producto.getPlu().trim().isEmpty()) {
                    codigoSalida = producto.getPlu();
                    tipoSalida = com.isam.dto.producto.ResultadoTraduccionDto.TipoIdentificador.PLU;
                }
                break;
            case EAN:
                // Si entró EAN, devolvemos SKU
                codigoSalida = producto.getSku();
                tipoSalida = com.isam.dto.producto.ResultadoTraduccionDto.TipoIdentificador.SKU;
                break;
            case PLU:
                // Si entró PLU, devolvemos SKU
                codigoSalida = producto.getSku();
                tipoSalida = com.isam.dto.producto.ResultadoTraduccionDto.TipoIdentificador.SKU;
                break;
        }
        
        if (codigoSalida == null) {
            throw Status.NOT_FOUND
                .withDescription("El producto no tiene un identificador alternativo")
                .asRuntimeException();
        }
        
        return new com.isam.dto.producto.ResultadoTraduccionDto(
            codigo,
            codigoSalida,
            tipoEntradaResultado,
            tipoSalida
        );
    }
}