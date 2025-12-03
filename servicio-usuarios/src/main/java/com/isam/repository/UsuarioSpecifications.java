package com.isam.repository;

import com.isam.model.Rol;
import com.isam.model.Usuario;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

/**
 * Especificaciones para consultas dinámicas de usuarios.
 */
public class UsuarioSpecifications {

    //TODO - Aqui hay un metodo decrepted.
    public static Specification<Usuario> conFiltros(Optional<String> idUsuario, 
                                                    Optional<String> nombreUsuario, 
                                                    Optional<String> idRol) {
        return Specification.where(idUsuarioContains(idUsuario))
                .and(nombreUsuarioContains(nombreUsuario))
                .and(tieneRol(idRol));
    }

    private static Specification<Usuario> idUsuarioContains(Optional<String> idUsuario) {
        return (root, query, builder) -> 
            idUsuario.map(id -> builder.equal(root.get("idUsuario"), id))
                     .orElse(null);
    }

    private static Specification<Usuario> nombreUsuarioContains(Optional<String> nombreUsuario) {
        return (root, query, builder) -> 
            nombreUsuario.map(nombre -> builder.like(
                    builder.lower(root.get("nombreUsuario")), 
                    "%" + nombre.toLowerCase() + "%"))
                .orElse(null);
    }

    private static Specification<Usuario> tieneRol(Optional<String> idRol) {
        return (root, query, builder) -> {
            if (idRol.isEmpty()) {
                return null;
            }
            Join<Usuario, Rol> rolesJoin = root.join("roles", JoinType.INNER);
            return builder.equal(rolesJoin.get("idRol"), idRol.get());
        };
    }
}
