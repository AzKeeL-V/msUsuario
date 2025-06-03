package com.msUsuario.msUsuario.service;

import com.msUsuario.msUsuario.model.Usuario;
import com.msUsuario.msUsuario.model.Permiso;
import com.msUsuario.msUsuario.model.Rol;
import com.msUsuario.msUsuario.repository.UsuarioRepository;
import com.msUsuario.msUsuario.repository.RolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        // Al crear un usuario, asegúrate de que el estado sea activo por defecto
        if (usuarioRepository.findByCorreoUsuarioAndEstadoUsuario(usuario.getCorreoUsuario(), true).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico '" + usuario.getCorreoUsuario() + "' ya está en uso por un usuario activo.");
        }

        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new IllegalArgumentException("El rol es obligatorio para crear un usuario.");
        }
        Rol rol = rolRepository.findById(usuario.getRol().getId())
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + usuario.getRol().getId()));
        usuario.setRol(rol);

        // Aseguramos que el usuario se crea como activo
        usuario.setEstadoUsuario(true);

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Integer id, Usuario usuarioDetails) {
        // Buscar al usuario por ID o lanzar excepción si no existe, aquí no importa si está activo o inactivo inicialmente para poder actualizarlo
        Usuario existingUsuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        if (usuarioDetails.getNomUsuario() != null) {
            existingUsuario.setNomUsuario(usuarioDetails.getNomUsuario());
        }
        if (usuarioDetails.getApUsuario() != null) {
            existingUsuario.setApUsuario(usuarioDetails.getApUsuario());
        }

        if (usuarioDetails.getCorreoUsuario() != null && !usuarioDetails.getCorreoUsuario().equals(existingUsuario.getCorreoUsuario())) {
            // Verificar si el nuevo correo ya está en uso por OTRO usuario activo
            if (usuarioRepository.findByCorreoUsuarioAndEstadoUsuario(usuarioDetails.getCorreoUsuario(), true).isPresent()) {
                throw new IllegalArgumentException("El correo electrónico '" + usuarioDetails.getCorreoUsuario() + "' ya está en uso por otro usuario activo.");
            }
            existingUsuario.setCorreoUsuario(usuarioDetails.getCorreoUsuario());
        }

        if (usuarioDetails.getRol() != null && usuarioDetails.getRol().getId() != null) {
            Rol nuevoRol = rolRepository.findById(usuarioDetails.getRol().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + usuarioDetails.getRol().getId()));
            existingUsuario.setRol(nuevoRol);
        }

        if (usuarioDetails.getPassUsuario() != null && !usuarioDetails.getPassUsuario().isEmpty()) {
            existingUsuario.setPassUsuario(usuarioDetails.getPassUsuario());
        }

        if (usuarioDetails.getIdTienda() != null) {
            existingUsuario.setIdTienda(usuarioDetails.getIdTienda());
        }
        
        // Permite actualizar el estado del usuario (activo/inactivo)
        if (usuarioDetails.getEstadoUsuario() != null) {
            existingUsuario.setEstadoUsuario(usuarioDetails.getEstadoUsuario());
        }

        return usuarioRepository.save(existingUsuario);
    }

    // Este método ahora se encargará de desactivar el usuario (eliminación lógica)
    @Override
    @Transactional
    public Usuario desactivarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findByIdUsuarioAndEstadoUsuario(id, true) // Busca solo usuarios activos
                .orElseThrow(() -> new EntityNotFoundException("Usuario activo no encontrado con id: " + id));

        usuario.setEstadoUsuario(false); // Cambia el estado a inactivo
        return usuarioRepository.save(usuario);
    }

    // Nuevo método para reactivar un usuario
    @Override
    @Transactional
    public Usuario reactivarUsuario(Integer id) {
        // Busca usuarios inactivos para reactivar
        Usuario usuario = usuarioRepository.findByIdUsuarioAndEstadoUsuario(id, false)
                .orElseThrow(() -> new EntityNotFoundException("Usuario inactivo no encontrado con id: " + id));

        usuario.setEstadoUsuario(true); // Cambia el estado a activo
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> obtenerUsuarioPorId(Integer id) {
        // Por defecto, este método buscará usuarios activos
        return usuarioRepository.findByIdUsuarioAndEstadoUsuario(id, true);
    }

    @Override
    public List<Usuario> obtenerTodosUsuariosActivos() {
        // Retorna solo los usuarios cuyo estado es 'true' (activos)
        return usuarioRepository.findByEstadoUsuario(true);
    }

    @Override
    public List<Usuario> obtenerTodosUsuarios() {
        // Retorna todos los usuarios, incluyendo activos e inactivos
        return usuarioRepository.findAll();
    }

    @Override
    public List<Permiso> obtenerPermisosUsuario(Integer idUsuario) {
        // Considera solo usuarios activos para obtener permisos
        Usuario usuario = usuarioRepository.findByIdUsuarioAndEstadoUsuario(idUsuario, true)
                .orElseThrow(() -> new EntityNotFoundException("Usuario activo no encontrado con id: " + idUsuario));

        if (usuario.getRol() != null && usuario.getRol().getPermisosRol() != null) {
            return usuario.getRol().getPermisosRol();
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public Usuario asignarRol(Integer idUsuario, Long rolId) {
        // Considera solo usuarios activos para asignar roles
        Usuario usuario = usuarioRepository.findByIdUsuarioAndEstadoUsuario(idUsuario, true)
                .orElseThrow(() -> new EntityNotFoundException("Usuario activo no encontrado con id: " + idUsuario));
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + rolId));

        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> listarUsuariosPorTienda(Integer idTienda) {
        // Por defecto, listar usuarios activos por tienda
        return usuarioRepository.findByIdTiendaAndEstadoUsuario(idTienda, true);
    }
}