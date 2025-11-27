package com.isam.mapper;

import com.isam.dto.categoria.CrearCategoriaDto;
import com.isam.dto.categoria.ModificarCategoriaDto;
import com.isam.dto.comun.PaginacionDto;
import com.isam.dto.oferta.OfertaDto;
import com.isam.dto.producto.BuscarProductosDto;
import com.isam.dto.producto.ConsultarProductoDto;
import com.isam.dto.producto.CrearProductoDto;
import com.isam.dto.producto.ModificarProductoDto;
import com.isam.dto.producto.DescatalogarProductoDto;
import com.isam.dto.producto.ListaProductosDto;
import com.isam.dto.producto.ListarProductosRequestDto;
import com.isam.dto.producto.ProductoDto;
import com.isam.dto.oferta.CrearOfertaDto;
import com.isam.dto.producto.RecatalogarProductoDto;
import com.isam.grpc.catalogo.BuscarProductosRequest;
import com.isam.grpc.catalogo.ListarProductosRequest;
import com.isam.grpc.catalogo.ModificarCategoriaRequest;
import com.isam.grpc.catalogo.ModificarProductoRequest;
import com.isam.grpc.catalogo.CategoriaProto;
import com.isam.grpc.catalogo.ConsultarProductoRequest;
import com.isam.grpc.catalogo.CrearCategoriaRequest;
import com.isam.grpc.catalogo.CrearProductoRequest;
import com.isam.grpc.catalogo.DatosActualizar;
import com.isam.grpc.catalogo.CrearOfertaRequest;
import com.isam.grpc.catalogo.DescatalogarProductoRequest;
import com.isam.grpc.catalogo.RecatalogarProductoRequest;
import com.isam.grpc.catalogo.ListaProductos;
import com.isam.grpc.catalogo.DetallesProductoCompleto;
import com.isam.grpc.catalogo.OfertaProto;
import com.isam.grpc.common.PaginationResponse;
import com.isam.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class CatalogoMapper {

    /**
     * Convierte gRPC CrearProductoRequest a DTO.
     * Este método solo gestiona la asignación entre protocolos, no la lógica empresarial.
     */
    public CrearProductoDto toDto(CrearProductoRequest req) {
        System.out.println("DEBUG: Converting gRPC request to DTO");
        System.out.println("DEBUG: Request - SKU: " + req.getSku());
        System.out.println("DEBUG: Request - id_categoria: " + req.getIdCategoria());
        System.out.println("DEBUG: Request - etiquetas count: " + req.getEtiquetasList().size());
        
        // Extraer valores para el constructor de registros
        String ean = req.hasEan() ? req.getEan() : null;
        String plu = req.hasPlu() ? req.getPlu() : null;
        if (req.hasPlu()) {
            System.out.println("DEBUG: Valor del PLU: '" + plu + "', tamanyo: " + plu.length());
        }
        
        Long idCategoria = req.getIdCategoria() > 0 ? req.getIdCategoria() : null;
        if (idCategoria != null) {
            System.out.println("DEBUG: Category ID set in DTO: " + idCategoria);
        }
        
        List<String> etiquetas = !req.getEtiquetasList().isEmpty() ? req.getEtiquetasList() : null;
        if (etiquetas != null) {
            System.out.println("DEBUG: Etiquetas set in DTO: " + etiquetas);
        }
        
        // Crear registro utilizando el constructor
        CrearProductoDto dto = new CrearProductoDto(
            req.getSku(),
            ean,
            plu,
            req.getNombre(),
            req.getDescripcion(),
            new BigDecimal(req.getPrecioVenta()),
            req.getCaduca(),
            req.getEsGranel(),
            idCategoria,
            mapPoliticaRotacion(req.getPoliticaRotacion()),
            mapUnidadMedida(req.getUnidadMedida()),
            etiquetas
        );

        System.out.println("DEBUG: DTO created successfully: " + dto);
        return dto;
    }


    public ConsultarProductoDto toDto(ConsultarProductoRequest req){
        return new ConsultarProductoDto(req.getSku());
    }

    public BuscarProductosDto toDto(BuscarProductosRequest req) {
        BuscarProductosDto.CriteriosBusquedaDto criterios = null;
        
        if (req.hasCriterios()) {
            criterios = new BuscarProductosDto.CriteriosBusquedaDto(
                req.getCriterios().hasNombre() ? req.getCriterios().getNombre() : null,
                req.getCriterios().getIdCategoria() > 0 ? req.getCriterios().getIdCategoria() : null,
                req.getCriterios().hasPrecioMin() ? new BigDecimal(req.getCriterios().getPrecioMin()) : null,
                req.getCriterios().hasPrecioMax() ? new BigDecimal(req.getCriterios().getPrecioMax()) : null,
                req.getCriterios().hasEsGranel() ? req.getCriterios().getEsGranel() : null,
                !req.getCriterios().getEtiquetasList().isEmpty() ? req.getCriterios().getEtiquetasList() : null
            );
        }
        
        PaginacionDto paginacion = null;
        if (req.hasPaginacion()) {
            paginacion = new PaginacionDto(
                req.getPaginacion().getPage() > 0 ? req.getPaginacion().getPage() : null,
                req.getPaginacion().getPageSize() > 0 ? req.getPaginacion().getPageSize() : null
            );
        }
        
        return new BuscarProductosDto(criterios, paginacion);
    }

    public ListarProductosRequestDto toDto(ListarProductosRequest req) {
        PaginacionDto paginacion = null;
        if (req.hasPaginacion()) {
            paginacion = new PaginacionDto(
                req.getPaginacion().getPage() > 0 ? req.getPaginacion().getPage() : null,
                req.getPaginacion().getPageSize() > 0 ? req.getPaginacion().getPageSize() : null
            );
        }
        
        return new ListarProductosRequestDto(paginacion);
    }

    /**
     * Convierte gRPC DescatalogarProductoRequest a DTO.
     */
    public DescatalogarProductoDto toDto(DescatalogarProductoRequest req) {
        return new DescatalogarProductoDto(req.getSku());
    }

    /**
     * Convierte gRPC RecatalogarProductoRequest a DTO.
     */
    public RecatalogarProductoDto toDto(RecatalogarProductoRequest req) {
        return new RecatalogarProductoDto(req.getSku());
    }

    /**
     * Convierte DTO a Entidad.
     * Nota: Categoria será nulo aquí; debe ser rellenado por la capa de servicio.
     */
    public Producto toEntity(CrearProductoDto dto) {
        System.out.println("DEBUG: Converting DTO to Entity");
        System.out.println("DEBUG: DTO: " + dto);
        
        Producto entity = new Producto();

        // Propiedades fundamentales
        entity.setSku(dto.sku());
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        entity.setPrecioVenta(dto.precioVenta());
        entity.setCaduca(dto.caduca());
        entity.setEsGranel(dto.esGranel());

        // Map enums
        entity.setPoliticaRotacion(dto.politicaRotacion());
        entity.setUnidadMedida(dto.unidadMedida());

        // Manejar identificadores ean y plu del producto
        entity.setEan(dto.ean());
        entity.setPlu(dto.plu());

        // Establecemos un estado por defecto 
        //NOTE - Deberia establecerlo por defecto asi?
        entity.setEstado(EstadoProducto.ACTIVO);

        // Manejar etiquetas
        if (dto.etiquetas() != null && !dto.etiquetas().isEmpty()) {
            String etiquetasString = String.join(",", dto.etiquetas());
            entity.setEtiquetas(etiquetasString);
            System.out.println("DEBUG: Etiquetas set in entity: " + etiquetasString);
        }

        // NOTA: Categoria será nulo aquí. Es responsabilidad de la capa de servicio rellenarlo.
        System.out.println("DEBUG: Entity created (without categoria): " + entity);
        return entity;
    }

    private PoliticaRotacion mapPoliticaRotacion(com.isam.grpc.common.PoliticaRotacion protoEnum) {
        System.out.println("DEBUG: Mapping PoliticaRotacion from proto: " + protoEnum);       
        try {
            System.out.println("DEBUG: Enum ordinal: " + protoEnum.getNumber());
        } catch (IllegalArgumentException e) {
            System.out.println("DEBUG: Cannot get number for enum " + protoEnum + ", treating as UNRECOGNIZED");
            return PoliticaRotacion.FIFO; // Por defecto, FIFO para valores enumerados problemáticos.
        }
        
        switch (protoEnum) {
            case FIFO:
                System.out.println("DEBUG: Mapped to FIFO");
                return PoliticaRotacion.FIFO;
            case FEFO:
                System.out.println("DEBUG: Mapped to FEFO");
                return PoliticaRotacion.FEFO;
            case LIFO:
                System.out.println("DEBUG: Mapped to LIFO");
                return PoliticaRotacion.LIFO;
            case UNRECOGNIZED:
                return PoliticaRotacion.POLITICA_ROTACION_UNSPECIFIED;
            default:
                System.out.println("DEBUG: Unmapped enum value: " + protoEnum + ", defaulting to FIFO");
                return PoliticaRotacion.FIFO; // Por defecto, FIFO en lugar de NO ESPECIFICADO.
        }
    }

    private UnidadMedida mapUnidadMedida(com.isam.grpc.common.UnidadMedida protoEnum) {
        switch (protoEnum) {
            case UNIDAD:
                return UnidadMedida.UNIDAD;
            case KILOGRAMO:
                return UnidadMedida.KILOGRAMO;
            case GRAMO:
                return UnidadMedida.GRAMO;
            case LITRO:
                return UnidadMedida.LITRO;
            case MILILITRO:
                return UnidadMedida.MILILITRO;
            case METRO:
                return UnidadMedida.METRO;
            case PAQUETE:
                return UnidadMedida.PAQUETE;
            case DOCENA:
                return UnidadMedida.DOCENA;
            default:
                return UnidadMedida.UNIDAD_MEDIDA_UNSPECIFIED;
        }
    }

    public com.isam.grpc.catalogo.ProductoProto toProto(Producto entity) {
        System.out.println("DEBUG: Converting entity to proto. PoliticaRotacion: " + entity.getPoliticaRotacion());
        System.out.println("DEBUG: Entity details: " + entity);

        com.isam.grpc.common.PoliticaRotacion protoRotacion = mapPoliticaRotacionToProto(entity.getPoliticaRotacion());
        System.out.println("DEBUG: Mapped PoliticaRotacion to proto: " + protoRotacion);

        com.isam.grpc.catalogo.ProductoProto.Builder builder = com.isam.grpc.catalogo.ProductoProto.newBuilder()
                .setSku(entity.getSku())
                .setNombre(entity.getNombre())
                .setPrecioVenta(formatPrecio(entity.getPrecioVenta()))
                .setCaduca(entity.getCaduca())
                .setEsGranel(entity.getEsGranel())
                .setPoliticaRotacion(protoRotacion)
                .setUnidadMedida(mapUnidadMedidaToProto(entity.getUnidadMedida()))
                .setEstado(mapEstadoProductoToProto(entity.getEstado()))
                .addAllEtiquetas(parseEtiquetas(entity.getEtiquetas()));

        if (entity.getDescripcion() != null) {
            builder.setDescripcion(entity.getDescripcion());
        }

        if (entity.getCategoria() != null) {
            CategoriaProto catProto = toProto(entity.getCategoria());
            builder.setCategoria(catProto);
        }

        // Handle oneof identificador
        if (entity.getEan() != null) {
            builder.setEan(entity.getEan());
        } else if (entity.getPlu() != null) {
            builder.setPlu(entity.getPlu());
        }

        return builder.build();
    }


    public CrearCategoriaDto toDto(CrearCategoriaRequest crearCategoriaRequest){

        return new CrearCategoriaDto(
            crearCategoriaRequest.getNombreCategoria(),
            crearCategoriaRequest.getDescripcion()
        );
    }

    public Categoria toEntity(CrearCategoriaDto dto){

        Categoria categoria = new Categoria();
        categoria.setNombreCategoria(dto.nombreCategoria());
        categoria.setDescripcion(dto.descripcion());

        return categoria;
    }


    public CategoriaProto toProto(Categoria categoria){

        CategoriaProto categoriaProto = CategoriaProto.newBuilder()
                .setDescripcion(categoria.getDescripcion())
                .setIdCategoria(categoria.getIdCategoria())
                .setNombreCategoria(categoria.getNombreCategoria())
                .build();

        return categoriaProto;
    }


    private com.isam.grpc.common.PoliticaRotacion mapPoliticaRotacionToProto(PoliticaRotacion entityEnum) {
        System.out.println("DEBUG: Converting entity enum to proto. Input: " + entityEnum);
        if (entityEnum == null) {
            System.out.println("DEBUG: Entity enum is NULL! This will cause NPE");
            return com.isam.grpc.common.PoliticaRotacion.UNRECOGNIZED; // Return a default instead of null
        }
        
        switch (entityEnum) {
            case FIFO:
                System.out.println("DEBUG: Converting FIFO to proto");
                return com.isam.grpc.common.PoliticaRotacion.FIFO;
            case FEFO:
                System.out.println("DEBUG: Converting FEFO to proto");
                return com.isam.grpc.common.PoliticaRotacion.FEFO;
            case LIFO:
                System.out.println("DEBUG: Converting LIFO to proto");
                return com.isam.grpc.common.PoliticaRotacion.LIFO;
            case POLITICA_ROTACION_UNSPECIFIED:
                System.out.println("DEBUG: Converting UNSPECIFIED to proto");
                return com.isam.grpc.common.PoliticaRotacion.UNRECOGNIZED;
            default:
                System.out.println("DEBUG: Unknown enum value: " + entityEnum + ", returning UNRECOGNIZED");
                return com.isam.grpc.common.PoliticaRotacion.UNRECOGNIZED; // Return a default instead of null
        }
    }

    private com.isam.grpc.common.UnidadMedida mapUnidadMedidaToProto(UnidadMedida entityEnum) {
        switch (entityEnum) {
            case UNIDAD:
                return com.isam.grpc.common.UnidadMedida.UNIDAD;
            case KILOGRAMO:
                return com.isam.grpc.common.UnidadMedida.KILOGRAMO;
            case GRAMO:
                return com.isam.grpc.common.UnidadMedida.GRAMO;
            case LITRO:
                return com.isam.grpc.common.UnidadMedida.LITRO;
            case MILILITRO:
                return com.isam.grpc.common.UnidadMedida.MILILITRO;
            case METRO:
                return com.isam.grpc.common.UnidadMedida.METRO;
            case PAQUETE:
                return com.isam.grpc.common.UnidadMedida.PAQUETE;
            case DOCENA:
                return com.isam.grpc.common.UnidadMedida.DOCENA;
            default:
                return com.isam.grpc.common.UnidadMedida.UNIDAD_MEDIDA_UNSPECIFIED;
        }
    }

    private com.isam.grpc.common.EstadoProducto mapEstadoProductoToProto(EstadoProducto entityEnum) {
        switch (entityEnum) {
            case ACTIVO:
                return com.isam.grpc.common.EstadoProducto.ACTIVO;
            case DESCATALOGADO:
                return com.isam.grpc.common.EstadoProducto.DESCATALOGADO;
            default:
                return com.isam.grpc.common.EstadoProducto.UNRECOGNIZED; //TODO - Mejor tirar un error
        }
    }

    private List<String> parseEtiquetas(String etiquetas) {
        if (etiquetas == null || etiquetas.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(etiquetas.split(","));
    }

    public ListaProductos toProto(ListaProductosDto dto) {
        ListaProductos.Builder builder = ListaProductos.newBuilder();
        
        // Map productos
        if (dto.productos() != null) {
            List<DetallesProductoCompleto> detallesProto = dto.productos().stream()
                .map(this::toDetallesProductoCompletoProto)
                .collect(Collectors.toList());
            builder.addAllProductos(detallesProto);
        }
        
        // Map paginacion
        if (dto.paginacion() != null) {
            PaginationResponse paginacionProto = PaginationResponse.newBuilder()
                .setPage(dto.paginacion().page())
                .setPageSize(dto.paginacion().pageSize())
                .setTotalPages(dto.paginacion().totalPages())
                .setTotalElements(dto.paginacion().totalElements())
                .build();
            builder.setPaginacion(paginacionProto);
        }
        
        return builder.build();
    }

    private DetallesProductoCompleto toDetallesProductoCompletoProto(ListaProductosDto.DetallesProductoCompletoDto dto) {
        DetallesProductoCompleto.Builder builder = DetallesProductoCompleto.newBuilder();
        
        // Map producto
        if (dto.producto() != null) {
            builder.setProducto(toProductoProto(dto.producto()));
        }
        
        // Map ofertas
        if (dto.ofertas() != null) {
            List<OfertaProto> ofertasProto = dto.ofertas().stream()
                .map(this::toOfertaProto)
                .collect(Collectors.toList());
            builder.addAllOferta(ofertasProto);
        }
        
        return builder.build();
    }

    private com.isam.grpc.catalogo.ProductoProto toProductoProto(ProductoDto dto) {
        com.isam.grpc.catalogo.ProductoProto.Builder builder = com.isam.grpc.catalogo.ProductoProto.newBuilder()
            .setSku(dto.sku())
            .setNombre(dto.nombre())
            .setPrecioVenta(formatPrecio(dto.precioVenta()))
            .setCaduca(dto.caduca())
            .setEsGranel(dto.esGranel());
        
        if (dto.ean() != null) {
            builder.setEan(dto.ean());
        } else if (dto.plu() != null) {
            builder.setPlu(dto.plu());
        }
        
        if (dto.descripcion() != null) {
            builder.setDescripcion(dto.descripcion());
        }
        
        if (dto.categoria() != null) {
            CategoriaProto categoriaProto = CategoriaProto.newBuilder()
                .setIdCategoria(dto.categoria().idCategoria())
                .setNombreCategoria(dto.categoria().nombreCategoria())
                .setDescripcion(dto.categoria().descripcion() != null ? dto.categoria().descripcion() : "")
                .build();
            builder.setCategoria(categoriaProto);
        }
        
        if (dto.politicaRotacion() != null) {
            builder.setPoliticaRotacion(mapStringToPoliticaRotacionProto(dto.politicaRotacion()));
        }
        
        if (dto.unidadMedida() != null) {
            builder.setUnidadMedida(mapStringToUnidadMedidaProto(dto.unidadMedida()));
        }
        
        if (dto.etiquetas() != null) {
            builder.addAllEtiquetas(dto.etiquetas());
        }
        
        if (dto.estado() != null) {
            builder.setEstado(mapStringToEstadoProductoProto(dto.estado()));
        }
        
        return builder.build();
    }

    private OfertaProto toOfertaProto(OfertaDto dto) {
        OfertaProto.Builder builder = OfertaProto.newBuilder()
            .setIdOferta(dto.idOferta())
            .setSku(dto.sku())
            .setPrecioPromocional(formatPrecio(dto.precioPromocional()))
            .setTipoPromocion(dto.tipoPromocion())
            .setFechaInicio(dto.fechaInicio())
            .setFechaFin(dto.fechaFin());
        
        if (dto.estado() != null) {
            builder.setEstado(mapStringToEstadoOfertaProto(dto.estado()));
        }
        
        return builder.build();
    }

    private com.isam.grpc.common.PoliticaRotacion mapStringToPoliticaRotacionProto(String value) {
        switch (value) {
            case "FIFO":
                return com.isam.grpc.common.PoliticaRotacion.FIFO;
            case "FEFO":
                return com.isam.grpc.common.PoliticaRotacion.FEFO;
            case "LIFO":
                return com.isam.grpc.common.PoliticaRotacion.LIFO;
            default:
                return com.isam.grpc.common.PoliticaRotacion.UNRECOGNIZED;
        }
    }

    private com.isam.grpc.common.UnidadMedida mapStringToUnidadMedidaProto(String value) {
        switch (value) {
            case "UNIDAD":
                return com.isam.grpc.common.UnidadMedida.UNIDAD;
            case "KILOGRAMO":
                return com.isam.grpc.common.UnidadMedida.KILOGRAMO;
            case "GRAMO":
                return com.isam.grpc.common.UnidadMedida.GRAMO;
            case "LITRO":
                return com.isam.grpc.common.UnidadMedida.LITRO;
            case "MILILITRO":
                return com.isam.grpc.common.UnidadMedida.MILILITRO;
            case "METRO":
                return com.isam.grpc.common.UnidadMedida.METRO;
            case "PAQUETE":
                return com.isam.grpc.common.UnidadMedida.PAQUETE;
            case "DOCENA":
                return com.isam.grpc.common.UnidadMedida.DOCENA;
            default:
                return com.isam.grpc.common.UnidadMedida.UNIDAD_MEDIDA_UNSPECIFIED;
        }
    }

    private com.isam.grpc.common.EstadoProducto mapStringToEstadoProductoProto(String value) {
        switch (value) {
            case "ACTIVO":
                return com.isam.grpc.common.EstadoProducto.ACTIVO;
            case "DESCATALOGADO":
                return com.isam.grpc.common.EstadoProducto.DESCATALOGADO;
            default:
                return com.isam.grpc.common.EstadoProducto.UNRECOGNIZED;
        }
    }

    private com.isam.grpc.common.EstadoOferta mapStringToEstadoOfertaProto(String value) {
        switch (value) {
            case "ACTIVA":
                return com.isam.grpc.common.EstadoOferta.ACTIVA;
            case "VENCIDA":
                return com.isam.grpc.common.EstadoOferta.VENCIDA;
            case "CANCELADA":
                return com.isam.grpc.common.EstadoOferta.CANCELADA;
            default:
                return com.isam.grpc.common.EstadoOferta.UNRECOGNIZED;
        }
    }
    
    /**
     * Convierte gRPC CrearOfertaRequest a DTO.
     */
    public CrearOfertaDto toDto(CrearOfertaRequest req) {
        return new CrearOfertaDto(
            req.getSku(),
            new BigDecimal(req.getPrecioPromocional()),
            req.getTipoPromocion(),
            req.getFechaInicio(),
            req.getFechaFin()
        );
    }

    /**
     * Convierte entidad Oferta a proto OfertaProto.
     */
    public OfertaProto toProto(Oferta oferta) {
        OfertaProto.Builder builder = OfertaProto.newBuilder()
            .setIdOferta(oferta.getIdOferta())
            .setSku(oferta.getProducto().getSku())
            .setPrecioPromocional(formatPrecio(oferta.getPrecioPromocional()))
            .setTipoPromocion(oferta.getTipoPromocion())
            .setFechaInicio(oferta.getFechaInicio().toString())
            .setFechaFin(oferta.getFechaFin().toString())
            .setEstado(mapEstadoOfertaToProto(oferta.getEstado()));
        
        return builder.build();
    }

    private com.isam.grpc.common.EstadoOferta mapEstadoOfertaToProto(EstadoOferta entityEnum) {
        switch (entityEnum) {
            case ACTIVA:
                return com.isam.grpc.common.EstadoOferta.ACTIVA;
            case VENCIDA:
                return com.isam.grpc.common.EstadoOferta.VENCIDA;
            case CANCELADA:
                return com.isam.grpc.common.EstadoOferta.CANCELADA;
            default:
                return com.isam.grpc.common.EstadoOferta.UNRECOGNIZED;
        }
    }



    /**
     * Convierte gRPC ModificarCategoriaRequest a DTO.
     */
    public ModificarCategoriaDto toDto(ModificarCategoriaRequest req) {
        return new ModificarCategoriaDto(
            req.getIdCategoria(),
            req.hasNombreCategoria() ? req.getNombreCategoria() : null,
            req.hasDescripcion() ? req.getDescripcion() : null
        );
    }

    /**
     * Convierte gRPC ModificarProductoRequest a DTO.
     */
    public ModificarProductoDto toDto(ModificarProductoRequest req) {
        DatosActualizar datos = req.getDatosActualizar();
        
        List<String> etiquetas = !datos.getEtiquetasList().isEmpty() ? datos.getEtiquetasList() : null;
        
        return new ModificarProductoDto(
            req.getSku(),
            datos.hasNombre() ? datos.getNombre() : null,
            datos.hasDescripcion() ? datos.getDescripcion() : null,
            datos.hasPrecioVenta() ? new BigDecimal(datos.getPrecioVenta()) : null,
            datos.hasIdCategoria() ? datos.getIdCategoria() : null,
            datos.hasPoliticaRotacion() ? mapPoliticaRotacion(datos.getPoliticaRotacion()) : null,
            etiquetas
        );
    }

    
    /**
     * Convierte gRPC AsignarEtiquetasRequest a DTO.
     */
    public com.isam.dto.producto.AsignarEtiquetasDto toDto(com.isam.grpc.catalogo.AsignarEtiquetasRequest req) {
        return new com.isam.dto.producto.AsignarEtiquetasDto(
            req.getSku(),
            req.getEtiquetasList()
        );
    }

    /**
     * Convierte gRPC TraducirIdentificadorRequest a DTO.
     */
    public com.isam.dto.producto.TraducirIdentificadorRequestDto toDto(com.isam.grpc.catalogo.TraducirIdentificadorRequest req) {
        String codigo = null;
        com.isam.dto.producto.TraducirIdentificadorRequestDto.TipoIdentificador tipo = null;
        
        // Extraer el código y tipo del oneof
        switch (req.getCodigoCase()) {
            case SKU:
                codigo = req.getSku();
                tipo = com.isam.dto.producto.TraducirIdentificadorRequestDto.TipoIdentificador.SKU;
                break;
            case EAN:
                codigo = req.getEan();
                tipo = com.isam.dto.producto.TraducirIdentificadorRequestDto.TipoIdentificador.EAN;
                break;
            case PLU:
                codigo = req.getPlu();
                tipo = com.isam.dto.producto.TraducirIdentificadorRequestDto.TipoIdentificador.PLU;
                break;
            case CODIGO_NOT_SET:
                throw new IllegalArgumentException("No se proporcionó ningún código (SKU, EAN o PLU)");
        }
        
        return new com.isam.dto.producto.TraducirIdentificadorRequestDto(codigo, tipo);
    }

    /**
     * Convierte ResultadoTraduccionDto a proto ResultadoTraduccion.
     */
    public com.isam.grpc.catalogo.ResultadoTraduccion toProto(com.isam.dto.producto.ResultadoTraduccionDto dto) {
        com.isam.grpc.catalogo.ResultadoTraduccion.Builder builder = com.isam.grpc.catalogo.ResultadoTraduccion.newBuilder();
        
        // Mapear código de entrada según su tipo
        switch (dto.tipoEntrada()) {
            case SKU:
                builder.setSkuEntrada(dto.codigoEntrada());
                break;
            case EAN:
                builder.setEanEntrada(dto.codigoEntrada());
                break;
            case PLU:
                builder.setPluEntrada(dto.codigoEntrada());
                break;
        }
        
        // Mapear código de salida según su tipo
        switch (dto.tipoSalida()) {
            case SKU:
                builder.setSkuSalida(dto.codigoSalida());
                break;
            case EAN:
                builder.setEanSalida(dto.codigoSalida());
                break;
            case PLU:
                builder.setPluSalida(dto.codigoSalida());
                break;
        }
        
        return builder.build();
    }

    /**
     * Formatea un BigDecimal a String asegurando que el separador decimal sea punto (.).
     * Esto garantiza consistencia independientemente de la configuración regional del sistema.
     * @param precio BigDecimal a formatear
     * @return String con formato "0.00" usando punto como separador decimal
     */
    private String formatPrecio(BigDecimal precio) {
        if (precio == null) {
            return "0.00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#0.00", symbols);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(precio);
    }

}
