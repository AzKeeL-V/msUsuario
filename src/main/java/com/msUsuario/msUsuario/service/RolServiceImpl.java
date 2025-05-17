package com.msUsuario.msUsuario.service;

import com.msUsuario.msUsuario.model.Rol;
import com.msUsuario.msUsuario.repository.RolRepository;
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
        return rolRepository.save(rol);
    }

    @Override
    public Rol actualizarRol(Long id, Rol rol) {
        return rolRepository.findById(id)
                .map(existingRol -> {
                    existingRol.setNombreRol(rol.getNombreRol());
                    existingRol.setPermisosRol(rol.getPermisosRol());
                    return rolRepository.save(existingRol);
                })
                .orElse(null);
    }

    @Override
    public void eliminarRol(Long id) {
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