package com.msUsuario.msUsuario.service;

import com.msUsuario.msUsuario.model.Rol;
import com.msUsuario.msUsuario.repository.RolRepository;
import jakarta.persistence.EntityNotFoundException; // Para manejo de errores
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public Rol crearRol(Rol rol) {
        // Validar si ya existe un rol con el mismo nombre
        if(rolRepository.findByNombreRol(rol.getNombreRol()).isPresent()){
            throw new IllegalArgumentException("Ya existe un rol con el nombre: " + rol.getNombreRol());
        }
        return rolRepository.save(rol);
    }

    @Override
    public Rol actualizarRol(Long id, Rol rolDetails) {
        Rol existingRol = rolRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));

        // Validar si el nuevo nombre de rol ya existe en otro rol (si se cambia el nombre)
        if (rolDetails.getNombreRol() != null && !rolDetails.getNombreRol().equals(existingRol.getNombreRol())) {
            rolRepository.findByNombreRol(rolDetails.getNombreRol()).ifPresent(r -> {
                if (!r.getId().equals(id)) {
                    throw new IllegalArgumentException("El nombre de rol '" + rolDetails.getNombreRol() + "' ya está en uso.");
                }
            });
            existingRol.setNombreRol(rolDetails.getNombreRol());
        }

        if (rolDetails.getPermisosRol() != null) {
            existingRol.setPermisosRol(rolDetails.getPermisosRol());
        }
        return rolRepository.save(existingRol);
    }

    @Override
    public void eliminarRol(Long id) {
        if (!rolRepository.existsById(id)) {
            throw new EntityNotFoundException("Rol no encontrado con id: " + id + " para eliminar.");
        }
        // Considerar verificar si hay usuarios asignados a este rol antes de eliminar
        rolRepository.deleteById(id);
    }

    @Override
    public Optional<Rol> obtenerRolPorId(Long id) {
        return rolRepository.findById(id);
    }

    @Override
    public List<Rol> obtenerTodosRoles() {
        return rolRepository.findAll();
    }
}