package com.msUsuario.msUsuario.service;

import com.msUsuario.msUsuario.model.Usuario;
import com.msUsuario.msUsuario.repository.UsuarioRepository;
//import com.msUsuario.msUsuario.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public Usuario crearUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public void eliminarUsuario(int id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public Usuario actualizarUsuario(int id, Usuario usuario) {
        Optional<Usuario> existente = usuarioRepository.findById(id);
        if (existente.isPresent()) {
            usuario.setIdUsuario(id);
            return usuarioRepository.save(usuario);
        }
        return null;
    }

    @Override
    public Usuario iniciarSesion(String correo, String password) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getCorreoUsuario().equals(correo) && u.getPassUsuario().equals(password))
                .findFirst().orElse(null);
    }

    @Override
    public void cerrarSesion() {
        // Aquí podrías eliminar un token o hacer logout manual
    }

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }
}