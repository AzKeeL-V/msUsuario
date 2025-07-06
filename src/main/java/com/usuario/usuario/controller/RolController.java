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

// --- INICIO DE IMPORTACIONES HATEOAS ---
// Estas clases son el núcleo de Spring HATEOAS para construir respuestas enriquecidas con enlaces.

// EntityModel: Es un contenedor genérico que envuelve un objeto de dominio (como 'Rol') y le añade una colección de enlaces (Links).
// Se utiliza para representar un único recurso.
import org.springframework.hateoas.EntityModel;

// CollectionModel: Similar a EntityModel, pero diseñado para envolver una colección de recursos (por ejemplo, una lista de 'Rol').
// También puede contener enlaces que se aplican a la colección en su conjunto (como un enlace para crear un nuevo rol).
import org.springframework.hateoas.CollectionModel;

// WebMvcLinkBuilder: Es la clase principal para construir enlaces (Links).
// Permite crear enlaces apuntando a los métodos de los controladores de Spring MVC de una manera segura y refactorizable.
// El 'import static' permite usar sus métodos 'linkTo' y 'methodOn' directamente sin prefijo de clase.
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
// --- FIN DE IMPORTACIONES HATEOAS ---


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
        
        // --- INICIO HATEOAS ---
        // 1. Envolver el objeto 'nuevoRol' en un 'EntityModel'.
        //    'EntityModel.of(nuevoRol)' crea un contenedor para el rol que ahora puede tener enlaces.
        EntityModel<Rol> rolConLinks = EntityModel.of(nuevoRol);

        // 2. Añadir enlaces al 'EntityModel' para describir las posibles acciones siguientes.
        
        // 'linkTo(methodOn(RolController.class).obtenerRolPorId(nuevoRol.getId()))' construye una URL que apunta
        // al método 'obtenerRolPorId' de este mismo controlador, pasándole el ID del rol recién creado.
        // '.withSelfRel()' designa este enlace como el enlace "self", que es la URL canónica para este recurso.
        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerRolPorId(nuevoRol.getId()))
                                .withSelfRel());

        // Se añade un enlace con la relación (rel) "update". El cliente sabe que este enlace es para actualizar el rol.
        // Apunta al método 'actualizarRol'. Pasamos 'null' en el body porque no es necesario para construir la URL.
        rolConLinks.add(linkTo(methodOn(RolController.class).actualizarRol(nuevoRol.getId(), null))
                                .withRel("update"));

        // Enlace para la acción de desactivar el rol.
        rolConLinks.add(linkTo(methodOn(RolController.class).desactivarRol(nuevoRol.getId()))
                                .withRel("deactivate"));
        
        // Enlace a la colección de todos los roles.
        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerTodosRoles()).withRel("all-roles"));
        // --- FIN HATEOAS ---

        // La respuesta ahora no solo contiene los datos del rol, sino también un objeto '_links' con las URLs de las acciones.
        return new ResponseEntity<>(rolConLinks, HttpStatus.CREATED);
    }

    // 2. Actualizar Rol (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Rol>> actualizarRol(@PathVariable Long id, @RequestBody Rol rol) {
        Rol rolActualizado = rolService.actualizarRol(id, rol);
        
        // --- HATEOAS: Se sigue el mismo patrón que en 'crearRol' ---
        EntityModel<Rol> rolConLinks = EntityModel.of(rolActualizado);

        // Enlace 'self' al propio recurso actualizado.
        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerRolPorId(rolActualizado.getId()))
                                .withSelfRel());
        
        // Enlace para la posible acción de desactivar.
        rolConLinks.add(linkTo(methodOn(RolController.class).desactivarRol(rolActualizado.getId()))
                                .withRel("deactivate"));
        
        // Enlace a la colección completa.
        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerTodosRoles()).withRel("all-roles"));

        return ResponseEntity.ok(rolConLinks);
    }

    // 3. Desactivar Rol (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModel<Rol>> desactivarRol(@PathVariable Long id) {
        Rol rolDesactivado = rolService.desactivarRol(id);
        EntityModel<Rol> rolConLinks = EntityModel.of(rolDesactivado);

        // --- HATEOAS: Los enlaces reflejan el nuevo estado del recurso ---
        // Enlace 'self' para ver el rol (ahora en estado inactivo).
        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerRolPorId(rolDesactivado.getId()))
                                .withSelfRel());
        
        // Como el rol está desactivado, la siguiente acción lógica es "reactivar".
        // Proporcionamos un enlace para ello.
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

        // --- HATEOAS: Similar a desactivar, los enlaces reflejan el nuevo estado activo ---
        rolConLinks.add(linkTo(methodOn(RolController.class).obtenerRolPorId(rolReactivado.getId()))
                                .withSelfRel());
        
        // Ahora que está activo, la acción posible es "desactivar".
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
                    // --- HATEOAS: Enlaces condicionales basados en el estado del rol ---
                    EntityModel<Rol> rolConLinks = EntityModel.of(rol);

                    rolConLinks.add(linkTo(methodOn(RolController.class).obtenerRolPorId(rol.getId()))
                                            .withSelfRel());
                    rolConLinks.add(linkTo(methodOn(RolController.class).actualizarRol(rol.getId(), null))
                                            .withRel("update"));

                    // Se añade un enlace para desactivar o reactivar dependiendo del estado actual del rol.
                    // Esto hace que la API sea "inteligente", solo ofreciendo acciones que son válidas en el estado actual.
                    if (rol.getEstadoRol()) { // Si el rol está activo
                        rolConLinks.add(linkTo(methodOn(RolController.class).desactivarRol(rol.getId()))
                                                .withRel("deactivate"));
                    } else { // Si el rol está inactivo
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

        // --- HATEOAS para colecciones ---
        // 1. Se convierte cada objeto 'Rol' de la lista en un 'EntityModel<Rol>'.
        //    A cada rol individual se le añade su propio enlace 'self'.
        List<EntityModel<Rol>> rolesConLinks = roles.stream()
                .map(rol -> EntityModel.of(rol,
                        linkTo(methodOn(RolController.class).obtenerRolPorId(rol.getId())).withSelfRel()))
                .collect(Collectors.toList());

        // 2. Se envuelve la lista de 'EntityModel' en un 'CollectionModel'.
        //    Esto permite añadir enlaces que se aplican a la colección en sí, no a los elementos individuales.
        CollectionModel<EntityModel<Rol>> collectionModel = CollectionModel.of(rolesConLinks,
            // Enlace 'self' para esta misma consulta (la lista de roles activos).
            linkTo(methodOn(RolController.class).obtenerTodosRolesActivos()).withSelfRel(),
            // Enlace para ver la lista de *todos* los roles (activos e inactivos).
            linkTo(methodOn(RolController.class).obtenerTodosRoles()).withRel("all-roles"),
            // Enlace para crear un nuevo rol.
            linkTo(methodOn(RolController.class).crearRol(null)).withRel("create-role"));

        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // 7. Obtener todos los roles (GET)
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Rol>>> obtenerTodosRoles() {
        List<Rol> roles = rolService.obtenerTodosRoles();

        // --- HATEOAS: Colección con enlaces condicionales por elemento ---
        List<EntityModel<Rol>> rolesConLinks = roles.stream()
                .map(rol -> {
                    // Cada rol se convierte en un recurso con su enlace 'self'.
                    EntityModel<Rol> recurso = EntityModel.of(rol,
                            linkTo(methodOn(RolController.class).obtenerRolPorId(rol.getId())).withSelfRel());
                    
                    // Si un rol específico está inactivo, se le añade un enlace individual para reactivarlo.
                    if (!rol.getEstadoRol()) {
                        recurso.add(linkTo(methodOn(RolController.class).reactivarRol(rol.getId())).withRel("reactivate"));
                    }
                    return recurso;
                })
                .collect(Collectors.toList());

        // Se crea el 'CollectionModel' con sus enlaces a nivel de colección.
        CollectionModel<EntityModel<Rol>> collectionModel = CollectionModel.of(rolesConLinks,
            linkTo(methodOn(RolController.class).obtenerTodosRoles()).withSelfRel(),
            linkTo(methodOn(RolController.class).obtenerTodosRolesActivos()).withRel("active-roles"),
            linkTo(methodOn(RolController.class).crearRol(null)).withRel("create-role"));

        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }
    
    // --- MANEJADORES DE EXCEPCIONES ESPECÍFICOS PARA ESTE CONTROLADOR ---
    // (Estos no están relacionados con HATEOAS, pero son una buena práctica para manejar errores de forma centralizada en el controlador)
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