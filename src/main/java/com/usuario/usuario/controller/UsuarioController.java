package com.usuario.usuario.controller;

import com.usuario.usuario.model.Usuario;
import com.usuario.usuario.service.UsuarioService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
//import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.stream.Collectors; // Necesario para el stream en listas

// Importaciones de Spring HATEOAS
//import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;       // ¡NUEVO! Para envolver un solo recurso
import org.springframework.hateoas.CollectionModel;  // Para envolver listas de recursos
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // 1. Crear Usuario (POST)
    // Devolvemos EntityModel<Usuario> para incluir enlaces en la respuesta
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> crearUsuario(@RequestBody Usuario usuario) {
        Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
        
        // Envolvemos el usuario en un EntityModel para añadir los enlaces
        EntityModel<Usuario> usuarioConLinks = EntityModel.of(nuevoUsuario);

        // Añadir enlaces HATEOAS al recurso
        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(nuevoUsuario.getIdUsuario()))
                                .withSelfRel());
        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).actualizarUsuario(nuevoUsuario.getIdUsuario(), null))
                                .withRel("update"));
        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).desactivarUsuario(nuevoUsuario.getIdUsuario()))
                                .withRel("deactivate"));
        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerTodosUsuarios()).withRel("all-users"));

        return new ResponseEntity<>(usuarioConLinks, HttpStatus.CREATED);
    }

    // 2. Obtener Usuario por ID (GET)
    // Devolvemos EntityModel<Usuario> para incluir enlaces en la respuesta
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> obtenerUsuarioPorId(@PathVariable Integer id) {
        return usuarioService.obtenerUsuarioPorId(id)
            .map(usuario -> {
                // Envolvemos el usuario en un EntityModel
                EntityModel<Usuario> usuarioConLinks = EntityModel.of(usuario);

                // Añadir enlaces HATEOAS
                usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuario.getIdUsuario()))
                                        .withSelfRel());
                usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).actualizarUsuario(usuario.getIdUsuario(), null))
                                        .withRel("update"));

                if (usuario.getEstadoUsuario()) {
                    usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).desactivarUsuario(usuario.getIdUsuario()))
                                            .withRel("deactivate"));
                } else {
                    usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).reactivarUsuario(usuario.getIdUsuario()))
                                            .withRel("reactivate"));
                }
                usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerTodosUsuarios()).withRel("all-users"));
                usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).asignarRol(usuario.getIdUsuario(), null))
                                        .withRel("assign-role"));
                usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).listarUsuariosPorTienda(usuario.getIdTienda()))
                                        .withRel("users-by-store"));

                return ResponseEntity.ok(usuarioConLinks);
            })
            .orElseThrow(() -> new EntityNotFoundException("Usuario activo no encontrado con id: " + id));
    }

    // 3. Obtener todos los usuarios activos (GET)
    // Devolvemos CollectionModel<EntityModel<Usuario>> para incluir enlaces
    @GetMapping("/activos")
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> obtenerTodosUsuariosActivos() {
        List<Usuario> usuarios = usuarioService.obtenerTodosUsuariosActivos();
        
        // Mapear cada Usuario a un EntityModel y añadir self link a cada uno
        List<EntityModel<Usuario>> usuariosConLinks = usuarios.stream()
                                                            .map(usuario -> EntityModel.of(usuario,
                                                                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuario.getIdUsuario())).withSelfRel()))
                                                            .collect(Collectors.toList());

        // Añadir enlaces a la colección (la lista en sí)
        CollectionModel<EntityModel<Usuario>> collectionModel = CollectionModel.of(usuariosConLinks,
            linkTo(methodOn(UsuarioController.class).obtenerTodosUsuariosActivos()).withSelfRel(),
            linkTo(methodOn(UsuarioController.class).obtenerTodosUsuarios()).withRel("all-users"),
            linkTo(methodOn(UsuarioController.class).crearUsuario(null)).withRel("create-user"));

        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // 4. Obtener todos los usuarios (GET)
    // Devolvemos CollectionModel<EntityModel<Usuario>> para incluir enlaces
    @GetMapping("/todos")
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> obtenerTodosUsuarios() {
        List<Usuario> usuarios = usuarioService.obtenerTodosUsuarios();
        
        // Mapear cada Usuario a un EntityModel y añadir enlaces
        List<EntityModel<Usuario>> usuariosConLinks = usuarios.stream()
                                                            .map(usuario -> {
                                                                EntityModel<Usuario> recurso = EntityModel.of(usuario,
                                                                    linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuario.getIdUsuario())).withSelfRel());
                                                                if (!usuario.getEstadoUsuario()) {
                                                                    recurso.add(linkTo(methodOn(UsuarioController.class).reactivarUsuario(usuario.getIdUsuario())).withRel("reactivate"));
                                                                }
                                                                return recurso;
                                                            })
                                                            .collect(Collectors.toList());

        // Añadir enlaces a la colección
        CollectionModel<EntityModel<Usuario>> collectionModel = CollectionModel.of(usuariosConLinks,
            linkTo(methodOn(UsuarioController.class).obtenerTodosUsuarios()).withSelfRel(),
            linkTo(methodOn(UsuarioController.class).obtenerTodosUsuariosActivos()).withRel("active-users"),
            linkTo(methodOn(UsuarioController.class).crearUsuario(null)).withRel("create-user"));

        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // 5. Actualizar Usuario (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> actualizarUsuario(@PathVariable Integer id, @RequestBody Usuario usuarioDetails) {
        Usuario updatedUsuario = usuarioService.actualizarUsuario(id, usuarioDetails);
        EntityModel<Usuario> usuarioConLinks = EntityModel.of(updatedUsuario);

        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(updatedUsuario.getIdUsuario())).withSelfRel());
        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).desactivarUsuario(updatedUsuario.getIdUsuario())).withRel("deactivate"));
        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerTodosUsuarios()).withRel("all-users"));

        return new ResponseEntity<>(usuarioConLinks, HttpStatus.OK);
    }

    // 6. Desactivar Usuario (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> desactivarUsuario(@PathVariable Integer id) {
        Usuario desactivadoUsuario = usuarioService.desactivarUsuario(id);
        EntityModel<Usuario> usuarioConLinks = EntityModel.of(desactivadoUsuario);

        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(desactivadoUsuario.getIdUsuario())).withSelfRel());
        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).reactivarUsuario(desactivadoUsuario.getIdUsuario())).withRel("reactivate"));
        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerTodosUsuarios()).withRel("all-users"));

        return new ResponseEntity<>(usuarioConLinks, HttpStatus.OK);
    }

    // 7. Reactivar Usuario (PUT)
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<EntityModel<Usuario>> reactivarUsuario(@PathVariable Integer id) {
        Usuario reactivatedUsuario = usuarioService.reactivarUsuario(id);
        EntityModel<Usuario> usuarioConLinks = EntityModel.of(reactivatedUsuario);

        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(reactivatedUsuario.getIdUsuario())).withSelfRel());
        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).desactivarUsuario(reactivatedUsuario.getIdUsuario())).withRel("deactivate"));
        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerTodosUsuarios()).withRel("all-users"));

        return new ResponseEntity<>(usuarioConLinks, HttpStatus.OK);
    }

    // 8. Asignar Rol (PUT)
    @PutMapping("/{id}/asignar-rol/{rolId}")
    public ResponseEntity<EntityModel<Usuario>> asignarRol(@PathVariable Integer id, @PathVariable Long rolId) {
        Usuario usuarioConRol = usuarioService.asignarRol(id, rolId);
        EntityModel<Usuario> usuarioConLinks = EntityModel.of(usuarioConRol);

        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuarioConRol.getIdUsuario())).withSelfRel());
        usuarioConLinks.add(linkTo(methodOn(UsuarioController.class).obtenerTodosUsuarios()).withRel("all-users"));

        return new ResponseEntity<>(usuarioConLinks, HttpStatus.OK);
    }

    // 9. Listar Usuarios por Tienda (GET)
    @GetMapping("/tienda/{idTienda}")
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> listarUsuariosPorTienda(@PathVariable Integer idTienda) {
        List<Usuario> usuarios = usuarioService.listarUsuariosPorTienda(idTienda);
        
        List<EntityModel<Usuario>> usuariosConLinks = usuarios.stream()
                                                            .map(usuario -> EntityModel.of(usuario,
                                                                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuario.getIdUsuario())).withSelfRel()))
                                                            .collect(Collectors.toList());

        CollectionModel<EntityModel<Usuario>> collectionModel = CollectionModel.of(usuariosConLinks,
            linkTo(methodOn(UsuarioController.class).listarUsuariosPorTienda(idTienda)).withSelfRel(),
            linkTo(methodOn(UsuarioController.class).obtenerTodosUsuarios()).withRel("all-users"),
            linkTo(methodOn(UsuarioController.class).obtenerTodosUsuariosActivos()).withRel("active-users"));

        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // --- MANEJADORES DE EXCEPCIONES ESPECÍFICOS PARA ESTE CONTROLADOR ---
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.NOT_FOUND.value());
        errorDetails.put("error", "Not Found");
        errorDetails.put("message", ex.getMessage());
        return errorDetails;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", "Bad Request");
        errorDetails.put("message", ex.getMessage());
        return errorDetails;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGenericException(Exception ex) {
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put("error", "Internal Server Error");
        errorDetails.put("message", "Ha ocurrido un error inesperado: " + ex.getMessage());
        return errorDetails;
    }
}