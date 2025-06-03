package com.msUsuario.msUsuario.controller;

import com.msUsuario.msUsuario.model.Rol;
import com.msUsuario.msUsuario.service.RolService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @PostMapping
    public ResponseEntity<Rol> crearRol(@RequestBody Rol rol) {
        try {
            Rol nuevoRol = rolService.crearRol(rol);
            return new ResponseEntity<>(nuevoRol, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rol> actualizarRol(@PathVariable Long id, @RequestBody Rol rol) {
        try {
            Rol rolActualizado = rolService.actualizarRol(id, rol);
            return ResponseEntity.ok(rolActualizado);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // Método para DESACTIVAR (Eliminar Lógicamente) un Rol
    @DeleteMapping("/{id}") // Usamos DELETE para desactivar, siguiendo RESTful
    public ResponseEntity<Rol> desactivarRol(@PathVariable Long id) {
        try {
            Rol rolDesactivado = rolService.desactivarRol(id);
            // Puedes devolver el rol desactivado o un 200 OK sin contenido
            return ResponseEntity.ok(rolDesactivado);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) { // Captura la excepción si hay usuarios activos vinculados
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    // Nuevo método para REACTIVAR un Rol
    @PutMapping("/{id}/reactivar") // Usamos PUT para reactivar, en un endpoint específico
    public ResponseEntity<Rol> reactivarRol(@PathVariable Long id) {
        try {
            Rol rolReactivado = rolService.reactivarRol(id);
            return ResponseEntity.ok(rolReactivado);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    // Modificado: Ahora busca roles activos por defecto
    @GetMapping("/{id}")
    public ResponseEntity<Rol> obtenerRolPorId(@PathVariable Long id) {
        return rolService.obtenerRolPorId(id) // Este método en el servicio ya busca solo activos
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol activo no encontrado con id: " + id));
    }

    // Nuevo método para obtener solo roles activos
    @GetMapping("/activos")
    public ResponseEntity<List<Rol>> obtenerTodosRolesActivos() {
        return ResponseEntity.ok(rolService.obtenerTodosRolesActivos());
    }

    // Método para obtener TODOS los roles (activos e inactivos)
    @GetMapping
    public ResponseEntity<List<Rol>> obtenerTodosRoles() {
        return ResponseEntity.ok(rolService.obtenerTodosRoles());
    }
}