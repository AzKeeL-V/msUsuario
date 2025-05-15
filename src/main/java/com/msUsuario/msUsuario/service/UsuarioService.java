package com.msUsuario.msUsuario.service;

import com.msUsuario.msUsuario.model.Usuario;

import java.util.List;

public interface UsuarioService {
    Usuario crearUsuario(Usuario usuario);
    void eliminarUsuario(int id);
    Usuario actualizarUsuario(int id, Usuario usuario);
    Usuario iniciarSesion(String correo, String password);
    void cerrarSesion(); // Simulación
    List<Usuario> obtenerTodos();
}