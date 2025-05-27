package com.msUsuario.msUsuario.service;

import com.msUsuario.msUsuario.model.Rol;
import com.msUsuario.msUsuario.repository.RolRepository;
import jakarta.persistence.EntityNotFoundException; // Para manejo de errores si no se encuentra una entidad
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    // Constructor que inyecta el repositorio de roles para poder acceder a la base de datos
    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public Rol crearRol(Rol rol) {
        // Antes de guardar el rol, verificamos si ya existe un rol con el mismo nombre
        if(rolRepository.findByNombreRol(rol.getNombreRol()).isPresent()){
            throw new IllegalArgumentException("Ya existe un rol con el nombre: " + rol.getNombreRol());
        }
        // Si no existe, guardamos el nuevo rol
        return rolRepository.save(rol);
    }

    @Override
    public Rol actualizarRol(Long id, Rol rolDetails) {
        // Buscamos el rol por su ID, si no existe lanzamos una excepción
        Rol existingRol = rolRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));

        // Si se quiere cambiar el nombre del rol, verificamos que no esté repetido en otro rol
        if (rolDetails.getNombreRol() != null && !rolDetails.getNombreRol().equals(existingRol.getNombreRol())) {
            rolRepository.findByNombreRol(rolDetails.getNombreRol()).ifPresent(r -> {
                // Si encontramos otro rol con el mismo nombre y diferente ID, no permitimos el cambio
                if (!r.getId().equals(id)) {
                    throw new IllegalArgumentException("El nombre de rol '" + rolDetails.getNombreRol() + "' ya está en uso.");
                }
            });
            // Si el nombre es válido, lo actualizamos
            existingRol.setNombreRol(rolDetails.getNombreRol());
        }

        // Si se proporcionan nuevos permisos para el rol, los actualizamos también
        if (rolDetails.getPermisosRol() != null) {
            existingRol.setPermisosRol(rolDetails.getPermisosRol());
        }

        // Guardamos los cambios en la base de datos
        return rolRepository.save(existingRol);
    }

    @Override
    public void eliminarRol(Long id) {
        // Verificamos si el rol con ese ID existe antes de intentar eliminarlo
        if (!rolRepository.existsById(id)) {
            throw new EntityNotFoundException("Rol no encontrado con id: " + id + " para eliminar.");
        }
        // Aquí se podría validar si hay usuarios asociados a este rol antes de eliminarlo (opcional)
        rolRepository.deleteById(id);
    }

    @Override
    public Optional<Rol> obtenerRolPorId(Long id) {
        // Busca un rol por su ID y lo retorna como Optional
        return rolRepository.findById(id);
    }

    @Override
    public List<Rol> obtenerTodosRoles() {
        // Retorna todos los roles que existen en la base de datos
        return rolRepository.findAll();
    }
}
