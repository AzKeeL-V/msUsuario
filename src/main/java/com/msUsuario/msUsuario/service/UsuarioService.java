package com.msUsuario.msUsuario.service;
import com.msUsuario.msUsuario.model.*;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Usuario crearUsuario(Usuario usuario);
    Usuario actualizarUsuario(Integer id, Usuario usuario);
    void eliminarUsuario(Integer id);
    Optional<Usuario> obtenerUsuarioPorId(Integer id);
    List<Usuario> obtenerTodosUsuarios();
    List<Permiso> obtenerPermisosUsuario(Integer idUsuario);
}