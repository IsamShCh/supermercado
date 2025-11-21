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
import com.isam.dto.existencias.RegistrarNuevasExistenciasRequestDto;
import com.isam.dto.existencias.RegistrarNuevasExistenciasResponseDto;
import com.isam.dto.lote.LoteDto;
import com.isam.dto.inventario.CrearInventarioRequestDto;
import com.isam.dto.inventario.InventarioDto;
import com.isam.model.EstadoLote;
import com.isam.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * Registra nuevas existencias en el inventario (AC14).
     * Este método crea un nuevo lote y actualiza el inventario correspondiente.
     */
    @Transactional
    public RegistrarNuevasExistenciasResponseDto registrarNuevasExistencias(RegistrarNuevasExistenciasRequestDto dto) {
        
        // Validar que solo tenga EAN o PLU, no ambos (esto es muy defensivo, el validator lo deberia haber filtrado ya)
        if (isNotNullOrEmpty(dto.ean()) && isNotNullOrEmpty(dto.plu())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Un producto solo puede tener o EAN o PLU, pero no ambos")
                .asRuntimeException();
        }

        // Verificar que el proveedor existe
        Proveedor proveedor = proveedorRepository.findById(dto.idProveedor())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Proveedor no encontrado con ID '" + dto.idProveedor() + "'")
                .asRuntimeException());

        // Buscar o crear el inventario para este SKU
        // esto no sirve, porque necesito creacion implicita.
       // Buscar el inventario para este SKU
        Inventario inventario = inventarioRepository.findBySku(dto.sku())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Inventario no encontrado para SKU '" + dto.sku() + "'")
                .asRuntimeException());

        // Validar que la unidad de medida coincida
        if (!inventario.getUnidadMedida().equals(dto.unidadMedida())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("La unidad de medida no coincide con la del inventario existente")
                .asRuntimeException();
        }

        // Guardar inventario si es nuevo
        if (inventario.getIdInventario() == null) {
            inventario = inventarioRepository.save(inventario);
        }

        // Parsear fecha de caducidad si existe
        LocalDate fechaCaducidad = null;
        if (dto.fechaCaducidad() != null && !dto.fechaCaducidad().isBlank()) {
            try {
                fechaCaducidad = LocalDate.parse(dto.fechaCaducidad());
            } catch (Exception e) {
                throw Status.INVALID_ARGUMENT
                    .withDescription("Formato de fecha de caducidad inválido. Use YYYY-MM-DD")
                    .asRuntimeException();
            }
        }

        // Crear el nuevo lote
        Lote lote = new Lote();
        lote.setSku(dto.sku());
        lote.setIdInventario(inventario.getIdInventario());
        lote.setEan(dto.ean());
        lote.setPlu(dto.plu());
        lote.setNumeroLote(dto.numeroLote());
        lote.setCantidad(dto.cantidad());
        lote.setFechaCaducidad(fechaCaducidad);
        lote.setIdProveedor(dto.idProveedor());
        lote.setFechaIngreso(LocalDate.now());
        lote.setUnidadMedida(dto.unidadMedida());
        lote.setEstado(EstadoLote.DISPONIBLE);

        // Guardar el lote
        Lote loteGuardado = loteRepository.save(lote);

        // Actualizar el inventario: sumar la cantidad al almacén
        inventario.setCantidadAlmacen(inventario.getCantidadAlmacen().add(dto.cantidad()));
        Inventario inventarioActualizado = inventarioRepository.save(inventario);

        // Crear movimiento de inventario
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setSku(dto.sku());
        movimiento.setIdLote(loteGuardado.getIdLote());
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA);
        movimiento.setCantidad(dto.cantidad());
        movimiento.setUnidadMedida(dto.unidadMedida());
        movimiento.setFechaHora(LocalDateTime.now());
        movimiento.setIdUsuario("SYSTEM"); // TODO: Obtener del contexto de seguridad
        movimiento.setMotivo("Registro de nuevas existencias - Lote: " + dto.numeroLote());
        movimiento.setObservaciones("Proveedor: " + proveedor.getNombreProveedor());
        movimientoRepository.save(movimiento);

        // Convertir entidades a DTOs
        LoteDto loteDto = new LoteDto(
            loteGuardado.getIdLote(),
            loteGuardado.getSku(),
            loteGuardado.getIdInventario(),
            loteGuardado.getEan(),
            loteGuardado.getPlu(),
            loteGuardado.getNumeroLote(),
            loteGuardado.getCantidad().doubleValue(),
            loteGuardado.getFechaCaducidad() != null ? loteGuardado.getFechaCaducidad().toString() : null,
            loteGuardado.getIdProveedor(),
            loteGuardado.getFechaIngreso().toString(),
            loteGuardado.getUnidadMedida().name(),
            loteGuardado.getEstado().name()
        );

        InventarioDto inventarioDto = new InventarioDto(
            inventarioActualizado.getIdInventario(),
            inventarioActualizado.getSku(),
            inventarioActualizado.getCantidadAlmacen().doubleValue(),
            inventarioActualizado.getCantidadEstanteria().doubleValue(),
            inventarioActualizado.getUnidadMedida().name()
        );

        return new RegistrarNuevasExistenciasResponseDto(loteDto, inventarioDto);
    }

    private boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }



    @Transactional
    public InventarioDto crearInventario(CrearInventarioRequestDto dto) {
        // Comprobamos que no exista un inventario ya existente. Si existe y es igual a los datos del dto, lo dejamos pasar, si no, tiramos error
        Optional<Inventario> inventarioExistenteOpt = inventarioRepository.findBySku(dto.sku());
        
        if (inventarioExistenteOpt.isPresent()) {
            Inventario inventarioExistente = inventarioExistenteOpt.get();
            if (!inventarioExistente.getUnidadMedida().equals(dto.unidadMedida())) {
                throw Status.ALREADY_EXISTS
                    .withDescription("Ya existe un inventario para SKU '" + dto.sku() + "' con unidad de medida diferente")
                    .asRuntimeException();
            }
            // Si existe y coincide, devolver el DTO existente
            return new InventarioDto(
                inventarioExistente.getIdInventario(),
                inventarioExistente.getSku(),
                inventarioExistente.getCantidadAlmacen().doubleValue(),
                inventarioExistente.getCantidadEstanteria().doubleValue(),
                inventarioExistente.getUnidadMedida().name()
            );
        } else {
            // Crear nuevo inventario
            Inventario nuevoInventario = new Inventario(dto.sku(), BigDecimal.ZERO, BigDecimal.ZERO, dto.unidadMedida());
            Inventario inventarioGuardado = inventarioRepository.save(nuevoInventario);
            return new InventarioDto(
                inventarioGuardado.getIdInventario(),
                inventarioGuardado.getSku(),
                inventarioGuardado.getCantidadAlmacen().doubleValue(),
                inventarioGuardado.getCantidadEstanteria().doubleValue(),
                inventarioGuardado.getUnidadMedida().name()
            );
        }
    }

}