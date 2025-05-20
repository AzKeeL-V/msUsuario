package com.msUsuario.msUsuario.service;

import com.msUsuario.msUsuario.model.Usuario;
import com.msUsuario.msUsuario.model.Permiso;
import com.msUsuario.msUsuario.model.Rol;
import com.msUsuario.msUsuario.repository.UsuarioRepository;
import com.msUsuario.msUsuario.repository.RolRepository;
import jakarta.persistence.EntityNotFoundException; // Para manejo de errores
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Para asegurar atomicidad

import java.util.List;
import java.util.Optional;
import java.util.Collections;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    @Transactional // Asegura que la operación sea atómica
    public Usuario crearUsuario(Usuario usuario) {
        // Validar correo único
        if (usuarioRepository.findByCorreoUsuario(usuario.getCorreoUsuario()).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico '" + usuario.getCorreoUsuario() + "' ya está en uso.");
        }

        // Validar y asignar el rol
        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new IllegalArgumentException("El rol es obligatorio para crear un usuario.");
        }
        Rol rol = rolRepository.findById(usuario.getRol().getId())
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + usuario.getRol().getId()));
        usuario.setRol(rol); // Asignar la instancia de rol gestionada

        // Aquí se debería hashear la contraseña antes de guardarla
        // Ejemplo: usuario.setPassUsuario(passwordEncoder.encode(usuario.getPassUsuario()));
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Integer id, Usuario usuarioDetails) {
        Usuario existingUsuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        if (usuarioDetails.getNomUsuario() != null) {
            existingUsuario.setNomUsuario(usuarioDetails.getNomUsuario());
        }
        if (usuarioDetails.getApUsuario() != null) {
            existingUsuario.setApUsuario(usuarioDetails.getApUsuario());
        }
        // Validar correo único si se cambia
        if (usuarioDetails.getCorreoUsuario() != null && !usuarioDetails.getCorreoUsuario().equals(existingUsuario.getCorreoUsuario())) {
            if (usuarioRepository.findByCorreoUsuario(usuarioDetails.getCorreoUsuario()).isPresent()) {
                throw new IllegalArgumentException("El correo electrónico '" + usuarioDetails.getCorreoUsuario() + "' ya está en uso.");
            }
            existingUsuario.setCorreoUsuario(usuarioDetails.getCorreoUsuario());
        }
        // Actualizar rol si se proporciona uno nuevo y válido
        if (usuarioDetails.getRol() != null && usuarioDetails.getRol().getId() != null) {
            Rol nuevoRol = rolRepository.findById(usuarioDetails.getRol().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + usuarioDetails.getRol().getId()));
            existingUsuario.setRol(nuevoRol);
        }
        if (usuarioDetails.getPassUsuario() != null && !usuarioDetails.getPassUsuario().isEmpty()) {
            // Aquí se debería hashear la nueva contraseña
            // existingUsuario.setPassUsuario(passwordEncoder.encode(usuarioDetails.getPassUsuario()));
            existingUsuario.setPassUsuario(usuarioDetails.getPassUsuario()); // Temporalmente sin hashear
        }
        if (usuarioDetails.getIdTienda() != null) {
            existingUsuario.setIdTienda(usuarioDetails.getIdTienda());
        }
        return usuarioRepository.save(existingUsuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuario no encontrado con id: " + id + " para eliminar.");
        }
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
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + idUsuario));
        if (usuario.getRol() != null && usuario.getRol().getPermisosRol() != null) {
            return usuario.getRol().getPermisosRol();
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public Usuario desactivarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        // Lógica de desactivación: Si tuvieras un campo 'activo' en la entidad Usuario:
        // usuario.setActivo(false);
        // return usuarioRepository.save(usuario);
        // Como no existe, simplemente devolvemos el usuario o podríamos lanzar una excepción si no se puede "desactivar"
        System.out.println("Lógica de desactivación para usuario ID " + id + " pendiente (ej. campo 'activo').");
        return usuario; // Devolvemos el usuario encontrado por ahora
    }

    @Override
    @Transactional
    public Usuario asignarRol(Integer idUsuario, Long rolId) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + idUsuario));
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + rolId));
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> listarUsuariosPorTienda(Integer idTienda) {
        // Este método ya está definido en UsuarioRepository
        return usuarioRepository.findByIdTienda(idTienda);
    }
}