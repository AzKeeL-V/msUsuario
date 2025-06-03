package com.msUsuario.msUsuario.service;

import com.msUsuario.msUsuario.model.Permiso;
import com.msUsuario.msUsuario.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Usuario crearUsuario(Usuario usuario);
    Usuario actualizarUsuario(Integer id, Usuario usuario);
    // Cambiamos el nombre de eliminarUsuario a desactivarUsuario, para reflejar la eliminación lógica
    Usuario desactivarUsuario(Integer id);
    // Cambiamos el nombre de reactivarUsuario
    Usuario reactivarUsuario(Integer id); // Nuevo método para reactivar un usuario
    Optional<Usuario> obtenerUsuarioPorId(Integer id); // Este ahora debería buscar usuarios activos por defecto
    List<Usuario> obtenerTodosUsuariosActivos(); // Nuevo método para obtener solo usuarios activos
    List<Usuario> obtenerTodosUsuarios(); // Este puede obtener todos, incluyendo inactivos
    List<Permiso> obtenerPermisosUsuario(Integer idUsuario);
    Usuario asignarRol(Integer idUsuario, Long rolId);
    List<Usuario> listarUsuariosPorTienda(Integer idTienda);
}