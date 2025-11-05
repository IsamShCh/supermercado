package com.isam.mapper;

import com.isam.grpc.catalogo.CategoriaProto;
import com.isam.grpc.catalogo.CrearCategoriaRequest;
import com.isam.grpc.catalogo.CrearProductoReq;
import com.isam.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class CatalogoMapper {

    public Producto toEntity(CrearProductoReq req) {
        Producto entity = new Producto();

        entity.setSku(req.getSku());
        entity.setNombre(req.getNombre());
        entity.setDescripcion(req.getDescripcion());
        entity.setPrecioVenta(BigDecimal.valueOf(req.getPrecioVenta()));
        entity.setCaduca(req.getCaduca());
        entity.setEsGranel(req.getEsGranel());

        // Map enums
        entity.setPoliticaRotacion(mapPoliticaRotacion(req.getPoliticaRotacion()));
        entity.setUnidadMedida(mapUnidadMedida(req.getUnidadMedida()));

        // Handle oneof identificador
        if (req.hasEan()) {
            entity.setEan(req.getEan());
        } else if (req.hasPlu()) {
            String pluValue = req.getPlu();
            System.out.println("DEBUG: PLU value from request: '" + pluValue + "', length: " + pluValue.length());
            entity.setPlu(pluValue);
        }

        // Default estado
        entity.setEstado(EstadoProducto.ACTIVO);

        return entity;
    }

    private PoliticaRotacion mapPoliticaRotacion(com.isam.grpc.common.PoliticaRotacion protoEnum) {
        switch (protoEnum) {
            case FIFO:
                return PoliticaRotacion.FIFO;
            case FEFO:
                return PoliticaRotacion.FEFO;
            case LIFO:
                return PoliticaRotacion.LIFO;
            default:
                return PoliticaRotacion.POLITICA_ROTACION_UNSPECIFIED;
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
        com.isam.grpc.catalogo.ProductoProto.Builder builder = com.isam.grpc.catalogo.ProductoProto.newBuilder()
                .setSku(entity.getSku())
                .setNombre(entity.getNombre())
                .setPrecioVenta(entity.getPrecioVenta().doubleValue())
                .setCaduca(entity.getCaduca())
                .setEsGranel(entity.getEsGranel())
                .setPoliticaRotacion(mapPoliticaRotacionToProto(entity.getPoliticaRotacion()))
                .setUnidadMedida(mapUnidadMedidaToProto(entity.getUnidadMedida()))
                .setEstado(mapEstadoProductoToProto(entity.getEstado()))
                .addAllEtiquetas(parseEtiquetas(entity.getEtiquetas()));

        if (entity.getDescripcion() != null) {
            builder.setDescripcion(entity.getDescripcion());
        }

        if (entity.getCategoria() != null) {
            builder.setIdCategoria(entity.getCategoria().getIdCategoria());
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
        switch (entityEnum) {
            case FIFO:
                return com.isam.grpc.common.PoliticaRotacion.FIFO;
            case FEFO:
                return com.isam.grpc.common.PoliticaRotacion.FEFO;
            case LIFO:
                return com.isam.grpc.common.PoliticaRotacion.LIFO;
            default:
                return com.isam.grpc.common.PoliticaRotacion.POLITICA_ROTACION_UNSPECIFIED;
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

    private com.isam.grpc.catalogo.EstadoProducto mapEstadoProductoToProto(EstadoProducto entityEnum) {
        switch (entityEnum) {
            case ACTIVO:
                return com.isam.grpc.catalogo.EstadoProducto.PRODUCTO_ACTIVO;
            case DESCATALOGADO:
                return com.isam.grpc.catalogo.EstadoProducto.PRODUCTO_DESCATALOGADO;
            default:
                return com.isam.grpc.catalogo.EstadoProducto.ESTADO_PRODUCTO_UNSPECIFIED;
        }
    }

    private List<String> parseEtiquetas(String etiquetas) {
        if (etiquetas == null || etiquetas.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(etiquetas.split(","));
    }
}