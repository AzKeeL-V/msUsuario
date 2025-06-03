package com.msUsuario.msUsuario.repository;

import com.msUsuario.msUsuario.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    // Busca un rol por su nombre y estado
    Optional<Rol> findByNombreRolAndEstadoRol(String nombreRol, Boolean estadoRol);

    // Busca un rol por su ID y estado
    Optional<Rol> findByIdAndEstadoRol(Long id, Boolean estadoRol);

    // Busca roles por estado
    List<Rol> findByEstadoRol(Boolean estadoRol);

    // Busca un rol por su nombre (método original, mantenido por si aún lo usas)
    Optional<Rol> findByNombreRol(String nombreRol);
}