package com.isam.service;

import com.isam.repository.UsuarioRepository;
import com.isam.repository.PermisoRepository;
import com.isam.repository.RolRepository;
import com.isam.repository.SesionRepository;

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

}