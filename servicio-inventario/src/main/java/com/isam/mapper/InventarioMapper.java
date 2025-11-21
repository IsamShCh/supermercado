package com.isam.mapper;

import com.isam.model.Inventario;
import com.isam.model.Lote;
import com.isam.model.MovimientoInventario;
import com.isam.model.Proveedor;
import com.isam.dto.proveedor.ProveedorDto;
import com.isam.dto.proveedor.AgregarProveedorRequestDto;
import com.isam.dto.existencias.RegistrarNuevasExistenciasRequestDto;
import com.isam.dto.inventario.CrearInventarioRequestDto;
import com.isam.grpc.inventario.AgregarProveedorRequest;
import com.isam.grpc.inventario.CrearInventarioRequest;
import com.isam.grpc.inventario.ProveedorProto;
import com.isam.grpc.inventario.RegistrarNuevasExistenciasRequest;
import com.isam.grpc.inventario.LoteProto;
import com.isam.grpc.inventario.InventarioProto;
import com.isam.model.UnidadMedida;
import com.isam.model.EstadoLote;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class InventarioMapper {

    // Mapeeod proveedor
    public AgregarProveedorRequestDto toDto(AgregarProveedorRequest req) {
        return new AgregarProveedorRequestDto(
            req.getNombreProveedor(),
            req.getContacto(),
            req.getDireccion(),
            req.getTelefono(),
            req.getEmail()
        );
    }

    public ProveedorProto toProto(Proveedor proveedor) {
        ProveedorProto.Builder builder = ProveedorProto.newBuilder()
            .setIdProveedor(proveedor.getIdProveedor())
            .setNombreProveedor(proveedor.getNombreProveedor());
        
        // Establecer campos opcionales solo si no son null
        if (proveedor.getContacto() != null) {
            builder.setContacto(proveedor.getContacto());
        }
        if (proveedor.getDireccion() != null) {
            builder.setDireccion(proveedor.getDireccion());
        }
        if (proveedor.getTelefono() != null) {
            builder.setTelefono(proveedor.getTelefono());
        }
        if (proveedor.getEmail() != null) {
            builder.setEmail(proveedor.getEmail());
        }
        
        return builder.build();
    }

    public ProveedorProto toProto(ProveedorDto dto) {
        ProveedorProto.Builder builder = ProveedorProto.newBuilder()
            .setIdProveedor(dto.idProveedor())
            .setNombreProveedor(dto.nombreProveedor());
        
        if (dto.contacto() != null) {
            builder.setContacto(dto.contacto());
        }
        if (dto.direccion() != null) {
            builder.setDireccion(dto.direccion());
        }
        if (dto.telefono() != null) {
            builder.setTelefono(dto.telefono());
        }
        if (dto.email() != null) {
            builder.setEmail(dto.email());
        }
        
        return builder.build();
    }

    public ProveedorDto toDto(Proveedor proveedor) {
        return new ProveedorDto(
            proveedor.getIdProveedor(),
            proveedor.getNombreProveedor(),
            proveedor.getContacto(),
            proveedor.getDireccion(),
            proveedor.getTelefono(),
            proveedor.getEmail()
        );
    }

    // Mapeo para Registrar Nuevas Existencias
    public RegistrarNuevasExistenciasRequestDto toDto(RegistrarNuevasExistenciasRequest req) {
        String ean = req.hasEan() ? req.getEan() : null;
        String plu = req.hasPlu() ? req.getPlu() : null;
        String fechaCaducidad = req.hasFechaCaducidad() ? req.getFechaCaducidad() : null;
        
        return new RegistrarNuevasExistenciasRequestDto(
            req.getSku(),
            ean,
            plu,
            BigDecimal.valueOf(req.getCantidad()),
            req.getNumeroLote(),
            fechaCaducidad,
            req.getIdProveedor(),
            mapUnidadMedida(req.getUnidadMedida())
        );
    }

    public LoteProto toProto(Lote lote) {
        LoteProto.Builder builder = LoteProto.newBuilder()
            .setIdLote(lote.getIdLote())
            .setSku(lote.getSku())
            .setIdInventario(lote.getIdInventario())
            .setNumeroLote(lote.getNumeroLote())
            .setCantidad(lote.getCantidad().doubleValue())
            .setIdProveedor(lote.getIdProveedor())
            .setFechaIngreso(lote.getFechaIngreso().toString())
            .setUnidadMedida(mapUnidadMedidaToProto(lote.getUnidadMedida()))
            .setEstado(mapEstadoLoteToProto(lote.getEstado()));
        
        // Campos opcionales
        if (lote.getEan() != null) {
            builder.setEan(lote.getEan());
        } else if (lote.getPlu() != null) {
            builder.setPlu(lote.getPlu());
        }
        
        if (lote.getFechaCaducidad() != null) {
            builder.setFechaCaducidad(lote.getFechaCaducidad().toString());
        }
        
        return builder.build();
    }

    public InventarioProto toProto(Inventario inventario) {
        return InventarioProto.newBuilder()
            .setIdInventario(inventario.getIdInventario())
            .setSku(inventario.getSku())
            .setCantidadAlmacen(inventario.getCantidadAlmacen().doubleValue())
            .setCantidadEstanteria(inventario.getCantidadEstanteria().doubleValue())
            .setUnidadMedida(mapUnidadMedidaToProto(inventario.getUnidadMedida()))
            .build();
    }
    public LoteProto toProto(com.isam.dto.lote.LoteDto dto) {
        LoteProto.Builder builder = LoteProto.newBuilder()
            .setIdLote(dto.idLote())
            .setSku(dto.sku())
            .setIdInventario(dto.idInventario())
            .setNumeroLote(dto.numeroLote())
            .setCantidad(dto.cantidad())
            .setIdProveedor(dto.idProveedor())
            .setFechaIngreso(dto.fechaIngreso())
            .setUnidadMedida(mapUnidadMedidaToProto(UnidadMedida.valueOf(dto.unidadMedida())))
            .setEstado(mapEstadoLoteToProto(EstadoLote.valueOf(dto.estado())));
        
        // Campos opcionales
        if (dto.ean() != null) {
            builder.setEan(dto.ean());
        } else if (dto.plu() != null) {
            builder.setPlu(dto.plu());
        }
        
        if (dto.fechaCaducidad() != null) {
            builder.setFechaCaducidad(dto.fechaCaducidad());
        }
        
        return builder.build();
    }

    public InventarioProto toProto(com.isam.dto.inventario.InventarioDto dto) {
        return InventarioProto.newBuilder()
            .setIdInventario(dto.idInventario())
            .setSku(dto.sku())
            .setCantidadAlmacen(dto.cantidadAlmacen())
            .setCantidadEstanteria(dto.cantidadEstanteria())
            .setUnidadMedida(mapUnidadMedidaToProto(UnidadMedida.valueOf(dto.unidadMedida())))
            .build();
    }



    public CrearInventarioRequestDto toDto(CrearInventarioRequest req){
        return new CrearInventarioRequestDto(
            req.getSku(),
            mapUnidadMedida(req.getUnidadMedida())
        );
    }

    // Mappers de enums
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

    private com.isam.grpc.inventario.EstadoLote mapEstadoLoteToProto(EstadoLote entityEnum) {
        switch (entityEnum) {
            case DISPONIBLE:
                return com.isam.grpc.inventario.EstadoLote.DISPONIBLE;
            case BLOQUEADO:
                return com.isam.grpc.inventario.EstadoLote.BLOQUEADO;
            case ELIMINADO:
                return com.isam.grpc.inventario.EstadoLote.ELIMINADO;
            default:
                return com.isam.grpc.inventario.EstadoLote.ESTADO_LOTE_UNSPECIFIED;
        }
    }

    // Basic mappers for inventory entities
    // To be expanded when proto definitions are available

    // Example mapping methods (placeholders)
    public Inventario toEntity(Object dto) {
        // TODO: Implement when DTOs are defined
        return null;
    }

    public Object toDto(Inventario inventario) {
        // TODO: Implement when DTOs are defined
        return null;
    }

    public Lote loteToEntity(Object dto) {
        // TODO: Implement when DTOs are defined
        return null;
    }

    public Object loteToDto(Lote lote) {
        // TODO: Implement when DTOs are defined
        return null;
    }

    public MovimientoInventario movimientoToEntity(Object dto) {
        // TODO: Implement when DTOs are defined
        return null;
    }

    public Object movimientoToDto(MovimientoInventario movimiento) {
        // TODO: Implement when DTOs are defined
        return null;
    }
}