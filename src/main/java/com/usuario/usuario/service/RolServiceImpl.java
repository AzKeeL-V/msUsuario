package com.usuario.usuario.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usuario.usuario.model.Rol;
import com.usuario.usuario.repository.RolRepository;
import com.usuario.usuario.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository; // Inyectamos UsuarioRepository

    /**
     * Constructor que inyecta los repositorios de roles y usuarios.
     *
     * @param rolRepository Repositorio que permite acceder y manipular los roles en la base de datos.
     * @param usuarioRepository Repositorio que permite acceder y manipular los usuarios en la base de datos.
     */
    public RolServiceImpl(RolRepository rolRepository, UsuarioRepository usuarioRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea un nuevo rol en la base de datos si no existe otro con el mismo nombre y estado activo.
     *
     * @param rol Objeto Rol que se desea crear.
     * @return El rol creado y persistido en la base de datos.
     * @throws IllegalArgumentException Si ya existe un rol activo con el mismo nombre.
     */
    @Override
    @Transactional
    public Rol crearRol(Rol rol) {
        // Validar que no exista un rol ACTIVO con el mismo nombre
        if (rolRepository.findByNombreRolAndEstadoRol(rol.getNombreRol(), true).isPresent()) {
            throw new IllegalArgumentException("Ya existe un rol activo con el nombre: " + rol.getNombreRol());
        }
        rol.setEstadoRol(true); // Aseguramos que el rol se crea como activo
        return rolRepository.save(rol);
    }

    /**
     * Actualiza los datos de un rol existente.
     * Permite modificar el nombre, permisos y el estado del rol (activo/inactivo).
     *
     * @param id          ID del rol a actualizar.
     * @param rolDetails  Datos nuevos del rol.
     * @return El rol actualizado.
     * @throws EntityNotFoundException  Si no se encuentra el rol con el ID dado.
     * @throws IllegalArgumentException Si se intenta cambiar el nombre del rol a uno ya existente y activo.
     */
    @Override
    @Transactional
    public Rol actualizarRol(Long id, Rol rolDetails) {
        Rol existingRol = rolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));

        if (rolDetails.getNombreRol() != null && !rolDetails.getNombreRol().equals(existingRol.getNombreRol())) {
            // Verificar si el nuevo nombre ya está en uso por otro rol ACTIVO
            rolRepository.findByNombreRolAndEstadoRol(rolDetails.getNombreRol(), true).ifPresent(r -> {
                if (!r.getId().equals(id)) {
                    throw new IllegalArgumentException("El nombre de rol '" + rolDetails.getNombreRol() + "' ya está en uso por otro rol activo.");
                }
            });
            existingRol.setNombreRol(rolDetails.getNombreRol());
        }

        if (rolDetails.getPermisosRol() != null) {
            existingRol.setPermisosRol(rolDetails.getPermisosRol());
        }

        // Permite actualizar el estado del rol (activo/inactivo)
        if (rolDetails.getEstadoRol() != null) {
            existingRol.setEstadoRol(rolDetails.getEstadoRol());
        }

        return rolRepository.save(existingRol);
    }

    /**
     * Desactiva lógicamente un rol por la respectiva ID.
     * Un rol no puede ser desactivado si hay usuarios activos vinculados a él.
     *
     * @param id ID del rol a desactivar.
     * @return El rol desactivado.
     * @throws EntityNotFoundException Si no existe un rol activo con ese ID.
     * @throws IllegalStateException Si el rol está vinculado a usuarios activos.
     */
    @Override
    @Transactional
    public Rol desactivarRol(Long id) {
        // Busca el rol por su ID y que esté activo
        Rol rol = rolRepository.findByIdAndEstadoRol(id, true)
                .orElseThrow(() -> new EntityNotFoundException("Rol activo no encontrado con id: " + id));

        // *** VALIDACIÓN CRUCIAL: Verificar si hay usuarios activos vinculados a este rol ***
        // Asegúrate de que tu UsuarioRepository tenga el método findByRolAndEstadoUsuario(Rol rol, Boolean estadoUsuario)
        if (!usuarioRepository.findByRolAndEstadoUsuario(rol, true).isEmpty()) {
            throw new IllegalStateException("No se puede desactivar el rol '" + rol.getNombreRol() + "' porque está vinculado a usuarios activos.");
        }

        rol.setEstadoRol(false); // Cambia el estado a inactivo
        return rolRepository.save(rol);
    }

    /**
     * Reactiva un rol que fue desactivado lógicamente.
     *
     * @param id ID del rol a reactivar.
     * @return El rol reactivado.
     * @throws EntityNotFoundException Si no existe un rol inactivo con ese ID.
     */
    @Override
    @Transactional
    public Rol reactivarRol(Long id) {
        // Busca el rol por su ID y que esté inactivo
        Rol rol = rolRepository.findByIdAndEstadoRol(id, false)
                .orElseThrow(() -> new EntityNotFoundException("Rol inactivo no encontrado con id: " + id));

        rol.setEstadoRol(true); // Cambia el estado a activo
        return rolRepository.save(rol);
    }

    /**
     * Busca un rol por su ID. Por defecto, solo busca roles activos.
     *
     * @param id ID del rol a buscar.
     * @return Optional que puede contener el rol activo encontrado o estar vacío si no existe.
     */
    @Override
    public Optional<Rol> obtenerRolPorId(Long id) {
        return rolRepository.findByIdAndEstadoRol(id, true); // Busca solo roles activos
    }

    /**
     * Obtiene todos los roles activos existentes en la base de datos.
     *
     * @return Lista de todos los roles activos.
     */
    @Override
    public List<Rol> obtenerTodosRolesActivos() {
        return rolRepository.findByEstadoRol(true); // Retorna solo roles activos
    }

    /**
     * Obtiene todos los roles existentes en la base de datos, incluyendo activos e inactivos.
     *
     * @return Lista de todos los roles.
     */
    @Override
    public List<Rol> obtenerTodosRoles() {
        return rolRepository.findAll(); // Retorna todos los roles
    }
}