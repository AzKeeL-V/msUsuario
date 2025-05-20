package com.msUsuario.msUsuario.controller;

import com.msUsuario.msUsuario.model.Usuario;
// import com.msUsuario.msUsuario.model.Permiso; // No se usa directamente aquí, pero sí en servicio
import com.msUsuario.msUsuario.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // Para manejo de excepciones

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
            return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Integer id, @RequestBody Usuario usuario) {
        try {
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Integer id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con id: " + id));
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodosUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodosUsuarios());
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Usuario> desactivarUsuario(@PathVariable Integer id) {
        try {
            // El servicio ahora devuelve el usuario para confirmar, o lanza excepción si no se encuentra.
            Usuario usuarioDesactivado = usuarioService.desactivarUsuario(id);
            return ResponseEntity.ok(usuarioDesactivado); // O NO_CONTENT si no se devuelve nada
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{id}/asignar-rol/{rolId}")
    public ResponseEntity<Usuario> asignarRol(@PathVariable Integer id, @PathVariable Long rolId) {
        try {
            Usuario usuarioActualizado = usuarioService.asignarRol(id, rolId);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (EntityNotFoundException e) {
            // Puede ser que el usuario o el rol no se encuentren
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/tienda/{idTienda}")
    public ResponseEntity<List<Usuario>> listarUsuariosPorTienda(@PathVariable Integer idTienda) {
        List<Usuario> usuariosEnTienda = usuarioService.listarUsuariosPorTienda(idTienda);
        if (usuariosEnTienda.isEmpty()) {
            // Devuelve OK con lista vacía en lugar de NOT_FOUND si la tienda existe pero no tiene usuarios
            return ResponseEntity.ok(usuariosEnTienda);
        }
        return ResponseEntity.ok(usuariosEnTienda);
    }
}