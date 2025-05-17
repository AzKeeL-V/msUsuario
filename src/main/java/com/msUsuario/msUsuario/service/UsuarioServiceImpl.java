package com.msUsuario.msUsuario.service;

import com.msUsuario.msUsuario.model.Usuario;
import com.msUsuario.msUsuario.model.Permiso;
import com.msUsuario.msUsuario.repository.UsuarioRepository;
import com.msUsuario.msUsuario.repository.RolRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public Usuario crearUsuario(Usuario usuario) {
        // Asegurarse de que el rol exista antes de crear el usuario
        if (usuario.getRol() != null && usuario.getRol().getId() != null && rolRepository.existsById(usuario.getRol().getId())) {
            return usuarioRepository.save(usuario);
        }
        // Manejar el caso donde el rol no existe (lanzar excepción, etc.)
        return null;
    }

    @Override
    public Usuario actualizarUsuario(Integer id, Usuario usuario) {
        return usuarioRepository.findById(id)
                .map(existingUsuario -> {
                    existingUsuario.setNomUsuario(usuario.getNomUsuario());
                    existingUsuario.setApUsuario(usuario.getApUsuario());
                    // Asegurarse de que el rol exista antes de actualizar el usuario
                    if (usuario.getRol() != null && usuario.getRol().getId() != null && rolRepository.existsById(usuario.getRol().getId())) {
                        existingUsuario.setRol(usuario.getRol());
                    }
                    existingUsuario.setCorreoUsuario(usuario.getCorreoUsuario());
                    existingUsuario.setPassUsuario(usuario.getPassUsuario());
                    existingUsuario.setIdTienda(usuario.getIdTienda());
                    return usuarioRepository.save(existingUsuario);
                })
                .orElse(null);
    }

    @Override
    public void eliminarUsuario(Integer id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public Optional<Usuario> obtenerUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    public List<Permiso> obtenerPermisosUsuario(Integer idUsuario) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(idUsuario);
        return usuarioOptional.map(usuario -> usuario.getRol().getPermisosRol())
                .orElse(List.of());
    }
}