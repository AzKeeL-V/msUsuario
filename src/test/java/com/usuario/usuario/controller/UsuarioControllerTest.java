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
 * Clase de pruebas para UsuarioController.
 * Se enfoca en probar la capa web (controlador) de forma aislada.
 * Utiliza @WebMvcTest para configurar solo lo necesario para probar el controlador.
 * El UsuarioService es simulado (mockeado) para no depender de la lógica de negocio real.
 */
@WebMvcTest(UsuarioController.class)
@DisplayName("Tests de Integración (WebMvcTest) para UsuarioController")
class UsuarioControllerTest {

    // Objeto para simular peticiones HTTP al controlador.
    @Autowired
    private MockMvc mockMvc;

    // Crea un mock del servicio para poder controlar su comportamiento en las pruebas.
    @MockBean
    private UsuarioService usuarioService;

    // Utilidad para convertir objetos Java a JSON y viceversa.
    @Autowired
    private ObjectMapper objectMapper;

    // Datos de prueba que se usarán en múltiples tests.
    private Usuario testUsuario;
    private Rol testRol;

    /**
     * Método que se ejecuta ANTES de cada test.
     * Inicializa los objetos de prueba para asegurar que cada test
     * comience con un estado limpio y predecible.
     */
    @BeforeEach
    void setUp() {
        // Se crea un rol de prueba.
        testRol = new Rol();
        testRol.setId(1L);
        testRol.setNombreRol("ADMIN");
        testRol.setEstadoRol(true);

        // Se crea un usuario de prueba y se le asigna el rol.
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
        // ARRANGE: Se configura el mock del servicio. Cuando se llame a 'crearUsuario',
        // devolverá el objeto 'testUsuario'.
        Mockito.when(usuarioService.crearUsuario(any(Usuario.class))).thenReturn(testUsuario);

        // ACT & ASSERT: Se ejecuta la petición POST y se verifican los resultados.
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUsuario)))
                .andExpect(status().isCreated()) // Espera un estado HTTP 201 (Created).
                .andExpect(jsonPath("$.idUsuario").value(testUsuario.getIdUsuario())) // Verifica el ID en el JSON de respuesta.
                .andExpect(jsonPath("$.nomUsuario").value(testUsuario.getNomUsuario()))
                .andExpect(jsonPath("$.correoUsuario").value(testUsuario.getCorreoUsuario()))
                .andExpect(jsonPath("$._links.self.href").exists()); // Verifica que el enlace HATEOAS existe.

        // VERIFY: Confirma que el método 'crearUsuario' del servicio fue llamado una vez.
        Mockito.verify(usuarioService, Mockito.times(1)).crearUsuario(any(Usuario.class));
    }

    @Test
    @DisplayName("POST /usuarios - Debería retornar 400 BAD_REQUEST si el correo está duplicado")
    void testCrearUsuario_EmailDuplicate_BadRequest() throws Exception {
        // ARRANGE: Se configura el mock para que lance una excepción, simulando un error de negocio.
        String errorMessage = "El correo test@example.com ya está en uso.";
        Mockito.when(usuarioService.crearUsuario(any(Usuario.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // ACT & ASSERT: Se realiza la petición y se verifica la respuesta de error 400.
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUsuario)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage)); // Verifica el mensaje de error.

        Mockito.verify(usuarioService, Mockito.times(1)).crearUsuario(any(Usuario.class));
    }

    // --- PRUEBAS PARA actualizarUsuario (PUT /usuarios/{id}) ---
    
    @Test
    @DisplayName("PUT /usuarios/{id} - Debería actualizar un usuario y retornar 200 OK")
    void testActualizarUsuario_Success() throws Exception {
        // ARRANGE: Se crea un objeto con los datos actualizados.
        Usuario updatedUsuario = new Usuario();
        updatedUsuario.setIdUsuario(testUsuario.getIdUsuario());
        updatedUsuario.setNomUsuario("Updated Name");
        updatedUsuario.setCorreoUsuario("updated@example.com");

        // Se configura el mock para que devuelva el usuario actualizado.
        Mockito.when(usuarioService.actualizarUsuario(eq(testUsuario.getIdUsuario()), any(Usuario.class)))
                .thenReturn(updatedUsuario);

        // ACT & ASSERT: Se ejecuta la petición PUT.
        mockMvc.perform(put("/usuarios/{id}", testUsuario.getIdUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUsuario)))
                .andExpect(status().isOk()) // Espera un estado HTTP 200 (OK).
                .andExpect(jsonPath("$.nomUsuario").value("Updated Name"))
                .andExpect(jsonPath("$.correoUsuario").value("updated@example.com"));

        Mockito.verify(usuarioService, Mockito.times(1))
                .actualizarUsuario(eq(testUsuario.getIdUsuario()), any(Usuario.class));
    }


    // --- PRUEBAS PARA desactivarUsuario (DELETE /usuarios/{id}) ---

    @Test
    @DisplayName("DELETE /usuarios/{id} - Debería desactivar un usuario y retornar 200 OK")
    void testDesactivarUsuario_Success() throws Exception {
        // ARRANGE: Se crea un objeto que representa el estado final del usuario (desactivado).
        Usuario deactivatedUsuario = new Usuario();
        deactivatedUsuario.setIdUsuario(testUsuario.getIdUsuario());
        deactivatedUsuario.setEstadoUsuario(false); // Estado cambiado a false.

        Mockito.when(usuarioService.desactivarUsuario(eq(testUsuario.getIdUsuario())))
                .thenReturn(deactivatedUsuario);

        // ACT & ASSERT: Se simula una petición DELETE.
        mockMvc.perform(delete("/usuarios/{id}", testUsuario.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoUsuario").value(false)) // Verifica que el estado en la respuesta sea false.
                .andExpect(jsonPath("$._links.reactivate.href").exists()); // Ahora debe existir un enlace para reactivar.

        Mockito.verify(usuarioService, Mockito.times(1)).desactivarUsuario(eq(testUsuario.getIdUsuario()));
    }


    // --- PRUEBAS PARA obtenerUsuarioPorId (GET /usuarios/{id}) ---

    @Test
    @DisplayName("GET /usuarios/{id} - Debería retornar un usuario activo y 200 OK")
    void testObtenerUsuarioPorId_Success() throws Exception {
        // ARRANGE: Se configura el mock para que devuelva un Optional que contiene el usuario.
        Mockito.when(usuarioService.obtenerUsuarioPorId(eq(testUsuario.getIdUsuario())))
                .thenReturn(Optional.of(testUsuario));

        // ACT & ASSERT: Se realiza la petición GET.
        mockMvc.perform(get("/usuarios/{id}", testUsuario.getIdUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(testUsuario.getIdUsuario()));

        Mockito.verify(usuarioService, Mockito.times(1)).obtenerUsuarioPorId(eq(testUsuario.getIdUsuario()));
    }

    @Test
    @DisplayName("GET /usuarios/{id} - Debería retornar 404 NOT_FOUND si el usuario activo no existe")
    void testObtenerUsuarioPorId_NotFound() throws Exception {
        // ARRANGE: Se configura el mock para que devuelva un Optional vacío.
        Mockito.when(usuarioService.obtenerUsuarioPorId(eq(99)))
                .thenReturn(Optional.empty());

        // ACT & ASSERT: Se realiza la petición y se verifica la respuesta 404.
        mockMvc.perform(get("/usuarios/{id}", 99))
                .andExpect(status().isNotFound());

        Mockito.verify(usuarioService, Mockito.times(1)).obtenerUsuarioPorId(eq(99));
    }


    // --- PRUEBAS PARA obtenerTodosUsuariosActivos (GET /usuarios/activos) ---

    @Test
    @DisplayName("GET /usuarios/activos - Debería retornar una lista de usuarios activos y 200 OK")
    void testObtenerTodosUsuariosActivos_Success() throws Exception {
        // ARRANGE: Se crea un segundo usuario para la lista.
        Usuario usuario2 = new Usuario();
        usuario2.setIdUsuario(2);
        usuario2.setEstadoUsuario(true);

        Mockito.when(usuarioService.obtenerTodosUsuariosActivos()).thenReturn(Arrays.asList(testUsuario, usuario2));

        // ACT & ASSERT:
        mockMvc.perform(get("/usuarios/activos"))
                .andExpect(status().isOk())
                // CORRECCIÓN: Las respuestas de colección con HATEOAS anidan la lista dentro de '_embedded'.
                // Se debe verificar la longitud y los elementos dentro de esta estructura.
                .andExpect(jsonPath("$._embedded.usuarioList.length()").value(2))
                .andExpect(jsonPath("$._embedded.usuarioList[0].idUsuario").value(testUsuario.getIdUsuario()))
                .andExpect(jsonPath("$._embedded.usuarioList[1].idUsuario").value(usuario2.getIdUsuario()));

        Mockito.verify(usuarioService, Mockito.times(1)).obtenerTodosUsuariosActivos();
    }

    @Test
    @DisplayName("GET /usuarios/activos - Debería retornar una respuesta apropiada si no hay usuarios activos")
    void testObtenerTodosUsuariosActivos_EmptyList() throws Exception {
        // ARRANGE: Se configura el mock para que devuelva una lista vacía.
        Mockito.when(usuarioService.obtenerTodosUsuariosActivos()).thenReturn(Collections.emptyList());

        // ACT & ASSERT:
        mockMvc.perform(get("/usuarios/activos"))
                .andExpect(status().isOk())
                // CORRECCIÓN: Para una lista vacía en HATEOAS, la propiedad '_embedded' no debería existir.
                // Esta es una forma más robusta de verificar una colección vacía.
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists()); // El enlace a 'self' sí debe existir.

        Mockito.verify(usuarioService, Mockito.times(1)).obtenerTodosUsuariosActivos();
    }

    // --- PRUEBAS PARA obtenerTodosUsuarios (GET /usuarios/todos) ---

    @Test
    @DisplayName("GET /usuarios/todos - Debería retornar todos los usuarios (activos e inactivos) y 200 OK")
    void testObtenerTodosUsuarios_Success() throws Exception {
        // ARRANGE: Se crea un usuario inactivo para la lista.
        Usuario inactiveUser = new Usuario();
        inactiveUser.setIdUsuario(3);
        inactiveUser.setEstadoUsuario(false);

        Mockito.when(usuarioService.obtenerTodosUsuarios()).thenReturn(Arrays.asList(testUsuario, inactiveUser));

        // ACT & ASSERT:
        mockMvc.perform(get("/usuarios/todos"))
                .andExpect(status().isOk())
                // CORRECCIÓN: Se ajusta el jsonPath para la estructura HATEOAS.
                .andExpect(jsonPath("$._embedded.usuarioList.length()").value(2))
                .andExpect(jsonPath("$._embedded.usuarioList[0].estadoUsuario").value(true))
                .andExpect(jsonPath("$._embedded.usuarioList[1].estadoUsuario").value(false));

        Mockito.verify(usuarioService, Mockito.times(1)).obtenerTodosUsuarios();
    }


    // --- PRUEBAS PARA listarUsuariosPorTienda (GET /usuarios/tienda/{idTienda}) ---

    @Test
    @DisplayName("GET /usuarios/tienda/{idTienda} - Debería retornar usuarios de una tienda y 200 OK")
    void testListarUsuariosPorTienda_Success() throws Exception {
        // ARRANGE: El 'testUsuario' ya pertenece a la tienda 101.
        Mockito.when(usuarioService.listarUsuariosPorTienda(eq(101))).thenReturn(Collections.singletonList(testUsuario));

        // ACT & ASSERT:
        mockMvc.perform(get("/usuarios/tienda/{idTienda}", 101))
                .andExpect(status().isOk())
                // CORRECCIÓN: Se ajusta el jsonPath para la estructura HATEOAS.
                // CORRECCIÓN: Se verifica la longitud correcta (1) que coincide con la configuración del mock.
                .andExpect(jsonPath("$._embedded.usuarioList.length()").value(1))
                .andExpect(jsonPath("$._embedded.usuarioList[0].idTienda").value(101));

        Mockito.verify(usuarioService, Mockito.times(1)).listarUsuariosPorTienda(eq(101));
    }
    
    // Y aquí puedes añadir el resto de los tests que ya funcionaban, bien comentados...
    // Por ejemplo: testActualizarUsuario_UserNotFound_NotFound, testAsignarRol_Success, etc.
    // Todos ellos seguirían la misma estructura de Arrange-Act-Assert-Verify.
}