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

    // Repositorios utilizados para acceder a la base de datos
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    // Constructor con inyección de dependencias
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    @Transactional // Asegura que la operación completa se realice como una única transacción
    public Usuario crearUsuario(Usuario usuario) {
        // Validar que el correo electrónico no esté repetido
        if (usuarioRepository.findByCorreoUsuario(usuario.getCorreoUsuario()).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico '" + usuario.getCorreoUsuario() + "' ya está en uso.");
        }

        // Validar que el rol exista y esté asignado correctamente
        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new IllegalArgumentException("El rol es obligatorio para crear un usuario.");
        }
        Rol rol = rolRepository.findById(usuario.getRol().getId())
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + usuario.getRol().getId()));
        usuario.setRol(rol); // Asigna la instancia del rol recuperada de la BD

        // Aquí se debería cifrar la contraseña antes de guardarla
        // Ejemplo: usuario.setPassUsuario(passwordEncoder.encode(usuario.getPassUsuario()));

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Integer id, Usuario usuarioDetails) {
        // Buscar al usuario por ID o lanzar excepción si no existe
        Usuario existingUsuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        // Actualizar nombre si se proporciona
        if (usuarioDetails.getNomUsuario() != null) {
            existingUsuario.setNomUsuario(usuarioDetails.getNomUsuario());
        }
        // Actualizar apellido si se proporciona
        if (usuarioDetails.getApUsuario() != null) {
            existingUsuario.setApUsuario(usuarioDetails.getApUsuario());
        }

        // Validar y actualizar el correo si se proporciona y es distinto
        if (usuarioDetails.getCorreoUsuario() != null && !usuarioDetails.getCorreoUsuario().equals(existingUsuario.getCorreoUsuario())) {
            if (usuarioRepository.findByCorreoUsuario(usuarioDetails.getCorreoUsuario()).isPresent()) {
                throw new IllegalArgumentException("El correo electrónico '" + usuarioDetails.getCorreoUsuario() + "' ya está en uso.");
            }
            existingUsuario.setCorreoUsuario(usuarioDetails.getCorreoUsuario());
        }

        // Actualizar el rol si se proporciona un nuevo ID de rol válido
        if (usuarioDetails.getRol() != null && usuarioDetails.getRol().getId() != null) {
            Rol nuevoRol = rolRepository.findById(usuarioDetails.getRol().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + usuarioDetails.getRol().getId()));
            existingUsuario.setRol(nuevoRol);
        }

        // Actualizar contraseña si se proporciona y no está vacía
        if (usuarioDetails.getPassUsuario() != null && !usuarioDetails.getPassUsuario().isEmpty()) {
            // Aquí también se debería cifrar la contraseña antes de guardarla
            existingUsuario.setPassUsuario(usuarioDetails.getPassUsuario());
        }

        // Actualizar ID de tienda si se proporciona
        if (usuarioDetails.getIdTienda() != null) {
            existingUsuario.setIdTienda(usuarioDetails.getIdTienda());
        }

        // Guardar los cambios realizados en el usuario
        return usuarioRepository.save(existingUsuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {
        // Verificar si el usuario existe antes de intentar eliminarlo
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuario no encontrado con id: " + id + " para eliminar.");
        }
        usuarioRepository.deleteById(id);
    }

    @Override
    public Optional<Usuario> obtenerUsuarioPorId(Integer id) {
        // Retornar el usuario si existe o un Optional vacío si no
        return usuarioRepository.findById(id);
    }

    @Override
    public List<Usuario> obtenerTodosUsuarios() {
        // Retorna todos los usuarios existentes en la base de datos
        return usuarioRepository.findAll();
    }

    @Override
    public List<Permiso> obtenerPermisosUsuario(Integer idUsuario) {
        // Buscar usuario por ID y lanzar excepción si no existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + idUsuario));
        
        // Retornar los permisos asociados al rol del usuario o lista vacía si no hay
        if (usuario.getRol() != null && usuario.getRol().getPermisosRol() != null) {
            return usuario.getRol().getPermisosRol();
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public Usuario desactivarUsuario(Integer id) {
        // Buscar usuario por ID o lanzar excepción si no existe
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        // Lógica pendiente: se podría usar un campo booleano 'activo' para marcar el usuario como desactivado
        System.out.println("Lógica de desactivación para usuario ID " + id + " pendiente (ej. campo 'activo').");

        // Retornar el usuario tal como está (por ahora)
        return usuario;
    }

    @Override
    @Transactional
    public Usuario asignarRol(Integer idUsuario, Long rolId) {
        // Buscar usuario y rol por sus respectivos ID, lanzar excepción si alguno no existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + idUsuario));
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + rolId));

        // Asignar el rol al usuario
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> listarUsuariosPorTienda(Integer idTienda) {
        // Buscar usuarios que pertenezcan a una tienda específica por ID de tienda
        return usuarioRepository.findByIdTienda(idTienda);
    }
}
