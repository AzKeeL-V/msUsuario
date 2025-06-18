package com.usuario.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usuario.usuario.model.Rol;
import com.usuario.usuario.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Busca un usuario por su correo electrónico y estado
    Optional<Usuario> findByCorreoUsuarioAndEstadoUsuario(String correoUsuario, Boolean estadoUsuario);

    // Busca usuarios por el ID de la tienda y estado
    List<Usuario> findByIdTiendaAndEstadoUsuario(long idTienda, Boolean estadoUsuario);

    // Busca usuarios por estado
    List<Usuario> findByEstadoUsuario(Boolean estadoUsuario);

    // Sobrescribe el método findById para considerar solo usuarios activos por defecto
    Optional<Usuario> findByIdUsuarioAndEstadoUsuario(Integer idUsuario, Boolean estadoUsuario);
    
    List<Usuario> findByRolAndEstadoUsuario(Rol rol, Boolean estadoUsuario);
}