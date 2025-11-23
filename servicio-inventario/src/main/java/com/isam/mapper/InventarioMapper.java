package com.isam.mapper;

import com.isam.model.Inventario;
import com.isam.model.Lote;
import com.isam.model.MovimientoInventario;
import com.isam.model.Proveedor;
import com.isam.dto.proveedor.ProveedorDto;
import com.isam.dto.proveedor.AgregarProveedorRequestDto;
import com.isam.dto.existencias.RegistrarNuevasExistenciasRequestDto;
import com.isam.dto.inventario.CrearInventarioRequestDto;
import com.isam.dto.inventario.DetallesInventarioCompletoDto;
import com.isam.dto.inventario.ConsultarInventarioRequestDto;
import com.isam.dto.inventario.ConsultarInventarioResponseDto;
import com.isam.dto.lote.DetalleLoteDto;
import com.isam.grpc.inventario.AgregarProveedorRequest;
import com.isam.grpc.inventario.CrearInventarioRequest;
import com.isam.grpc.inventario.ConsultarInventarioRequest;
import com.isam.grpc.inventario.MoverStockEstanteriaRequest;
import com.isam.grpc.inventario.MovimientoInventarioProto;
import com.isam.grpc.inventario.ProveedorProto;
import com.isam.grpc.inventario.RegistrarNuevasExistenciasRequest;
import com.isam.grpc.inventario.LoteProto;
import com.isam.grpc.inventario.InventarioProto;
import com.isam.grpc.inventario.DetallesInventarioCompleto;
import com.isam.grpc.inventario.DetalleLote;
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
        String fechaCaducidad = req.hasFechaCaducidad() ? req.getFechaCaducidad() : null;
        
        return new RegistrarNuevasExistenciasRequestDto(
            req.getSku(),
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
            .setCantidadEntrada(lote.getCantidadEntrada() != null ? lote.getCantidadEntrada().doubleValue() : 0.0)
            .setCantidadAlmacen(lote.getCantidadAlmacen() != null ? lote.getCantidadAlmacen().doubleValue() : 0.0)
            .setCantidadEstanteria(lote.getCantidadEstanteria() != null ? lote.getCantidadEstanteria().doubleValue() : 0.0)
            .setIdProveedor(lote.getIdProveedor())
            .setFechaIngreso(lote.getFechaIngreso() != null ? lote.getFechaIngreso().toString() : "")
            .setUnidadMedida(mapUnidadMedidaToProto(lote.getUnidadMedida()))
            .setEstado(mapEstadoLoteToProto(lote.getEstado()));
        
        // Campos opcionales
        if (lote.getFechaCaducidad() != null) {
            builder.setFechaCaducidad(lote.getFechaCaducidad().toString());
        }
        
        return builder.build();
    }

    public InventarioProto toProto(Inventario inventario) {
        InventarioProto.Builder builder = InventarioProto.newBuilder()
            .setIdInventario(inventario.getIdInventario())
            .setSku(inventario.getSku())
            .setCantidadAlmacen(inventario.getCantidadAlmacen() != null ? inventario.getCantidadAlmacen().doubleValue() : 0.0)
            .setCantidadEstanteria(inventario.getCantidadEstanteria() != null ? inventario.getCantidadEstanteria().doubleValue() : 0.0)
            .setUnidadMedida(mapUnidadMedidaToProto(inventario.getUnidadMedida()));
        
        // Campos opcionales EAN/PLU - usar oneof
        if (inventario.getEan() != null) {
            builder.setEan(inventario.getEan());
        } else if (inventario.getPlu() != null) {
            builder.setPlu(inventario.getPlu());
        }
        
        return builder.build();
    }
    public LoteProto toProto(com.isam.dto.lote.LoteDto dto) {
        LoteProto.Builder builder = LoteProto.newBuilder()
            .setIdLote(dto.idLote())
            .setSku(dto.sku())
            .setIdInventario(dto.idInventario())
            .setNumeroLote(dto.numeroLote())
            .setCantidadEntrada(dto.cantidadEntrada())
            .setCantidadAlmacen(dto.cantidadAlmacen())
            .setCantidadEstanteria(dto.cantidadEstanteria())
            .setIdProveedor(dto.idProveedor())
            .setFechaIngreso(dto.fechaIngreso())
            .setUnidadMedida(mapUnidadMedidaToProto(UnidadMedida.valueOf(dto.unidadMedida())))
            .setEstado(mapEstadoLoteToProto(EstadoLote.valueOf(dto.estado())));
        
        // Campos opcionales
        if (dto.fechaCaducidad() != null) {
            builder.setFechaCaducidad(dto.fechaCaducidad());
        }
        
        return builder.build();
    }

    public InventarioProto toProto(com.isam.dto.inventario.InventarioDto dto) {
        InventarioProto.Builder builder = InventarioProto.newBuilder()
            .setIdInventario(dto.idInventario())
            .setSku(dto.sku())
            .setCantidadAlmacen(dto.cantidadAlmacen())
            .setCantidadEstanteria(dto.cantidadEstanteria())
            .setUnidadMedida(mapUnidadMedidaToProto(UnidadMedida.valueOf(dto.unidadMedida())));
        
        // Campos opcionales EAN/PLU - usar oneof
        if (dto.ean() != null) {
            builder.setEan(dto.ean());
        } else if (dto.plu() != null) {
            builder.setPlu(dto.plu());
        }
        
        return builder.build();
    }



    public CrearInventarioRequestDto toDto(CrearInventarioRequest req){
        String ean = null;
        String plu = null;
        
        // Manejar el campo oneof identificador
        if (req.hasEan()) {
            ean = req.getEan();
        } else if (req.hasPlu()) {
            plu = req.getPlu();
        }
        
        return new CrearInventarioRequestDto(
            req.getSku(),
            ean,
            plu,
            mapUnidadMedida(req.getUnidadMedida())
        );
    }

    // Mapeo para Mover Stock a Estantería
    public com.isam.dto.stock.MoverStockEstanteriaRequestDto toDto(MoverStockEstanteriaRequest req) {
        return new com.isam.dto.stock.MoverStockEstanteriaRequestDto(
            req.getSku(),
            req.getIdLote(),
            java.math.BigDecimal.valueOf(req.getCantidadTransladar()),
            mapUnidadMedida(req.getUnidadMedida())
        );
    }

    public MovimientoInventarioProto toProto(com.isam.dto.movimiento.MovimientoInventarioDto dto) {
        MovimientoInventarioProto.Builder builder = MovimientoInventarioProto.newBuilder()
            .setIdMovimiento(dto.idMovimiento())
            .setSku(dto.sku())
            .setTipoMovimiento(mapTipoMovimientoToProto(dto.tipoMovimiento()))
            .setCantidad(dto.cantidad())
            .setUnidadMedida(mapUnidadMedidaToProto(com.isam.model.UnidadMedida.valueOf(dto.unidadMedida())))
            .setFechaHora(dto.fechaHora())
            .setIdUsuario(dto.idUsuario());
        
        if (dto.idLote() != null) {
            builder.setIdLote(dto.idLote());
        }
        if (dto.motivo() != null) {
            builder.setMotivo(dto.motivo());
        }
        if (dto.observaciones() != null) {
            builder.setObservaciones(dto.observaciones());
        }
        
        return builder.build();
    }

    private com.isam.grpc.inventario.TipoMovimiento mapTipoMovimientoToProto(String tipoMovimiento) {
        switch (tipoMovimiento) {
            case "ENTRADA":
                return com.isam.grpc.inventario.TipoMovimiento.ENTRADA;
            case "SALIDA":
                return com.isam.grpc.inventario.TipoMovimiento.SALIDA;
            case "AJUSTE":
                return com.isam.grpc.inventario.TipoMovimiento.AJUSTE;
            case "TRASLADO_ESTANTERIA":
                return com.isam.grpc.inventario.TipoMovimiento.TRASLADO_ESTANTERIA;
            case "VENTA":
                return com.isam.grpc.inventario.TipoMovimiento.VENTA;
            case "DEVOLUCION":
                return com.isam.grpc.inventario.TipoMovimiento.DEVOLUCION;
            case "MERMA":
                return com.isam.grpc.inventario.TipoMovimiento.MERMA;
            default:
                return com.isam.grpc.inventario.TipoMovimiento.TIPO_MOVIMIENTO_UNSPECIFIED;
        }
    }

    // Mapeo para Consultar Inventario
    public ConsultarInventarioRequestDto toDto(ConsultarInventarioRequest req) {
        return new ConsultarInventarioRequestDto(req.getSku());
    }

    public DetallesInventarioCompleto toProto(ConsultarInventarioResponseDto dto) {
        // DEBUG: Fixed method access - need to get the nested DTO first
        DetallesInventarioCompletoDto detallesDto = dto.detallesInventario();
        
        DetallesInventarioCompleto.Builder builder = DetallesInventarioCompleto.newBuilder()
            .setSku(detallesDto.sku())
            .setNombreProducto(detallesDto.nombreProducto())
            .setStockTotalAlmacen(detallesDto.stockTotalAlmacen())
            .setStockTotalEstanteria(detallesDto.stockTotalEstanteria())
            .setUnidadMedida(mapUnidadMedidaToProto(detallesDto.unidadMedida()));
        
        // Añadir detalles de lotes
        for (DetalleLoteDto loteDto : detallesDto.lotes()) {
            DetalleLote loteProto = toProtoDetalleLote(loteDto);
            builder.addLote(loteProto);
        }
        
        return builder.build();
    }

    private DetalleLote toProtoDetalleLote(DetalleLoteDto dto) {
        DetalleLote.Builder builder = DetalleLote.newBuilder()
            .setIdLote(dto.idLote())
            .setNumeroLote(dto.numeroLote())
            .setCantidadAlmacen(dto.cantidadAlmacen())
            .setCantidadEstanteria(dto.cantidadEstanteria())
            .setFechaIngreso(dto.fechaIngreso());
        
        if (dto.fechaCaducidad() != null) {
            builder.setFechaCaducidad(dto.fechaCaducidad());
        }
        
        return builder.build();
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