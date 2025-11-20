package com.isam.service;

import com.isam.model.Inventario;
import com.isam.model.Lote;
import com.isam.model.MovimientoInventario;
import com.isam.model.Proveedor;
import com.isam.repository.InventarioRepository;
import com.isam.repository.LoteRepository;
import com.isam.repository.MovimientoInventarioRepository;
import com.isam.repository.ProveedorRepository;
import com.isam.dto.proveedor.AgregarProveedorRequestDto;
import com.isam.dto.proveedor.ProveedorDto;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.grpc.Status;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;
    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private MovimientoInventarioRepository movimientoRepository;
    @Autowired
    private ProveedorRepository proveedorRepository;

    
    /**
     * Crea un proveedor a partir de DTO, gestionando validaciones y la creación de la entidad.
     * Este método contiene la lógica empresarial para la creación de proveedores.
     */
    @Transactional
    public ProveedorDto agregarProveedor(AgregarProveedorRequestDto dto) {

        // Verificar duplicado por nombre ANTES de crear la entidad
        if (proveedorRepository.existsByNombreProveedor(dto.nombreProveedor())) {
            throw Status.ALREADY_EXISTS
                .withDescription("Ya existe un proveedor con el nombre: " + dto.nombreProveedor())
                .asRuntimeException();
        }

        // Verificar duplicado por email si se proporciona
        if (dto.email() != null && !dto.email().isBlank()) {
            if (proveedorRepository.existsByEmail(dto.email())) {
                throw Status.ALREADY_EXISTS
                    .withDescription("Ya existe un proveedor con el email: " + dto.email())
                    .asRuntimeException();
            }
        }

        // Generar ID único para el proveedor
        String idProveedor = "PROV-" + System.currentTimeMillis();

        // Crear el proveedor
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(idProveedor);
        proveedor.setNombreProveedor(dto.nombreProveedor());
        proveedor.setContacto(dto.contacto());
        proveedor.setDireccion(dto.direccion());
        proveedor.setTelefono(dto.telefono());
        proveedor.setEmail(dto.email());

        // Guardar proveedor
        Proveedor proveedorGuardado = proveedorRepository.save(proveedor);

        // Convertir a DTO para devolver
        return new ProveedorDto(
            proveedorGuardado.getIdProveedor(),
            proveedorGuardado.getNombreProveedor(),
            proveedorGuardado.getContacto(),
            proveedorGuardado.getDireccion(),
            proveedorGuardado.getTelefono(),
            proveedorGuardado.getEmail()
        );
    }
}