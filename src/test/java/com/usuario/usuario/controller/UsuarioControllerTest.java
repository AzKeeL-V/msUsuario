package com.usuario.usuario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usuario.usuario.model.Rol;
import com.usuario.usuario.model.Usuario;
import com.usuario.usuario.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@DisplayName("Tests para UsuarioController con Cobertura Completa")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Datos de prueba ---
    private Usuario usuarioActivo;
    private Usuario usuarioInactivo;
    private Rol rolAdmin;
    private Usuario usuarioSinRol; // Nuevo usuario sin rol

    @BeforeEach
    void setUp() {
        rolAdmin = new Rol();
        rolAdmin.setId(1L);
        rolAdmin.setNombreRol("ADMIN");
        rolAdmin.setEstadoRol(true);

        usuarioActivo = new Usuario();
        usuarioActivo.setIdUsuario(1);
        usuarioActivo.setNomUsuario("Test");
        usuarioActivo.setCorreoUsuario("test@example.com");
        usuarioActivo.setEstadoUsuario(true);
        usuarioActivo.setRol(rolAdmin);
        usuarioActivo.setIdTienda(101);

        usuarioInactivo = new Usuario();
        usuarioInactivo.setIdUsuario(2);
        usuarioInactivo.setNomUsuario("Inactive");
        usuarioInactivo.setCorreoUsuario("inactive@example.com");
        usuarioInactivo.setEstadoUsuario(false);
        usuarioInactivo.setRol(rolAdmin);
        usuarioInactivo.setIdTienda(102);

        usuarioSinRol = new Usuario();
        usuarioSinRol.setIdUsuario(3);
        usuarioSinRol.setNomUsuario("NoRole");
        usuarioSinRol.setCorreoUsuario("norole@example.com");
        usuarioSinRol.setEstadoUsuario(true);
        usuarioSinRol.setRol(null); // Explicitamente sin rol
        usuarioSinRol.setIdTienda(103);
    }

    // --- 1. Crear Usuario (POST /usuarios) ---

    @Test
    @DisplayName("POST /usuarios - Éxito al crear")
    void testCrearUsuario_Success() throws Exception {
        Mockito.when(usuarioService.crearUsuario(any(Usuario.class))).thenReturn(usuarioActivo);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioActivo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(usuarioActivo.getIdUsuario()))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("POST /usuarios - Falla por argumento ilegal")
    void testCrearUsuario_EmailDuplicate_BadRequest() throws Exception {
        String errorMessage = "El correo test@example.com ya está en uso.";
        Mockito.when(usuarioService.crearUsuario(any(Usuario.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioActivo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    // --- 2. Obtener Usuario por ID (GET /usuarios/{id}) ---

    @Test
    @DisplayName("GET /usuarios/{id} - Éxito con usuario activo")
    void testObtenerUsuarioPorId_Activo_Success() throws Exception {
        Mockito.when(usuarioService.obtenerUsuarioPorId(eq(usuarioActivo.getIdUsuario())))
                .thenReturn(Optional.of(usuarioActivo));

        mockMvc.perform(get("/usuarios/{id}", usuarioActivo.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(usuarioActivo.getIdUsuario()))
                .andExpect(jsonPath("$._links.deactivate.href").exists()) // Debe tener enlace para desactivar
                .andExpect(jsonPath("$._links.reactivate").doesNotExist()); // No debe tener para reactivar
    }

    @Test
    @DisplayName("GET /usuarios/{id} - Éxito con usuario inactivo")
    void testObtenerUsuarioPorId_Inactivo_Success() throws Exception {
        Mockito.when(usuarioService.obtenerUsuarioPorId(eq(usuarioInactivo.getIdUsuario())))
                .thenReturn(Optional.of(usuarioInactivo));

        mockMvc.perform(get("/usuarios/{id}", usuarioInactivo.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(usuarioInactivo.getIdUsuario()))
                .andExpect(jsonPath("$._links.reactivate.href").exists()) // Debe tener enlace para reactivar
                .andExpect(jsonPath("$._links.deactivate").doesNotExist()); // No debe tener para desactivar
    }

    @Test
    @DisplayName("GET /usuarios/{id} - Falla por no encontrar")
    void testObtenerUsuarioPorId_NotFound() throws Exception {
        Mockito.when(usuarioService.obtenerUsuarioPorId(eq(99))).thenReturn(Optional.empty());

        mockMvc.perform(get("/usuarios/{id}", 99))
                .andExpect(status().isNotFound());
    }

    // --- 3. Obtener todos los usuarios activos (GET /usuarios/activos) ---

    @Test
    @DisplayName("GET /usuarios/activos - Éxito con lista")
    void testObtenerTodosUsuariosActivos_Success() throws Exception {
        Mockito.when(usuarioService.obtenerTodosUsuariosActivos()).thenReturn(Collections.singletonList(usuarioActivo));

        mockMvc.perform(get("/usuarios/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.usuarioList.length()").value(1))
                .andExpect(jsonPath("$._embedded.usuarioList[0].idUsuario").value(usuarioActivo.getIdUsuario()));
    }
    
    @Test
    @DisplayName("GET /usuarios/activos - Éxito con lista vacía")
    void testObtenerTodosUsuariosActivos_EmptyList() throws Exception {
        Mockito.when(usuarioService.obtenerTodosUsuariosActivos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/usuarios/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    // --- 4. Obtener todos los usuarios (GET /usuarios/todos) ---

    @Test
    @DisplayName("GET /usuarios/todos - Éxito con lista mixta")
    void testObtenerTodosUsuarios_Success() throws Exception {
        Mockito.when(usuarioService.obtenerTodosUsuarios()).thenReturn(Arrays.asList(usuarioActivo, usuarioInactivo));

        mockMvc.perform(get("/usuarios/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.usuarioList.length()").value(2))
                .andExpect(jsonPath("$._embedded.usuarioList[0].estadoUsuario").value(true))
                .andExpect(jsonPath("$._embedded.usuarioList[0]._links.reactivate").doesNotExist())
                .andExpect(jsonPath("$._embedded.usuarioList[1].estadoUsuario").value(false))
                .andExpect(jsonPath("$._embedded.usuarioList[1]._links.reactivate.href").exists());
    }

    @Test
    @DisplayName("GET /usuarios/todos - Éxito con lista vacía")
    void testObtenerTodosUsuarios_EmptyList() throws Exception {
        Mockito.when(usuarioService.obtenerTodosUsuarios()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/usuarios/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }
    
    // --- 5. Actualizar Usuario (PUT /usuarios/{id}) ---
    
    @Test
    @DisplayName("PUT /usuarios/{id} - Éxito al actualizar")
    void testActualizarUsuario_Success() throws Exception {
        Mockito.when(usuarioService.actualizarUsuario(eq(usuarioActivo.getIdUsuario()), any(Usuario.class)))
                .thenReturn(usuarioActivo);

        mockMvc.perform(put("/usuarios/{id}", usuarioActivo.getIdUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioActivo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(usuarioActivo.getIdUsuario()));
    }
    
    @Test
    @DisplayName("PUT /usuarios/{id} - Falla por no encontrar")
    void testActualizarUsuario_NotFound() throws Exception {
        Mockito.when(usuarioService.actualizarUsuario(eq(99), any(Usuario.class)))
                .thenThrow(new EntityNotFoundException("Usuario no encontrado"));
        
        mockMvc.perform(put("/usuarios/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioActivo)))
                .andExpect(status().isNotFound());
    }

    // --- 6. Desactivar Usuario (DELETE /usuarios/{id}) ---

    @Test
    @DisplayName("DELETE /usuarios/{id} - Éxito al desactivar")
    void testDesactivarUsuario_Success() throws Exception {
        Mockito.when(usuarioService.desactivarUsuario(eq(usuarioActivo.getIdUsuario())))
                .thenReturn(usuarioInactivo);

        mockMvc.perform(delete("/usuarios/{id}", usuarioActivo.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoUsuario").value(false));
    }
    
    @Test
    @DisplayName("DELETE /usuarios/{id} - Falla por no encontrar")
    void testDesactivarUsuario_NotFound() throws Exception {
        Mockito.when(usuarioService.desactivarUsuario(eq(99)))
                .thenThrow(new EntityNotFoundException("Usuario no encontrado"));
        
        mockMvc.perform(delete("/usuarios/{id}", 99))
                .andExpect(status().isNotFound());
    }

    // --- 7. Reactivar Usuario (PUT /usuarios/{id}/reactivar) ---

    @Test
    @DisplayName("PUT /usuarios/{id}/reactivar - Éxito al reactivar")
    void testReactivarUsuario_Success() throws Exception {
        Mockito.when(usuarioService.reactivarUsuario(eq(usuarioInactivo.getIdUsuario())))
                .thenReturn(usuarioActivo);
        
        mockMvc.perform(put("/usuarios/{id}/reactivar", usuarioInactivo.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoUsuario").value(true));
    }
    
    @Test
    @DisplayName("PUT /usuarios/{id}/reactivar - Falla por no encontrar")
    void testReactivarUsuario_NotFound() throws Exception {
        Mockito.when(usuarioService.reactivarUsuario(eq(99)))
                .thenThrow(new EntityNotFoundException("Usuario no encontrado"));

        mockMvc.perform(put("/usuarios/{id}/reactivar", 99))
                .andExpect(status().isNotFound());
    }

    // --- 8. Asignar Rol (PUT /{id}/asignar-rol/{rolId}) ---

    @Test
    @DisplayName("PUT /{id}/asignar-rol/{rolId} - Éxito al asignar rol")
    void testAsignarRol_Success() throws Exception {
        Mockito.when(usuarioService.asignarRol(eq(usuarioActivo.getIdUsuario()), eq(rolAdmin.getId())))
                .thenReturn(usuarioActivo);

        mockMvc.perform(put("/usuarios/{id}/asignar-rol/{rolId}", usuarioActivo.getIdUsuario(), rolAdmin.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol.id").value(rolAdmin.getId()));
    }

    @Test
    @DisplayName("PUT /{id}/asignar-rol/{rolId} - Falla si no encuentra usuario")
    void testAsignarRol_UsuarioNotFound() throws Exception {
        Mockito.when(usuarioService.asignarRol(eq(99), eq(rolAdmin.getId())))
                .thenThrow(new EntityNotFoundException("Usuario no encontrado"));

        mockMvc.perform(put("/usuarios/{id}/asignar-rol/{rolId}", 99, rolAdmin.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /{id}/asignar-rol/{rolId} - Falla si no encuentra rol")
    void testAsignarRol_RolNotFound() throws Exception {
        Mockito.when(usuarioService.asignarRol(eq(usuarioActivo.getIdUsuario()), eq(99L)))
                .thenThrow(new EntityNotFoundException("Rol no encontrado"));

        mockMvc.perform(put("/usuarios/{id}/asignar-rol/{rolId}", usuarioActivo.getIdUsuario(), 99L))
                .andExpect(status().isNotFound());
    }

    // --- NUEVO: Test para Quitar Rol (DELETE /{id}/rol) ---
    @Test
    @DisplayName("DELETE /usuarios/{id}/rol - Éxito al quitar rol")
    void testQuitarRolDeUsuario_Success() throws Exception {
        Mockito.when(usuarioService.quitarRol(eq(usuarioActivo.getIdUsuario())))
                .thenReturn(usuarioSinRol); // Devuelve el usuario sin rol

        mockMvc.perform(delete("/usuarios/{id}/rol", usuarioActivo.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").doesNotExist()) // El campo rol no debe existir
                .andExpect(jsonPath("$._links.assign-role.href").exists()); // Debe tener enlace para asignar rol
    }

    @Test
    @DisplayName("DELETE /usuarios/{id}/rol - Falla por no encontrar usuario al quitar rol")
    void testQuitarRolDeUsuario_NotFound() throws Exception {
        Mockito.when(usuarioService.quitarRol(eq(99)))
                .thenThrow(new EntityNotFoundException("Usuario no encontrado para quitar rol"));

        mockMvc.perform(delete("/usuarios/{id}/rol", 99))
                .andExpect(status().isNotFound());
    }

    // --- 9. Listar Usuarios por Tienda (GET /usuarios/tienda/{idTienda}) ---

    @Test
    @DisplayName("GET /usuarios/tienda/{id} - Éxito con lista")
    void testListarUsuariosPorTienda_Success() throws Exception {
        Mockito.when(usuarioService.listarUsuariosPorTienda(eq(101)))
                .thenReturn(Collections.singletonList(usuarioActivo));

        mockMvc.perform(get("/usuarios/tienda/{idTienda}", 101))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.usuarioList.length()").value(1))
                .andExpect(jsonPath("$._embedded.usuarioList[0].idTienda").value(101));
    }
    
    @Test
    @DisplayName("GET /usuarios/tienda/{id} - Éxito con lista vacía")
    void testListarUsuariosPorTienda_EmptyList() throws Exception {
        Mockito.when(usuarioService.listarUsuariosPorTienda(eq(999)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/usuarios/tienda/{idTienda}", 999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    // --- 10. Pruebas para Manejadores de Excepciones ---
    
    // Test para la rama "exception.class" con una excepción que no sea IllegalArgument o EntityNotFound
    @Test
    @DisplayName("Handler - Debería manejar errores genéricos con 500 INTERNAL_SERVER_ERROR")
    void testHandleGenericException_InternalServerError() throws Exception {
        String errorMessage = "Error inesperado de base de datos";
        // Simula que cualquier método lanza una RuntimeException (no EntityNotFoundException ni IllegalArgumentException)
        Mockito.when(usuarioService.obtenerUsuarioPorId(any(Integer.class)))
                .thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/usuarios/{id}", 1)) // Llama a un endpoint que pueda lanzar la excepción
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message", containsString(errorMessage)));
    }
}