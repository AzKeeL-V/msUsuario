package com.usuario.usuario.service;

import java.util.List;
import java.util.Optional;

import com.usuario.usuario.model.Rol;

public interface RolService {
    Rol crearRol(Rol rol);
    Rol actualizarRol(Long id, Rol rol);
    // Cambiado de eliminarRol a desactivarRol para reflejar la eliminación lógica
    Rol desactivarRol(Long id);
    // Nuevo método para reactivar un rol
    Rol reactivarRol(Long id);
    // Este método ahora buscará roles activos por defecto
    Optional<Rol> obtenerRolPorId(Long id);
    // Nuevo método para obtener solo roles activos
    List<Rol> obtenerTodosRolesActivos();
    // Este método puede obtener todos los roles, incluyendo inactivos
    List<Rol> obtenerTodosRoles();
}