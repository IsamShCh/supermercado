package com.isam.mapper;

import com.isam.model.Inventario;
import com.isam.model.Lote;
import com.isam.model.MovimientoInventario;
import com.isam.model.Proveedor;
import com.isam.dto.proveedor.ProveedorDto;
import com.isam.dto.proveedor.AgregarProveedorRequestDto;
import com.isam.grpc.inventario.AgregarProveedorRequest;
import com.isam.grpc.inventario.ProveedorProto;
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