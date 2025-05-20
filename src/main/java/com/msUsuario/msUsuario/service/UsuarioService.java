package com.msUsuario.msUsuario.service;

import com.msUsuario.msUsuario.model.Permiso;
import com.msUsuario.msUsuario.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Usuario crearUsuario(Usuario usuario);
    Usuario actualizarUsuario(Integer id, Usuario usuario);
    void eliminarUsuario(Integer id);
    Optional<Usuario> obtenerUsuarioPorId(Integer id);
    List<Usuario> obtenerTodosUsuarios();
    List<Permiso> obtenerPermisosUsuario(Integer idUsuario);
    Usuario desactivarUsuario(Integer id); // Devuelve el usuario para confirmar estado o lanza excepción
    Usuario asignarRol(Integer idUsuario, Long rolId);
    List<Usuario> listarUsuariosPorTienda(Integer idTienda);
}