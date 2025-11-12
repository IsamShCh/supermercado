package com.isam.mapper;

import com.isam.dto.producto.CrearProductoDto;
import com.isam.grpc.catalogo.CategoriaProto;
import com.isam.grpc.catalogo.CrearCategoriaRequest;
import com.isam.grpc.catalogo.CrearProductoRequest;
import com.isam.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class CatalogoMapper {

    /**
     * Converts gRPC CrearProductoRequest to DTO
     * This method only handles the mapping between protocols, not business logic
     */
    public CrearProductoDto toDto(CrearProductoRequest req) {
        System.out.println("DEBUG: Converting gRPC request to DTO");
        System.out.println("DEBUG: Request - SKU: " + req.getSku());
        System.out.println("DEBUG: Request - id_categoria: " + req.getIdCategoria());
        System.out.println("DEBUG: Request - etiquetas count: " + req.getEtiquetasList().size());
        
        CrearProductoDto dto = new CrearProductoDto();

        // Propiedades fundamentales
        dto.setSku(req.getSku());
        dto.setNombre(req.getNombre());
        dto.setDescripcion(req.getDescripcion());
        dto.setPrecioVenta(BigDecimal.valueOf(req.getPrecioVenta()));
        dto.setCaduca(req.getCaduca());
        dto.setEsGranel(req.getEsGranel());

        // Map enums
        dto.setPoliticaRotacion(mapPoliticaRotacion(req.getPoliticaRotacion()));
        dto.setUnidadMedida(mapUnidadMedida(req.getUnidadMedida()));

        // Handle oneof identificador
        if (req.hasEan()) {
            dto.setEan(req.getEan());
        } else if (req.hasPlu()) {
            String pluValue = req.getPlu();
            System.out.println("DEBUG: Valor del PLU: '" + pluValue + "', tamanyo: " + pluValue.length());
            dto.setPlu(pluValue);
        }

        // Handle categoria ID
        if (req.getIdCategoria() > 0) {
            dto.setIdCategoria(req.getIdCategoria());
            System.out.println("DEBUG: Category ID set in DTO: " + req.getIdCategoria());
        }

        // Handle etiquetas
        if (!req.getEtiquetasList().isEmpty()) {
            dto.setEtiquetas(req.getEtiquetasList());
            System.out.println("DEBUG: Etiquetas set in DTO: " + req.getEtiquetasList());
        }

        System.out.println("DEBUG: DTO created successfully: " + dto);
        return dto;
    }

    /**
     * Converts DTO to Entity
     * Note: Categoria will be null here - it should be populated by the Service layer
     */
    public Producto toEntity(CrearProductoDto dto) {
        System.out.println("DEBUG: Converting DTO to Entity");
        System.out.println("DEBUG: DTO: " + dto);
        
        Producto entity = new Producto();

        // Propiedades fundamentales
        entity.setSku(dto.getSku());
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setPrecioVenta(dto.getPrecioVenta());
        entity.setCaduca(dto.getCaduca());
        entity.setEsGranel(dto.getEsGranel());

        // Map enums
        entity.setPoliticaRotacion(dto.getPoliticaRotacion());
        entity.setUnidadMedida(dto.getUnidadMedida());

        // Handle identificadores
        entity.setEan(dto.getEan());
        entity.setPlu(dto.getPlu());

        // Default estado
        entity.setEstado(EstadoProducto.ACTIVO);

        // Handle etiquetas
        if (dto.getEtiquetas() != null && !dto.getEtiquetas().isEmpty()) {
            String etiquetasString = String.join(",", dto.getEtiquetas());
            entity.setEtiquetas(etiquetasString);
            System.out.println("DEBUG: Etiquetas set in entity: " + etiquetasString);
        }

        // NOTE: Categoria will be null here - Service layer responsibility to populate it
        System.out.println("DEBUG: Entity created (without categoria): " + entity);
        return entity;
    }

    private PoliticaRotacion mapPoliticaRotacion(com.isam.grpc.common.PoliticaRotacion protoEnum) {
        System.out.println("DEBUG: Mapping PoliticaRotacion from proto: " + protoEnum);       
        try {
            System.out.println("DEBUG: Enum ordinal: " + protoEnum.getNumber());
        } catch (IllegalArgumentException e) {
            System.out.println("DEBUG: Cannot get number for enum " + protoEnum + ", treating as UNRECOGNIZED");
            return PoliticaRotacion.FIFO; // Default to FIFO for problematic enum values
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
                return PoliticaRotacion.FIFO; // Default to FIFO instead of UNSPECIFIED
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
                .setPrecioVenta(entity.getPrecioVenta().doubleValue())
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


    public Categoria toEntity(CrearCategoriaRequest crearCategoriaRequest){

        Categoria categoria = new Categoria();
        categoria.setNombreCategoria(crearCategoriaRequest.getNombreCategoria());
        categoria.setDescripcion(crearCategoriaRequest.getDescripcion());

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
}