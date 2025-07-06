package com.usuario.usuario.controller;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usuario.usuario.model.Permiso;
import com.usuario.usuario.model.Rol;
import com.usuario.usuario.service.RolService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(RolController.class)
class RolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RolService rolService;

    @Autowired
    private ObjectMapper objectMapper;

    // Variables para almacenar los datos de prueba
    private Rol rolAdmin;
    private Rol rolUsuario;
    private Rol rolInactivo;

    @BeforeEach
    void setUp() {
        rolAdmin = new Rol(1L, "ADMIN", Arrays.asList(Permiso.CREAR_USUARIO, Permiso.VER_USUARIO), true);
        rolUsuario = new Rol(2L, "USUARIO", Arrays.asList(Permiso.VER_USUARIO), true);
        rolInactivo = new Rol(3L, "AUDITOR", Collections.emptyList(), false);
    }

    // --- PRUEBAS PARA crearRol (POST /roles) ---
    @Test
    void testCrearRol_Exito() throws Exception {
        Mockito.when(rolService.crearRol(any(Rol.class))).thenReturn(rolAdmin);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolAdmin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(rolAdmin.getId()))
                .andExpect(jsonPath("$.nombreRol").value(rolAdmin.getNombreRol()))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.update.href").exists())
                .andExpect(jsonPath("$._links.deactivate.href").exists())
                .andExpect(jsonPath("$._links.all-roles.href").exists());

        Mockito.verify(rolService, Mockito.times(1)).crearRol(any(Rol.class));
    }

    @Test
    void testCrearRol_BadRequest_NombreDuplicado() throws Exception {
        Mockito.when(rolService.crearRol(any(Rol.class)))
               .thenThrow(new IllegalArgumentException("Ya existe un rol activo con el nombre: ADMIN"));

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolAdmin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ya existe un rol activo con el nombre: ADMIN"));

        Mockito.verify(rolService, Mockito.times(1)).crearRol(any(Rol.class));
    }

    // --- PRUEBAS PARA actualizarRol (PUT /roles/{id}) ---
    @Test
    void testActualizarRol_Exito() throws Exception {
        Rol rolActualizado = new Rol(1L, "ADMIN_NUEVO", Arrays.asList(Permiso.ACTUALIZAR_USUARIO), true);
        Mockito.when(rolService.actualizarRol(eq(1L), any(Rol.class))).thenReturn(rolActualizado);

        mockMvc.perform(put("/roles/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreRol").value(rolActualizado.getNombreRol()))
                .andExpect(jsonPath("$._links.self.href").exists());

        Mockito.verify(rolService, Mockito.times(1)).actualizarRol(eq(1L), any(Rol.class));
    }

    @Test
    void testActualizarRol_NotFound() throws Exception {
        Mockito.when(rolService.actualizarRol(eq(99L), any(Rol.class)))
               .thenThrow(new EntityNotFoundException("Rol no encontrado con id: 99"));

        mockMvc.perform(put("/roles/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Rol())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Rol no encontrado con id: 99"));

        Mockito.verify(rolService, Mockito.times(1)).actualizarRol(eq(99L), any(Rol.class));
    }

    // --- PRUEBAS PARA desactivarRol (DELETE /roles/{id}) ---
    @Test
    void testDesactivarRol_Exito() throws Exception {
        Rol rolDesactivado = new Rol(1L, "ADMIN", Collections.emptyList(), false);
        Mockito.when(rolService.desactivarRol(eq(1L))).thenReturn(rolDesactivado);

        mockMvc.perform(delete("/roles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoRol").value(false))
                .andExpect(jsonPath("$._links.reactivate.href").exists());

        Mockito.verify(rolService, Mockito.times(1)).desactivarRol(eq(1L));
    }

    @Test
    void testDesactivarRol_Conflict_UsuariosVinculados() throws Exception {
        Mockito.when(rolService.desactivarRol(eq(1L)))
               .thenThrow(new IllegalStateException("No se puede desactivar el rol."));

        mockMvc.perform(delete("/roles/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No se puede desactivar el rol."));

        Mockito.verify(rolService, Mockito.times(1)).desactivarRol(eq(1L));
    }

    // --- PRUEBAS PARA reactivarRol (PUT /roles/{id}/reactivar) ---
    @Test
    void testReactivarRol_Exito() throws Exception {
        Rol rolReactivado = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Mockito.when(rolService.reactivarRol(eq(1L))).thenReturn(rolReactivado);

        mockMvc.perform(put("/roles/{id}/reactivar", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoRol").value(true))
                .andExpect(jsonPath("$._links.deactivate.href").exists());

        Mockito.verify(rolService, Mockito.times(1)).reactivarRol(eq(1L));
    }

    @Test
    void testReactivarRol_NotFound() throws Exception {
        Mockito.when(rolService.reactivarRol(eq(99L)))
               .thenThrow(new EntityNotFoundException("Rol no encontrado para reactivar con id: 99"));

        mockMvc.perform(put("/roles/{id}/reactivar", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Rol no encontrado para reactivar con id: 99"));

        Mockito.verify(rolService, Mockito.times(1)).reactivarRol(eq(99L));
    }

    // --- PRUEBAS PARA obtenerRolPorId (GET /roles/{id}) ---
    
    /**
     * MODIFICADO: Se añade una aserción explícita para el enlace 'deactivate'.
     * Prueba la obtención de un rol ACTIVO y verifica que se ofrece la opción de desactivar.
     * Esto cubre la rama 'if (rol.getEstadoRol())' del controlador.
     */
    @Test
    void testObtenerRolPorId_Exito_ConRolActivo() throws Exception {
        Mockito.when(rolService.obtenerRolPorId(eq(1L))).thenReturn(Optional.of(rolAdmin));

        mockMvc.perform(get("/roles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreRol").value(rolAdmin.getNombreRol()))
                .andExpect(jsonPath("$._links.deactivate.href").exists())
                .andExpect(jsonPath("$._links.reactivate").doesNotExist());

        Mockito.verify(rolService, Mockito.times(1)).obtenerRolPorId(eq(1L));
    }

    /**
     * NUEVO TEST: Prueba la obtención de un rol INACTIVO por su ID.
     * Esto cubre la rama 'else' del controlador que faltaba por validar.
     */
    @Test
    void testObtenerRolPorId_Exito_ConRolInactivo() throws Exception {
        Mockito.when(rolService.obtenerRolPorId(eq(3L))).thenReturn(Optional.of(rolInactivo));

        mockMvc.perform(get("/roles/{id}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreRol").value(rolInactivo.getNombreRol()))
                .andExpect(jsonPath("$._links.reactivate.href").exists())
                .andExpect(jsonPath("$._links.deactivate").doesNotExist());

        Mockito.verify(rolService, Mockito.times(1)).obtenerRolPorId(eq(3L));
    }
    
    @Test
    void testObtenerRolPorId_NotFound() throws Exception {
        Mockito.when(rolService.obtenerRolPorId(eq(99L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/roles/{id}", 99L))
                .andExpect(status().isNotFound());

        Mockito.verify(rolService, Mockito.times(1)).obtenerRolPorId(eq(99L));
    }

    // --- PRUEBAS PARA obtenerTodosRolesActivos (GET /roles/activos) ---
    @Test
    void testObtenerTodosRolesActivos_Exito() throws Exception {
        Mockito.when(rolService.obtenerTodosRolesActivos()).thenReturn(Arrays.asList(rolAdmin, rolUsuario));

        mockMvc.perform(get("/roles/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.rolList.length()").value(2));

        Mockito.verify(rolService, Mockito.times(1)).obtenerTodosRolesActivos();
    }
    
    @Test
    void testObtenerTodosRolesActivos_ListaVacia() throws Exception {
        Mockito.when(rolService.obtenerTodosRolesActivos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/roles/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());

        Mockito.verify(rolService, Mockito.times(1)).obtenerTodosRolesActivos();
    }

    // --- PRUEBAS PARA obtenerTodosRoles (GET /roles) ---

    @Test
    void testObtenerTodosRoles_Exito() throws Exception {
        Mockito.when(rolService.obtenerTodosRoles()).thenReturn(Arrays.asList(rolAdmin, rolInactivo));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.rolList.length()").value(2))
                .andExpect(jsonPath("$._embedded.rolList[0].nombreRol").value("ADMIN"))
                .andExpect(jsonPath("$._embedded.rolList[0]._links.reactivate").doesNotExist())
                .andExpect(jsonPath("$._embedded.rolList[1].nombreRol").value("AUDITOR"))
                .andExpect(jsonPath("$._embedded.rolList[1]._links.reactivate.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.active-roles.href").exists())
                .andExpect(jsonPath("$._links.create-role.href").exists());

        Mockito.verify(rolService, Mockito.times(1)).obtenerTodosRoles();
    }

    @Test
    void testObtenerTodosRoles_ListaVacia() throws Exception {
        Mockito.when(rolService.obtenerTodosRoles()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());

        Mockito.verify(rolService, Mockito.times(1)).obtenerTodosRoles();
    }

    // --- PRUEBAS PARA MANEJADORES DE EXCEPCIONES ---
    
    @Test
    void testHandleGenericException_InternalServerError() throws Exception {
        String errorMessage = "Error inesperado en la capa de servicio";
        Mockito.when(rolService.obtenerTodosRoles()).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message", containsString(errorMessage)));

        Mockito.verify(rolService, Mockito.times(1)).obtenerTodosRoles();
    }
}