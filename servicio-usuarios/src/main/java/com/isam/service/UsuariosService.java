package com.isam.service;

import com.isam.dto.rol.CrearRolRequestDto;
import com.isam.dto.rol.CrearRolResponseDto;
import com.isam.dto.rol.RolDto;
import com.isam.mapper.UsuariosMapper;
import com.isam.model.Rol;
import com.isam.repository.UsuarioRepository;
import com.isam.repository.PermisoRepository;
import com.isam.repository.RolRepository;
import com.isam.repository.SesionRepository;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la gestión de usuarios.
 * Contiene la lógica de negocio para operaciones CRUD de usuarios.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UsuariosService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final SesionRepository sesionRepository;
    private final UsuariosMapper usuariosMapper;

    /**
     * Crea un nuevo rol en el sistema.
     * @param dto DTO con los datos del rol a crear
     * @return DTO con los datos del rol creado
     */
    @Transactional
    public CrearRolResponseDto crearRol(CrearRolRequestDto dto) {
        log.info("Creando rol con nombre: {}", dto.nombreRol());

        // Verificar si ya existe un rol con ese nombre
        if (rolRepository.existsByNombreRol(dto.nombreRol())) {
            throw Status.ALREADY_EXISTS
                .withDescription("Ya existe un rol con el nombre '" + dto.nombreRol() + "'")
                .asRuntimeException();
        }

        // Crear la entidad Rol
        Rol rol = new Rol();
        rol.setNombreRol(dto.nombreRol());
        rol.setDescripcionRol(dto.descripcion());

        // Guardar el rol
        Rol rolGuardado = rolRepository.save(rol);
        log.info("Rol creado exitosamente con ID: {}", rolGuardado.getIdRol());

        // Convertir a DTO y retornar
        RolDto rolDto = usuariosMapper.toDto(rolGuardado);
        return new CrearRolResponseDto(rolDto);
    }

}