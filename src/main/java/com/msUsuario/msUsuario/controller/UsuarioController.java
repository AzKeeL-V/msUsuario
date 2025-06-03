package com.msUsuario.msUsuario.controller;

import com.msUsuario.msUsuario.model.Usuario;
import com.msUsuario.msUsuario.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * El usuario se creará con estado 'activo' por defecto.
     * @param usuario El objeto Usuario a crear.
     * @return ResponseEntity con el usuario creado y estado HTTP 201 (CREATED).
     * @throws ResponseStatusException Si el correo ya está en uso o el rol es inválido,
     * devuelve 400 (BAD_REQUEST) o 404 (NOT_FOUND) si el rol no existe.
     */
    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
            return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Captura errores de validación como correo duplicado
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (EntityNotFoundException e) {
            // Captura si el rol asociado no se encuentra
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Actualiza la información de un usuario existente.
     * Permite modificar cualquier campo del usuario, incluyendo su estado (activo/inactivo).
     * @param id El ID del usuario a actualizar.
     * @param usuario El objeto Usuario con los datos a actualizar.
     * @return ResponseEntity con el usuario actualizado y estado HTTP 200 (OK).
     * @throws ResponseStatusException Si el usuario no se encuentra (404 NOT_FOUND),
     * o si el correo ya está en uso por otro usuario activo (400 BAD_REQUEST).
     */
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

    /**
     * Desactiva lógicamente un usuario por su ID.
     * Cambia el campo 'estadoUsuario' a 'false'.
     * @param id El ID del usuario a desactivar.
     * @return ResponseEntity con el usuario desactivado y estado HTTP 200 (OK).
     * @throws ResponseStatusException Si el usuario activo no se encuentra (404 NOT_FOUND).
     */
    @DeleteMapping("/{id}") // Cambiamos el comportamiento de DELETE a desactivación lógica
    public ResponseEntity<Usuario> desactivarUsuario(@PathVariable Integer id) {
        try {
            Usuario usuarioDesactivado = usuarioService.desactivarUsuario(id);
            return ResponseEntity.ok(usuarioDesactivado);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Reactiva un usuario que fue desactivado lógicamente.
     * Cambia el campo 'estadoUsuario' a 'true'.
     * @param id El ID del usuario a reactivar.
     * @return ResponseEntity con el usuario reactivado y estado HTTP 200 (OK).
     * @throws ResponseStatusException Si el usuario inactivo no se encuentra (404 NOT_FOUND).
     */
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<Usuario> reactivarUsuario(@PathVariable Integer id) {
        try {
            Usuario usuarioReactivado = usuarioService.reactivarUsuario(id);
            return ResponseEntity.ok(usuarioReactivado);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Obtiene un usuario por su ID. Por defecto, solo busca usuarios activos.
     * @param id El ID del usuario a buscar.
     * @return ResponseEntity con el usuario encontrado y estado HTTP 200 (OK).
     * @throws ResponseStatusException Si el usuario activo no se encuentra (404 NOT_FOUND).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Integer id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario activo no encontrado con id: " + id));
    }

    /**
     * Obtiene una lista de todos los usuarios activos en el sistema.
     * @return ResponseEntity con la lista de usuarios activos y estado HTTP 200 (OK).
     */
    @GetMapping("/activos")
    public ResponseEntity<List<Usuario>> obtenerTodosUsuariosActivos() {
        return ResponseEntity.ok(usuarioService.obtenerTodosUsuariosActivos());
    }

    /**
     * Obtiene una lista de todos los usuarios en el sistema, incluyendo activos e inactivos.
     * @return ResponseEntity con la lista completa de usuarios y estado HTTP 200 (OK).
     */
    @GetMapping("/todos")
    public ResponseEntity<List<Usuario>> obtenerTodosUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodosUsuarios());
    }

    /**
     * Asigna un rol específico a un usuario. Solo se puede asignar rol a usuarios activos.
     * @param id El ID del usuario al que se le asignará el rol.
     * @param rolId El ID del rol a asignar.
     * @return ResponseEntity con el usuario actualizado y estado HTTP 200 (OK).
     * @throws ResponseStatusException Si el usuario activo o el rol no se encuentran (404 NOT_FOUND).
     */
    @PutMapping("/{id}/asignar-rol/{rolId}")
    public ResponseEntity<Usuario> asignarRol(@PathVariable Integer id, @PathVariable Long rolId) {
        try {
            Usuario usuarioActualizado = usuarioService.asignarRol(id, rolId);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Lista todos los usuarios activos que pertenecen a una tienda específica.
     * @param idTienda El ID de la tienda.
     * @return ResponseEntity con la lista de usuarios activos de la tienda y estado HTTP 200 (OK).
     */
    @GetMapping("/tienda/{idTienda}")
    public ResponseEntity<List<Usuario>> listarUsuariosPorTienda(@PathVariable Integer idTienda) {
        List<Usuario> usuariosEnTienda = usuarioService.listarUsuariosPorTienda(idTienda);
        // Siempre devuelve 200 OK, incluso si la lista está vacía, indicando que la consulta fue exitosa.
        return ResponseEntity.ok(usuariosEnTienda);
    }
}
