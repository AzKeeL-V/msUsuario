package com.usuario.usuario.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usuario.usuario.model.Rol;
import com.usuario.usuario.model.Usuario;
import com.usuario.usuario.service.UsuarioService;

import jakarta.persistence.EntityNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Clase de pruebas de integración (WebMvcTest) para UsuarioController.
 * Utiliza MockMvc para probar la capa web (el controlador) sin levantar
 * un servidor HTTP completo, pero probando el ruteo, serialización/deserialización
 * y manejo de errores HTTP. El servicio subyacente (UsuarioService) es mockeado.
 */
@WebMvcTest(UsuarioController.class) // Levanta solo el contexto web de Spring para el UsuarioController
// @AutoConfigureMockMvc(addFilters = false) // Descomentar si usas Spring Security y tienes problemas con filtros
@DisplayName("Tests de Integración (WebMvcTest) para UsuarioController")
class UsuarioControllerTest {

    @Autowired // Inyecta MockMvc para realizar llamadas HTTP simuladas
    private MockMvc mockMvc;

    @MockBean // Crea un mock del servicio y lo inyecta en el controlador
    private UsuarioService usuarioService;

    @Autowired // Inyecta ObjectMapper para convertir objetos Java a/desde JSON
    private ObjectMapper objectMapper;

    private Usuario testUsuario;
    private Rol testRol;

    /**
     * Configuración inicial antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        testRol = new Rol();
        testRol.setId(1L);
        testRol.setNombreRol("ADMIN");
        testRol.setEstadoRol(true); // Asumiendo estado para Rol

        testUsuario = new Usuario();
        testUsuario.setIdUsuario(1);
        testUsuario.setNomUsuario("Test");
        testUsuario.setApUsuario("User");
        testUsuario.setCorreoUsuario("test@example.com");
        testUsuario.setPassUsuario("hashed_password");
        testUsuario.setEstadoUsuario(true);
        testUsuario.setRol(testRol);
        testUsuario.setIdTienda(101);
    }

    // --- PRUEBAS PARA crearUsuario (POST /usuarios) ---

    @Test
    @DisplayName("POST /usuarios - Debería crear un usuario y retornar 201 CREATED")
    void testCrearUsuario_Success() throws Exception {
        Mockito.when(usuarioService.crearUsuario(any(Usuario.class))).thenReturn(testUsuario);

        mockMvc.perform(post("/usuarios") // Simula una petición POST a /usuarios
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUsuario))) // Convierte el objeto a JSON
                .andExpect(status().isCreated()) // Espera un estado HTTP 201
                .andExpect(jsonPath("$.idUsuario").value(testUsuario.getIdUsuario()))
                .andExpect(jsonPath("$.nomUsuario").value(testUsuario.getNomUsuario()))
                .andExpect(jsonPath("$.correoUsuario").value(testUsuario.getCorreoUsuario()))
                .andExpect(jsonPath("$.estadoUsuario").value(testUsuario.getEstadoUsuario()));

        // Verifica que el método del servicio fue llamado exactamente una vez con cualquier objeto Usuario
        Mockito.verify(usuarioService, Mockito.times(1)).crearUsuario(any(Usuario.class));
    }

    @Test
    @DisplayName("POST /usuarios - Debería retornar 400 BAD_REQUEST si el correo está duplicado")
    void testCrearUsuario_EmailDuplicate_BadRequest() throws Exception {
        String errorMessage = "El correo test@example.com ya está en uso.";
        Mockito.when(usuarioService.crearUsuario(any(Usuario.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUsuario)))
                .andExpect(status().isBadRequest()) // Espera un estado HTTP 400
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage));

        Mockito.verify(usuarioService, Mockito.times(1)).crearUsuario(any(Usuario.class));
    }

    @Test
    @DisplayName("POST /usuarios - Debería retornar 404 NOT_FOUND si el rol no existe")
    void testCrearUsuario_RoleNotFound_NotFound() throws Exception {
        String errorMessage = "Rol no encontrado con ID: 99";
        Mockito.when(usuarioService.crearUsuario(any(Usuario.class)))
                .thenThrow(new EntityNotFoundException(errorMessage));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUsuario)))
                .andExpect(status().isNotFound()) // Espera un estado HTTP 404
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(errorMessage));

        Mockito.verify(usuarioService, Mockito.times(1)).crearUsuario(any(Usuario.class));
    }

    // --- PRUEBAS PARA actualizarUsuario (PUT /usuarios/{id}) ---

    @Test
    @DisplayName("PUT /usuarios/{id} - Debería actualizar un usuario y retornar 200 OK")
    void testActualizarUsuario_Success() throws Exception {
        Usuario updatedUsuario = new Usuario();
        updatedUsuario.setIdUsuario(testUsuario.getIdUsuario());
        updatedUsuario.setNomUsuario("Updated Name");
        updatedUsuario.setApUsuario("Updated Last Name");
        updatedUsuario.setCorreoUsuario("updated@example.com");
        updatedUsuario.setPassUsuario("new_hashed_password");
        updatedUsuario.setEstadoUsuario(true);
        updatedUsuario.setRol(testRol);
        updatedUsuario.setIdTienda(testUsuario.getIdTienda());

        Mockito.when(usuarioService.actualizarUsuario(eq(testUsuario.getIdUsuario()), any(Usuario.class)))
                .thenReturn(updatedUsuario);

        mockMvc.perform(put("/usuarios/{id}", testUsuario.getIdUsuario()) // Simula PUT a /usuarios/{id}
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUsuario)))
                .andExpect(status().isOk()) // Espera un estado HTTP 200
                .andExpect(jsonPath("$.idUsuario").value(updatedUsuario.getIdUsuario()))
                .andExpect(jsonPath("$.nomUsuario").value(updatedUsuario.getNomUsuario()))
                .andExpect(jsonPath("$.correoUsuario").value(updatedUsuario.getCorreoUsuario()));

        Mockito.verify(usuarioService, Mockito.times(1))
                .actualizarUsuario(eq(testUsuario.getIdUsuario()), any(Usuario.class));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} - Debería retornar 404 NOT_FOUND si el usuario no existe")
    void testActualizarUsuario_UserNotFound_NotFound() throws Exception {
        Integer nonExistentId = 99;
        String errorMessage = "Usuario no encontrado con ID: " + nonExistentId;
        Mockito.when(usuarioService.actualizarUsuario(eq(nonExistentId), any(Usuario.class)))
                .thenThrow(new EntityNotFoundException(errorMessage));

        mockMvc.perform(put("/usuarios/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Usuario())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(errorMessage));

        Mockito.verify(usuarioService, Mockito.times(1))
                .actualizarUsuario(eq(nonExistentId), any(Usuario.class));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} - Debería retornar 400 BAD_REQUEST si el correo está en uso por otro usuario")
    void testActualizarUsuario_EmailInUse_BadRequest() throws Exception {
        Integer userId = testUsuario.getIdUsuario();
        String errorMessage = "El correo ya está en uso por otro usuario activo.";
        Mockito.when(usuarioService.actualizarUsuario(eq(userId), any(Usuario.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(put("/usuarios/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Usuario())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage));

        Mockito.verify(usuarioService, Mockito.times(1))
                .actualizarUsuario(eq(userId), any(Usuario.class));
    }

    // --- PRUEBAS PARA desactivarUsuario (DELETE /usuarios/{id}) ---

    @Test
    @DisplayName("DELETE /usuarios/{id} - Debería desactivar un usuario y retornar 200 OK")
    void testDesactivarUsuario_Success() throws Exception {
        Usuario deactivatedUsuario = new Usuario();
        deactivatedUsuario.setIdUsuario(testUsuario.getIdUsuario());
        deactivatedUsuario.setNomUsuario(testUsuario.getNomUsuario());
        deactivatedUsuario.setEstadoUsuario(false); // <--- Clave: estado false

        Mockito.when(usuarioService.desactivarUsuario(eq(testUsuario.getIdUsuario())))
                .thenReturn(deactivatedUsuario);

        mockMvc.perform(delete("/usuarios/{id}", testUsuario.getIdUsuario())) // Simula DELETE a /usuarios/{id}
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(deactivatedUsuario.getIdUsuario()))
                .andExpect(jsonPath("$.estadoUsuario").value(false));

        Mockito.verify(usuarioService, Mockito.times(1))
                .desactivarUsuario(eq(testUsuario.getIdUsuario()));
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} - Debería retornar 404 NOT_FOUND si el usuario activo no existe")
    void testDesactivarUsuario_NotFound() throws Exception {
        Integer nonExistentId = 99;
        String errorMessage = "Usuario activo no encontrado con ID: " + nonExistentId;
        Mockito.when(usuarioService.desactivarUsuario(eq(nonExistentId)))
                .thenThrow(new EntityNotFoundException(errorMessage));

        mockMvc.perform(delete("/usuarios/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(errorMessage));

        Mockito.verify(usuarioService, Mockito.times(1))
                .desactivarUsuario(eq(nonExistentId));
    }

    // --- PRUEBAS PARA reactivarUsuario (PUT /usuarios/{id}/reactivar) ---

    @Test
    @DisplayName("PUT /usuarios/{id}/reactivar - Debería reactivar un usuario y retornar 200 OK")
    void testReactivarUsuario_Success() throws Exception {
        Usuario reactivatedUsuario = new Usuario();
        reactivatedUsuario.setIdUsuario(testUsuario.getIdUsuario());
        reactivatedUsuario.setNomUsuario(testUsuario.getNomUsuario());
        reactivatedUsuario.setEstadoUsuario(true); // <--- Clave: estado true

        Mockito.when(usuarioService.reactivarUsuario(eq(testUsuario.getIdUsuario())))
                .thenReturn(reactivatedUsuario);

        mockMvc.perform(put("/usuarios/{id}/reactivar", testUsuario.getIdUsuario())) // Simula PUT a /usuarios/{id}/reactivar
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(reactivatedUsuario.getIdUsuario()))
                .andExpect(jsonPath("$.estadoUsuario").value(true));

        Mockito.verify(usuarioService, Mockito.times(1))
                .reactivarUsuario(eq(testUsuario.getIdUsuario()));
    }

    @Test
    @DisplayName("PUT /usuarios/{id}/reactivar - Debería retornar 404 NOT_FOUND si el usuario inactivo no existe")
    void testReactivarUsuario_NotFound() throws Exception {
        Integer nonExistentId = 99;
        String errorMessage = "Usuario inactivo no encontrado con ID: " + nonExistentId;
        Mockito.when(usuarioService.reactivarUsuario(eq(nonExistentId)))
                .thenThrow(new EntityNotFoundException(errorMessage));

        mockMvc.perform(put("/usuarios/{id}/reactivar", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(errorMessage));

        Mockito.verify(usuarioService, Mockito.times(1))
                .reactivarUsuario(eq(nonExistentId));
    }

    // --- PRUEBAS PARA obtenerUsuarioPorId (GET /usuarios/{id}) ---

    @Test
    @DisplayName("GET /usuarios/{id} - Debería retornar un usuario activo y 200 OK")
    void testObtenerUsuarioPorId_Success() throws Exception {
        Mockito.when(usuarioService.obtenerUsuarioPorId(eq(testUsuario.getIdUsuario())))
                .thenReturn(Optional.of(testUsuario));

        mockMvc.perform(get("/usuarios/{id}", testUsuario.getIdUsuario())) // Simula GET a /usuarios/{id}
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(testUsuario.getIdUsuario()))
                .andExpect(jsonPath("$.nomUsuario").value(testUsuario.getNomUsuario()));

        Mockito.verify(usuarioService, Mockito.times(1))
                .obtenerUsuarioPorId(eq(testUsuario.getIdUsuario()));
    }

    @Test
    @DisplayName("GET /usuarios/{id} - Debería retornar 404 NOT_FOUND si el usuario activo no existe")
    void testObtenerUsuarioPorId_NotFound() throws Exception {
        Integer nonExistentId = 99;
        Mockito.when(usuarioService.obtenerUsuarioPorId(eq(nonExistentId)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/usuarios/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Usuario activo no encontrado con id: " + nonExistentId));

        Mockito.verify(usuarioService, Mockito.times(1))
                .obtenerUsuarioPorId(eq(nonExistentId));
    }

    // --- PRUEBAS PARA obtenerTodosUsuariosActivos (GET /usuarios/activos) ---

    @Test
    @DisplayName("GET /usuarios/activos - Debería retornar una lista de usuarios activos y 200 OK")
    void testObtenerTodosUsuariosActivos_Success() throws Exception {
        Usuario usuario2 = new Usuario();
        usuario2.setIdUsuario(2);
        usuario2.setNomUsuario("Active User 2");
        usuario2.setEstadoUsuario(true);
        usuario2.setRol(testRol); // Asigna un rol
        usuario2.setIdTienda(102);


        java.util.List<Usuario> activeUsers = Arrays.asList(testUsuario, usuario2); // Fíjate en el "java.util."
        Mockito.when(usuarioService.obtenerTodosUsuariosActivos()).thenReturn(activeUsers);

        mockMvc.perform(get("/usuarios/activos")) // Simula GET a /usuarios/activos
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idUsuario").value(testUsuario.getIdUsuario()))
                .andExpect(jsonPath("$[1].idUsuario").value(usuario2.getIdUsuario()));

        Mockito.verify(usuarioService, Mockito.times(1)).obtenerTodosUsuariosActivos();
    }

    @Test
    @DisplayName("GET /usuarios/activos - Debería retornar una lista vacía y 200 OK si no hay usuarios activos")
    void testObtenerTodosUsuariosActivos_EmptyList() throws Exception {
        Mockito.when(usuarioService.obtenerTodosUsuariosActivos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/usuarios/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(usuarioService, Mockito.times(1)).obtenerTodosUsuariosActivos();
    }

    // --- PRUEBAS PARA obtenerTodosUsuarios (GET /usuarios/todos) ---

    @Test
    @DisplayName("GET /usuarios/todos - Debería retornar una lista de todos los usuarios (activos e inactivos) y 200 OK")
    void testObtenerTodosUsuarios_Success() throws Exception {
        Usuario inactiveUser = new Usuario();
        inactiveUser.setIdUsuario(3);
        inactiveUser.setNomUsuario("Inactive User");
        inactiveUser.setEstadoUsuario(false); // Inactivo
        inactiveUser.setRol(testRol); // Asigna un rol
        inactiveUser.setIdTienda(103);

        java.util.List<Usuario> allUsers = Arrays.asList(testUsuario, inactiveUser); // Fíjate en el "java.util."
        Mockito.when(usuarioService.obtenerTodosUsuarios()).thenReturn(allUsers);

        mockMvc.perform(get("/usuarios/todos")) // Simula GET a /usuarios/todos
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idUsuario").value(testUsuario.getIdUsuario()))
                .andExpect(jsonPath("$[1].idUsuario").value(inactiveUser.getIdUsuario()))
                .andExpect(jsonPath("$[1].estadoUsuario").value(false)); // Verifica el estado inactivo

        Mockito.verify(usuarioService, Mockito.times(1)).obtenerTodosUsuarios();
    }

    @Test
    @DisplayName("GET /usuarios/todos - Debería retornar una lista vacía y 200 OK si no hay usuarios")
    void testObtenerTodosUsuarios_EmptyList() throws Exception {
        Mockito.when(usuarioService.obtenerTodosUsuarios()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/usuarios/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(usuarioService, Mockito.times(1)).obtenerTodosUsuarios();
    }

    // --- PRUEBAS PARA asignarRol (PUT /usuarios/{id}/asignar-rol/{rolId}) ---

    @Test
    @DisplayName("PUT /usuarios/{id}/asignar-rol/{rolId} - Debería asignar un rol a un usuario y retornar 200 OK")
    void testAsignarRol_Success() throws Exception {
        Long newRolId = 2L;
        Rol newRol = new Rol();
        newRol.setId(newRolId);
        newRol.setNombreRol("OPERATOR");
        newRol.setEstadoRol(true);

        Usuario userWithNewRol = new Usuario();
        userWithNewRol.setIdUsuario(testUsuario.getIdUsuario());
        userWithNewRol.setNomUsuario(testUsuario.getNomUsuario());
        userWithNewRol.setEstadoUsuario(true);
        userWithNewRol.setRol(newRol); // Rol actualizado

        Mockito.when(usuarioService.asignarRol(eq(testUsuario.getIdUsuario()), eq(newRolId)))
                .thenReturn(userWithNewRol);

        mockMvc.perform(put("/usuarios/{id}/asignar-rol/{rolId}", testUsuario.getIdUsuario(), newRolId)) // Simula PUT
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(userWithNewRol.getIdUsuario()))
                .andExpect(jsonPath("$.rol.id").value(newRolId))
                .andExpect(jsonPath("$.rol.nombreRol").value(newRol.getNombreRol()));

        Mockito.verify(usuarioService, Mockito.times(1))
                .asignarRol(eq(testUsuario.getIdUsuario()), eq(newRolId));
    }

    @Test
    @DisplayName("PUT /usuarios/{id}/asignar-rol/{rolId} - Debería retornar 404 NOT_FOUND si el usuario o rol no existen")
    void testAsignarRol_NotFound() throws Exception {
        Integer nonExistentUserId = 99;
        Long nonExistentRolId = 999L;
        String errorMessage = "Usuario o Rol no encontrado.";
        Mockito.when(usuarioService.asignarRol(eq(nonExistentUserId), eq(nonExistentRolId)))
                .thenThrow(new EntityNotFoundException(errorMessage));

        mockMvc.perform(put("/usuarios/{id}/asignar-rol/{rolId}", nonExistentUserId, nonExistentRolId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(errorMessage));

        Mockito.verify(usuarioService, Mockito.times(1))
                .asignarRol(eq(nonExistentUserId), eq(nonExistentRolId));
    }

    // --- PRUEBAS PARA listarUsuariosPorTienda (GET /usuarios/tienda/{idTienda}) ---

    @Test
    @DisplayName("GET /usuarios/tienda/{idTienda} - Debería retornar una lista de usuarios de una tienda y 200 OK")
    void testListarUsuariosPorTienda_Success() throws Exception {
        Integer tiendaId = 101;
        Usuario usuarioTienda1 = new Usuario();
        usuarioTienda1.setIdUsuario(4);
        usuarioTienda1.setNomUsuario("User Tienda 1");
        usuarioTienda1.setEstadoUsuario(true);
        usuarioTienda1.setRol(testRol);
        usuarioTienda1.setIdTienda(tiendaId);

        java.util.List<Usuario> usersInStore = Arrays.asList(usuarioTienda1); // Fíjate en el "java.util."
        Mockito.when(usuarioService.listarUsuariosPorTienda(eq(tiendaId))).thenReturn(usersInStore);

        mockMvc.perform(get("/usuarios/tienda/{idTienda}", tiendaId)) // Simula GET a /usuarios/tienda/{idTienda}
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idUsuario").value(usuarioTienda1.getIdUsuario()))
                .andExpect(jsonPath("$[0].idTienda").value(tiendaId));

        Mockito.verify(usuarioService, Mockito.times(1)).listarUsuariosPorTienda(eq(tiendaId));
    }

    @Test
    @DisplayName("GET /usuarios/tienda/{idTienda} - Debería retornar una lista vacía y 200 OK si no hay usuarios en la tienda")
    void testListarUsuariosPorTienda_EmptyList() throws Exception {
        Integer nonExistentTiendaId = 999;
        Mockito.when(usuarioService.listarUsuariosPorTienda(eq(nonExistentTiendaId))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/usuarios/tienda/{idTienda}", nonExistentTiendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(usuarioService, Mockito.times(1)).listarUsuariosPorTienda(eq(nonExistentTiendaId));
    }
}
