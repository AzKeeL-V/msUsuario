package com.msUsuario.msUsuario.service;

import com.msUsuario.msUsuario.model.Rol;
import java.util.List;
import java.util.Optional;

public interface RolService {
    Rol crearRol(Rol rol);
    Rol actualizarRol(Long id, Rol rol);
    void eliminarRol(Long id);
    Optional<Rol> obtenerRolPorId(Long id);
    List<Rol> obtenerTodosRoles();
}