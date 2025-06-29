
package com.usuario.usuario.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.usuario.usuario.model.Rol;
import com.usuario.usuario.service.RolService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Optional;
import java.util.stream.Collectors;

// Importaciones de Spring HATEOAS
//import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel; // ¡NUEVO!
import org.springframework.hateoas.CollectionModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    // 1. Crear Rol (POST)
    @PostMapping
    public ResponseEntity<EntityModel<Rol>> crearRol(@RequestBody Rol rol) {
        Rol nuevoRol = rolService.crearRol(rol);
        
        EntityModel<Rol> rolConLinks = EntityModel.of(nuevoRol);

        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerRolPorId(nuevoRol.getId()))
                                .withSelfRel());
        rolConLinks.add(linkTo(methodOn(RolController.class).actualizarRol(nuevoRol.getId(), null))
                                .withRel("update"));
        rolConLinks.add(linkTo(methodOn(RolController.class).desactivarRol(nuevoRol.getId()))
                                .withRel("deactivate"));
        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerTodosRoles()).withRel("all-roles"));

        return new ResponseEntity<>(rolConLinks, HttpStatus.CREATED);
    }

    // 2. Actualizar Rol (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Rol>> actualizarRol(@PathVariable Long id, @RequestBody Rol rol) {
        Rol rolActualizado = rolService.actualizarRol(id, rol);
        EntityModel<Rol> rolConLinks = EntityModel.of(rolActualizado);

        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerRolPorId(rolActualizado.getId()))
                                .withSelfRel());
        rolConLinks.add(linkTo(methodOn(RolController.class).desactivarRol(rolActualizado.getId()))
                                .withRel("deactivate"));
        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerTodosRoles()).withRel("all-roles"));

        return ResponseEntity.ok(rolConLinks);
    }

    // 3. Desactivar Rol (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModel<Rol>> desactivarRol(@PathVariable Long id) {
        Rol rolDesactivado = rolService.desactivarRol(id);
        EntityModel<Rol> rolConLinks = EntityModel.of(rolDesactivado);

        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerRolPorId(rolDesactivado.getId()))
                                .withSelfRel());
        rolConLinks.add(linkTo(methodOn(RolController.class).reactivarRol(rolDesactivado.getId()))
                                .withRel("reactivate"));
        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerTodosRoles()).withRel("all-roles"));

        return ResponseEntity.ok(rolConLinks);
    }

    // 4. Reactivar Rol (PUT)
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<EntityModel<Rol>> reactivarRol(@PathVariable Long id) {
        Rol rolReactivado = rolService.reactivarRol(id);
        EntityModel<Rol> rolConLinks = EntityModel.of(rolReactivado);

        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerRolPorId(rolReactivado.getId()))
                                .withSelfRel());
        rolConLinks.add(linkTo(methodOn(RolController.class).desactivarRol(rolReactivado.getId()))
                                .withRel("deactivate"));
        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerTodosRoles()).withRel("all-roles"));

        return ResponseEntity.ok(rolConLinks);
    }

    // 5. Obtener Rol por ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Rol>> obtenerRolPorId(@PathVariable Long id) {
        return rolService.obtenerRolPorId(id)
            .map(rol -> {
                EntityModel<Rol> rolConLinks = EntityModel.of(rol);

                rolConLinks.add(linkTo(methodOn(RolController.class).obtenerRolPorId(rol.getId()))
                                        .withSelfRel());
                rolConLinks.add(linkTo(methodOn(RolController.class).actualizarRol(rol.getId(), null))
                                        .withRel("update"));

                if (rol.getEstadoRol()) {
                    rolConLinks.add(linkTo(methodOn(RolController.class).desactivarRol(rol.getId()))
                                            .withRel("deactivate"));
                } else {
                    rolConLinks.add(linkTo(methodOn(RolController.class).reactivarRol(rol.getId()))
                                            .withRel("reactivate"));
                }
                rolConLinks.add(linkTo(methodOn(RolController.class).obtenerTodosRoles()).withRel("all-roles"));

                return ResponseEntity.ok(rolConLinks);
            })
            .orElseThrow(() -> new EntityNotFoundException("Rol activo no encontrado con id: " + id));
    }

    // 6. Obtener todos los roles activos (GET)
    @GetMapping("/activos")
    public ResponseEntity<CollectionModel<EntityModel<Rol>>> obtenerTodosRolesActivos() {
        List<Rol> roles = rolService.obtenerTodosRolesActivos();

        List<EntityModel<Rol>> rolesConLinks = roles.stream()
                                                    .map(rol -> EntityModel.of(rol,
                                                        linkTo(methodOn(RolController.class).obtenerRolPorId(rol.getId())).withSelfRel()))
                                                    .collect(Collectors.toList());

        CollectionModel<EntityModel<Rol>> collectionModel = CollectionModel.of(rolesConLinks,
            linkTo(methodOn(RolController.class).obtenerTodosRolesActivos()).withSelfRel(),
            linkTo(methodOn(RolController.class).obtenerTodosRoles()).withRel("all-roles"),
            linkTo(methodOn(RolController.class).crearRol(null)).withRel("create-role"));

        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // 7. Obtener todos los roles (GET)
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Rol>>> obtenerTodosRoles() {
        List<Rol> roles = rolService.obtenerTodosRoles();

        List<EntityModel<Rol>> rolesConLinks = roles.stream()
                                                    .map(rol -> {
                                                        EntityModel<Rol> recurso = EntityModel.of(rol,
                                                            linkTo(methodOn(RolController.class).obtenerRolPorId(rol.getId())).withSelfRel());
                                                        if (!rol.getEstadoRol()) {
                                                            recurso.add(linkTo(methodOn(RolController.class).reactivarRol(rol.getId())).withRel("reactivate"));
                                                        }
                                                        return recurso;
                                                    })
                                                    .collect(Collectors.toList());

        CollectionModel<EntityModel<Rol>> collectionModel = CollectionModel.of(rolesConLinks,
            linkTo(methodOn(RolController.class).obtenerTodosRoles()).withSelfRel(),
            linkTo(methodOn(RolController.class).obtenerTodosRolesActivos()).withRel("active-roles"),
            linkTo(methodOn(RolController.class).crearRol(null)).withRel("create-role"));

        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // --- MANEJADORES DE EXCEPCIONES ESPECÍFICOS PARA ESTE CONTROLADOR ---
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.NOT_FOUND.value());
        errorDetails.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        errorDetails.put("message", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorDetails.put("message", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.CONFLICT.value());
        errorDetails.put("error", HttpStatus.CONFLICT.getReasonPhrase());
        errorDetails.put("message", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        errorDetails.put("message", "Ha ocurrido un error inesperado: " + ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}