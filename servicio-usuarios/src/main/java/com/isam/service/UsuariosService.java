package com.isam.service;

import com.isam.dto.permiso.ListarPermisosRequestDto;
import com.isam.dto.permiso.ListarPermisosResponseDto;
import com.isam.dto.permiso.PermisoDto;
import com.isam.dto.rol.AsignarPermisosRequestDto;
import com.isam.dto.rol.AsignarPermisosResponseDto;
import com.isam.dto.rol.CrearRolRequestDto;
import com.isam.dto.rol.CrearRolResponseDto;
import com.isam.dto.rol.ListarRolesRequestDto;
import com.isam.dto.rol.ListarRolesResponseDto;
import com.isam.dto.rol.RolDto;
import com.isam.mapper.UsuariosMapper;
import com.isam.mapper.UsuariosMapperAuto;
import com.isam.model.Permiso;
import com.isam.model.Rol;
import com.isam.model.enums.AccionPermiso;
import com.isam.repository.UsuarioRepository;
import com.isam.repository.PermisoRepository;
import com.isam.repository.RolRepository;
import com.isam.repository.SesionRepository;
import io.grpc.Status;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

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
    private final UsuariosMapperAuto usuariosMapperAuto;


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

    /**
     * Inicializa los permisos del sistema si no existen.
     * Este método se ejecuta al iniciar la aplicación y crea los permisos básicos
     * para cada recurso y acción del sistema.
     */
    @PostConstruct
    @Transactional
    public void inicializarPermisos() {
        log.info("Verificando permisos del sistema...");
        
        // Lista de recursos del sistema
        List<String> recursos = Arrays.asList(
            "usuarios", "roles", "permisos", "catalogo", "productos",
            "categorias", "ofertas", "inventario", "lotes", "proveedores",
            "ventas", "movimientos", "reportes"
        );
        
        // Lista de acciones posibles
        List<AccionPermiso> acciones = Arrays.asList(
            AccionPermiso.CREAR, AccionPermiso.LEER, AccionPermiso.ACTUALIZAR,
            AccionPermiso.ELIMINAR, AccionPermiso.EJECUTAR
        );
        
        int permisosCreados = 0;
        
        for (String recurso : recursos) {
            for (AccionPermiso accion : acciones) {
                String nombrePermiso = generarNombrePermiso(recurso, accion);
                
                // Verificar si el permiso ya existe
                if (!permisoRepository.existsByNombrePermiso(nombrePermiso)) {
                    Permiso permiso = new Permiso();
                    permiso.setNombrePermiso(nombrePermiso);
                    permiso.setDescripcion(generarDescripcionPermiso(recurso, accion));
                    permiso.setRecurso(recurso);
                    permiso.setAccion(accion);
                    
                    permisoRepository.save(permiso);
                    permisosCreados++;
                    
                    log.debug("Permiso creado: {} para recurso: {} con acción: {}",
                        nombrePermiso, recurso, accion);
                }
            }
        }
        
        if (permisosCreados > 0) {
            log.info("Se inicializaron {} permisos nuevos en el sistema", permisosCreados);
        } else {
            log.info("Todos los permisos del sistema ya existen");
        }
    }
    
    /**
     * Asigna permisos a un rol existente.
     * @param dto DTO con los datos de asignación de permisos
     * @return DTO de respuesta vacío en caso de éxito
     */
    @Transactional
    public AsignarPermisosResponseDto asignarPermisosARol(AsignarPermisosRequestDto dto) {
        log.info("Asignando permisos al rol: {}", dto.idRol());
        
        // Verificar que el rol existe
        Rol rol = rolRepository.findById(dto.idRol())
            .orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Rol no encontrado con ID: '" + dto.idRol() + "'")
                .asRuntimeException());
        
        // Obtener los permisos solicitados
        List<Permiso> permisosAsignar = permisoRepository.findAllById(dto.idPermisos());
        
        // Verificar que todos los permisos existen
        if (permisosAsignar.size() != dto.idPermisos().size()) {
            List<String> idsEncontrados = permisosAsignar.stream()
                .map(Permiso::getIdPermiso)
                .toList();
            
            List<String> idsNoEncontrados = dto.idPermisos().stream()
                .filter(id -> !idsEncontrados.contains(id))
                .toList();
            
            throw Status.NOT_FOUND
                .withDescription("Permisos no encontrados: " + String.join(", ", idsNoEncontrados))
                .asRuntimeException();
        }
        
        // Limpiar permisos existentes y asignar los nuevos
        rol.getPermisos().clear();
        rol.getPermisos().addAll(permisosAsignar);
        
        // Guardar el rol con los nuevos permisos
        rolRepository.save(rol);
        
        log.info("Se asignaron {} permisos al rol '{}'",
            permisosAsignar.size(), rol.getNombreRol());
        
        return new AsignarPermisosResponseDto();
    }

    /**
     * Lista todos los roles del sistema.
     * @return DTO con la lista de roles
     */
    @Transactional(readOnly = true)
    public ListarRolesResponseDto listarRoles(ListarRolesRequestDto dto) {
        log.info("Listando todos los roles");
        List<Rol> roles = rolRepository.findAll();
        // Mapeamos automaticamente todos los Roles a Dto y agrupamos en una lista.
        List<RolDto> rolDtos = roles.stream()
            .map(usuariosMapperAuto::toDto)
            .toList();
        log.info("Se encontraron {} roles", rolDtos.size());
        return new ListarRolesResponseDto(rolDtos);
    }

    /**
     * Lista todos los permisos del sistema.
     * @return DTO con la lista de permisos
     */
    @Transactional(readOnly = true)
    public ListarPermisosResponseDto listarPermisos(ListarPermisosRequestDto dto) {
        log.info("Listando todos los permisos");
        List<Permiso> permisos = permisoRepository.findAll();
        List<PermisoDto> permisoDtos = permisos.stream()
            .map(usuariosMapperAuto::toDto)
            .toList();
        log.info("Se encontraron {} permisos", permisoDtos.size());
        return new ListarPermisosResponseDto(permisoDtos);
    }
    
    /**
     * Genera el nombre de un permiso basado en el recurso y la acción.
     * Formato: ACCION_RECURSO (ej: CREAR_USUARIOS)
     */
    private String generarNombrePermiso(String recurso, AccionPermiso accion) {
        return accion.name() + "_" + recurso.toUpperCase();
    }
    
    /**
     * Genera la descripción de un permiso basado en el recurso y la acción.
     */
    private String generarDescripcionPermiso(String recurso, AccionPermiso accion) {
        String accionDesc = switch (accion) {
            case CREAR -> "Crear";
            case LEER -> "Leer/Consultar";
            case ACTUALIZAR -> "Actualizar/Modificar";
            case ELIMINAR -> "Eliminar";
            case EJECUTAR -> "Ejecutar";
        };
        
        return "Permiso para " + accionDesc.toLowerCase() + " " + recurso;
    }

}