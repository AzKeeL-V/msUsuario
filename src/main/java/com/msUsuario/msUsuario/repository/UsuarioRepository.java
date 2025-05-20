package com.msUsuario.msUsuario.repository;

import com.msUsuario.msUsuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Busca un usuario por su correo electrónico
    Optional<Usuario> findByCorreoUsuario(String correoUsuario);

    // Busca usuarios por el ID de la tienda
    List<Usuario> findByIdTienda(Integer idTienda);
}