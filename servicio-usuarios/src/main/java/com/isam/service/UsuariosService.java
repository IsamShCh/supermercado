package com.isam.service;

import com.isam.dto.autenticacion.IniciarSesionRequestDto;
import com.isam.dto.autenticacion.IniciarSesionResponseDto;
import com.isam.dto.autenticacion.VerificarTokenRequestDto;
import com.isam.dto.autenticacion.VerificarTokenResponseDto;
import com.isam.model.Sesion;
import com.isam.model.enums.EstadoSesion;
import com.isam.util.JwtUtil;
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
import com.isam.dto.usuario.ConsultarUsuariosRequestDto;
import com.isam.dto.usuario.ConsultarUsuariosResponseDto;
import com.isam.dto.usuario.CrearUsuarioRequestDto;
import com.isam.dto.usuario.CrearUsuarioResponseDto;
import com.isam.dto.usuario.UsuarioDto;
import com.isam.mapper.UsuariosMapper;
import com.isam.model.Permiso;
import com.isam.model.Rol;
import com.isam.model.Usuario;
import com.isam.model.enums.AccionPermiso;
import com.isam.model.enums.EstadoUsuario;
import com.isam.repository.UsuarioRepository;
import com.isam.repository.PermisoRepository;
import com.isam.repository.RolRepository;
import com.isam.repository.SesionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.grpc.Status;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    /**
     * Inicia sesión en el sistema.
     * @param dto Credenciales del usuario
     * @return Token JWT y datos del usuario
     */
    @Transactional
    public IniciarSesionResponseDto iniciarSesion(IniciarSesionRequestDto dto) {
        log.info("Intento de inicio de sesión para usuario: {}", dto.nombreUsuario());

        // Buscar usuario
        Usuario usuario = usuarioRepository.findByNombreUsuario(dto.nombreUsuario())
            .orElseThrow(() -> Status.UNAUTHENTICATED
                .withDescription("Credenciales inválidas")
                .asRuntimeException());

        // Verificar contraseña
        if (!passwordEncoder.matches(dto.password(), usuario.getHashContrasena())) {
            throw Status.UNAUTHENTICATED
                .withDescription("Credenciales inválidas")
                .asRuntimeException();
        }

        // Verificar estado del usuario
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw Status.PERMISSION_DENIED
                .withDescription("El usuario no está activo")
                .asRuntimeException();
        }

        // Actualizar fecha de último acceso
        usuario.setFechaUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        // Generar Token
        String token = jwtUtil.generateToken(usuario);

        // Crear Sesión
        Sesion sesion = new Sesion(token, usuario);
        sesionRepository.save(sesion);

        log.info("Sesión iniciada exitosamente para: {}", usuario.getNombreUsuario());
        
        return new IniciarSesionResponseDto(token, usuariosMapper.toDto(usuario));
    }

    /**
     * Cierra la sesión invalidando el token.
     * @param token Token JWT a invalidar
     */
    @Transactional
    public void cerrarSesion(String token) {
        log.info("Cerrando sesión para token: {}...", token != null && token.length() > 10 ? token.substring(0, 10) : "null");

        if (token == null || token.isBlank()) {
            
            throw Status.INVALID_ARGUMENT
                .withDescription("No se puede cerrar sesion. Token no proporcionado")
                .asRuntimeException();
        }

        // Eliminar prefijo "Bearer " si existe
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Buscar sesión
        Optional<Sesion> sesionOpt = sesionRepository.findByTokenJWT(token);
        
        if (sesionOpt.isPresent()) {

            if(sesionOpt.get().getEstado() == EstadoSesion.CERRADA){
                throw Status.FAILED_PRECONDITION
                    .withDescription("La sesión ya está cerrada")
                    .asRuntimeException();
            }

            Sesion sesion = sesionOpt.get();
            sesion.setEstado(EstadoSesion.CERRADA);
            sesion.setFechaHoraFin(LocalDateTime.now());
            sesionRepository.save(sesion);
            log.info("Sesión cerrada exitosamente");
        } else {
            log.warn("No se encontró sesión activa para el token proporcionado");
            throw Status.NOT_FOUND
                .withDescription("No se encontró sesión activa para el token proporcionado")
                .asRuntimeException();
        }
    }

    /**
     * Verifica si un token JWT es válido.
     * @param dto Token a verificar
     * @return Resultado de la verificación y datos del usuario
     */
    @Transactional(readOnly = true)
    public VerificarTokenResponseDto verificarToken(VerificarTokenRequestDto dto) {
        String token = dto.tokenJwt();
        
        if (token == null || token.isBlank()) {
            return new VerificarTokenResponseDto(false, Optional.empty(), Optional.empty(), List.of());
        }
        
        // Eliminar prefijo "Bearer " si existe
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validar estructura y firma del token
        if (!jwtUtil.validateToken(token)) {
            return new VerificarTokenResponseDto(false, Optional.empty(), Optional.empty(), List.of());
        }

        // Verificar estado de la sesión en BD
        Optional<Sesion> sesionOpt = sesionRepository.findByTokenJWT(token);
        if (sesionOpt.isEmpty() || sesionOpt.get().getEstado() != EstadoSesion.ACTIVA) {
            return new VerificarTokenResponseDto(false, Optional.empty(), Optional.empty(), List.of());
        }

        // Extraer datos
        String idUsuario = jwtUtil.extractIdUsuario(token);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        
        if (usuario == null) {
            return new VerificarTokenResponseDto(false, Optional.empty(), Optional.empty(), List.of());
        }

        List<RolDto> roles = usuario.getRoles().stream()
            .map(usuariosMapper::toDto)
            .toList();

        return new VerificarTokenResponseDto(
            true, 
            Optional.of(usuario.getIdUsuario()), 
            Optional.of(usuario.getNombreUsuario()), 
            roles
        );
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * @param dto DTO con los datos del usuario a crear
     * @return DTO con los datos del usuario creado
     */
    @Transactional
    @PreAuthorize("hasAuthority('CREAR_USUARIOS')")
    public CrearUsuarioResponseDto crearUsuario(CrearUsuarioRequestDto dto) {
        log.info("Creando usuario con nombre: {}", dto.nombreUsuario());

        // Verificar si ya existe un usuario con ese nombre
        if (usuarioRepository.existsByNombreUsuario(dto.nombreUsuario())) {
            throw Status.ALREADY_EXISTS
                .withDescription("Ya existe un usuario con el nombre '" + dto.nombreUsuario() + "'")
                .asRuntimeException();
        }

        // Verificar que se proporcionó al menos un rol
        if (dto.idRoles() == null || dto.idRoles().isEmpty()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Debe asignar al menos un rol al usuario")
                .asRuntimeException();
        }

        // Obtener los roles solicitados
        List<Rol> roles = rolRepository.findAllById(dto.idRoles());
        
        // Verificar que todos los roles existen
        if (roles.size() != dto.idRoles().size()) {
            List<String> idsEncontrados = roles.stream()
                .map(Rol::getIdRol)
                .toList();
            
            List<String> idsNoEncontrados = dto.idRoles().stream()
                .filter(id -> !idsEncontrados.contains(id))
                .toList();
            
            throw Status.NOT_FOUND
                .withDescription("Roles no encontrados: " + String.join(", ", idsNoEncontrados))
                .asRuntimeException();
        }

        // Generar hash contraseña
        String hashContrasena = passwordEncoder.encode(dto.password());

        // Crear la entidad Usuario
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(dto.nombreUsuario());
        usuario.setHashContrasena(hashContrasena);
        usuario.setNombreCompleto(dto.nombreCompleto());
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setRequiereCambioContrasena(false);
        usuario.setRoles(roles);

        // Guardar el usuario
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente con ID: {}", usuarioGuardado.getIdUsuario());

        // Convertir a DTO y retornar
        UsuarioDto usuarioDto = usuariosMapper.toDto(usuarioGuardado);
        return new CrearUsuarioResponseDto(usuarioDto);
    }

    /**
     * Crea un nuevo rol en el sistema.
     * @param dto DTO con los datos del rol a crear
     * @return DTO con los datos del rol creado
     */
    @Transactional
    @PreAuthorize("hasAuthority('CREAR_ROLES')")
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
     * Inicializa los permisos del sistema y el usuario admin por defecto si no existen.
     * Este método se ejecuta al iniciar la aplicación y crea:
     * 1. Los permisos básicos para cada recurso y acción del sistema
     * 2. Un rol "ADMIN" con todos los permisos
     * 3. Un usuario "admin" con el rol ADMIN (solo si la BD está vacía)
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
        
        // Inicializar usuario admin solo si la BD está vacía
        inicializarUsuarioAdmin();
    }
    
    /**
     * Crea el usuario admin por defecto si no existen usuarios en el sistema.
     * Credenciales:
     * - Usuario: admin
     * - Contraseña: admin123
     *
     * IMPORTANTE: Esta contraseña debe cambiarse en el primer inicio de sesión.
     */
    private void inicializarUsuarioAdmin() {
        // Verificar si ya existen usuarios en el sistema
        long cantidadUsuarios = usuarioRepository.count();
        
        if (cantidadUsuarios > 0) {
            log.info("Ya existen {} usuarios en el sistema. No se creará el usuario admin por defecto.", cantidadUsuarios);
            return;
        }
        
        log.info("Base de datos vacía. Creando usuario admin por defecto...");
        
        // Crear o buscar rol ADMIN
        Rol rolAdmin = rolRepository.findByNombreRol("ADMIN")
            .orElseGet(() -> {
                log.info("Creando rol ADMIN...");
                Rol nuevoRol = new Rol();
                nuevoRol.setNombreRol("ADMIN");
                nuevoRol.setDescripcionRol("Administrador del sistema con todos los permisos");
                nuevoRol.setPermisos(new java.util.ArrayList<>()); // Inicializar lista vacía
                return rolRepository.save(nuevoRol);
            });
        
        // Asignar todos los permisos al rol ADMIN
        List<Permiso> todosLosPermisos = permisoRepository.findAll();
        
        // Asegurar que la lista de permisos esté inicializada
        if (rolAdmin.getPermisos() == null) {
            rolAdmin.setPermisos(new java.util.ArrayList<>());
        }
        
        rolAdmin.getPermisos().clear();
        rolAdmin.getPermisos().addAll(todosLosPermisos);
        rolRepository.save(rolAdmin);
        log.info("Se asignaron {} permisos al rol ADMIN", todosLosPermisos.size());
        
        // Crear usuario admin
        Usuario admin = new Usuario();
        admin.setNombreUsuario("admin");
        admin.setHashContrasena(passwordEncoder.encode("admin123"));
        admin.setNombreCompleto("Administrador del Sistema");
        admin.setEstado(EstadoUsuario.ACTIVO);
        admin.setFechaCreacion(LocalDateTime.now());
        admin.setRequiereCambioContrasena(true); // Forzar cambio de contraseña
        admin.setRoles(List.of(rolAdmin));
        
        usuarioRepository.save(admin);
        
    }
    
    /**
     * Asigna permisos a un rol existente.
     * @param dto DTO con los datos de asignación de permisos
     * @return DTO de respuesta vacío en caso de éxito
     */
    @Transactional
    @PreAuthorize("hasAuthority('ACTUALIZAR_ROLES')")
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

        // Invalidar todas las sesiones activas de los usuarios que poseen este rol
        usuarioRepository.findByRolesContaining(rol).forEach(usuario -> {
            sesionRepository.findByUsuarioIdUsuario(usuario.getIdUsuario()).stream()
                .filter(sesion -> sesion.getEstado() == EstadoSesion.ACTIVA)
                .forEach(sesion -> {
                    sesion.setEstado(EstadoSesion.CERRADA);
                    sesion.setFechaHoraFin(LocalDateTime.now());
                    sesionRepository.save(sesion);
                    log.info("Sesión {} invalidada por cambio de permisos del rol '{}' para usuario '{}'",
                            sesion.getTokenJWT(), rol.getNombreRol(), usuario.getNombreUsuario());
                });
        });
        
        log.info("Se asignaron {} permisos al rol '{}'",
            permisosAsignar.size(), rol.getNombreRol());
        
        return new AsignarPermisosResponseDto();
    }

    /**
     * Lista todos los roles del sistema.
     * @return DTO con la lista de roles
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('LEER_ROLES')")
    public ListarRolesResponseDto listarRoles(ListarRolesRequestDto dto) {
        log.info("Listando todos los roles");
        List<Rol> roles = rolRepository.findAll();
        // Mapeamos automaticamente todos los Roles a Dto y agrupamos en una lista.
        List<RolDto> rolDtos = roles.stream()
            .map(usuariosMapper::toDto)
            .toList();
        log.info("Se encontraron {} roles", rolDtos.size());
        return new ListarRolesResponseDto(rolDtos);
    }

    /**
     * Lista todos los permisos del sistema.
     * @return DTO con la lista de permisos
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('LEER_PERMISOS')")
    public ListarPermisosResponseDto listarPermisos(ListarPermisosRequestDto dto) {
        log.info("Listando todos los permisos");
        List<Permiso> permisos = permisoRepository.findAll();
        List<PermisoDto> permisoDtos = permisos.stream()
            .map(usuariosMapper::toDto)
            .toList();
        log.info("Se encontraron {} permisos", permisoDtos.size());
        return new ListarPermisosResponseDto(permisoDtos);
    }

    /**
     * Consulta usuarios basándose en criterios de filtrado.
     * @param dto DTO con los filtros de búsqueda (id, nombre, rol)
     * @return DTO con la lista de usuarios encontrados
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ConsultarUsuariosResponseDto consultarUsuarios(ConsultarUsuariosRequestDto dto) {
        log.info("Consultando usuarios con filtros: idUsuario={}, nombreUsuario={}, idRol={}", 
            dto.idUsuario().orElse("N/A"), 
            dto.nombreUsuario().orElse("N/A"), 
            dto.idRol().orElse("N/A"));

        // Crear especificación de búsqueda
        org.springframework.data.jpa.domain.Specification<Usuario> spec = 
            com.isam.repository.UsuarioSpecifications.conFiltros(
                dto.idUsuario(), 
                dto.nombreUsuario(), 
                dto.idRol()
            );
        // org.springframework.data.jpa.domain.Specification<Usuario> spec = buildSpecification(dto); // En enfoque de arriba es mejor

        // Ejecutar consulta
        List<Usuario> usuarios = usuarioRepository.findAll(spec);

        // Convertir a DTOs
        List<UsuarioDto> usuarioDtos = usuarios.stream()
            .map(usuariosMapper::toDto)
            .toList();

        log.info("Se encontraron {} usuarios", usuarioDtos.size());
        return new com.isam.dto.usuario.ConsultarUsuariosResponseDto(usuarioDtos);
    }

    /**
     * Construye la especificación de búsqueda para usuarios (Estilo Catalogo).
     */
    private org.springframework.data.jpa.domain.Specification<Usuario> buildSpecification(com.isam.dto.usuario.ConsultarUsuariosRequestDto dto) {
        return (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // Filtro por ID Usuario
            if (dto.idUsuario().isPresent() && !dto.idUsuario().get().isEmpty()) {
                predicates.add(cb.equal(root.get("idUsuario"), dto.idUsuario().get()));
            }

            // Filtro por Nombre Usuario (LIKE)
            if (dto.nombreUsuario().isPresent() && !dto.nombreUsuario().get().isEmpty()) {
                predicates.add(cb.like(
                    cb.lower(root.get("nombreUsuario")), 
                    "%" + dto.nombreUsuario().get().toLowerCase() + "%"
                ));
            }

            // Filtro por Rol (JOIN)
            if (dto.idRol().isPresent() && !dto.idRol().get().isEmpty()) {
                jakarta.persistence.criteria.Join<Usuario, Rol> rolesJoin = root.join("roles", jakarta.persistence.criteria.JoinType.INNER);
                predicates.add(cb.equal(rolesJoin.get("idRol"), dto.idRol().get()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
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