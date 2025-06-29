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

/**
 * Clase de pruebas unitarias para el controlador 'RolController'.
 * El objetivo es verificar que el controlador responde correctamente a las peticiones HTTP
 * sin depender de la lógica de negocio interna ni de la base de datos.
 */
// Anotación que configura el entorno de prueba de Spring MVC, centrándose únicamente en 'RolController'.
// Esto evita cargar toda la aplicación, haciendo las pruebas más rápidas y aisladas.
@WebMvcTest(RolController.class)
class RolControllerTest {

    /**
     * Objeto para simular peticiones HTTP al controlador.
     * Es inyectado por Spring y permite realizar llamadas a los endpoints (ej. GET, POST)
     * y verificar sus respuestas.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Crea un "mock" o simulador del 'RolService'.
     * En lugar de usar el servicio real (que se conectaría a la BBDD),
     * se crea un objeto falso sobre el que se tiene control total para definir
     * su comportamiento en cada prueba. Esto aísla al controlador.
     */
    @MockBean
    private RolService rolService;

    /**
     * Utilidad para convertir objetos Java a formato JSON y viceversa.
     * Es necesaria para enviar datos en el cuerpo de las peticiones POST y PUT.
     */
    @Autowired
    private ObjectMapper objectMapper;

    // Variables para almacenar los datos de prueba que se usarán en múltiples tests.
    private Rol rolAdmin;
    private Rol rolUsuario;

    /**
     * Método de configuración que se ejecuta antes de cada test (@Test).
     * Su función es inicializar los datos de prueba para asegurar que cada test
     * comience con un estado limpio y predecible.
     */
    @BeforeEach
    void setUp() {
        rolAdmin = new Rol(1L, "ADMIN", Arrays.asList(Permiso.CREAR_USUARIO, Permiso.VER_USUARIO), true);
        rolUsuario = new Rol(2L, "USUARIO", Arrays.asList(Permiso.VER_USUARIO), true);
    }

    // --- PRUEBAS PARA crearRol (POST /roles) ---

    /**
     * Prueba el escenario exitoso de la creación de un rol.
     * Se espera que el controlador devuelva un estado HTTP 201 (Created) y
     * que el cuerpo de la respuesta contenga los datos del rol recién creado.
     */
    @Test
    void testCrearRol_Exito() throws Exception {
        // ARRANGE: Configuración del mock. Se le indica al servicio simulado
        // qué debe devolver cuando su método 'crearRol' sea invocado.
        Mockito.when(rolService.crearRol(any(Rol.class))).thenReturn(rolAdmin);

        // ACT & ASSERT: Se ejecuta la petición y se verifican los resultados.
        mockMvc.perform(post("/roles") // Simula una petición POST a la ruta /roles.
                        .contentType(MediaType.APPLICATION_JSON) // Indica que el contenido enviado es JSON.
                        .content(objectMapper.writeValueAsString(rolAdmin))) // Convierte el objeto 'rolAdmin' a JSON y lo envía como cuerpo.
                .andExpect(status().isCreated()) // Verifica que el código de respuesta HTTP sea 201.
                .andExpect(jsonPath("$.id").value(rolAdmin.getId())) // Verifica que el 'id' en el JSON de respuesta sea el correcto.
                .andExpect(jsonPath("$.nombreRol").value(rolAdmin.getNombreRol())) // Verifica el 'nombreRol'.
                .andExpect(jsonPath("$.estadoRol").value(rolAdmin.getEstadoRol())) // Verifica el 'estadoRol'.
                // Verifica que los enlaces HATEOAS esperados estén en la respuesta.
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.update.href").exists())
                .andExpect(jsonPath("$._links.deactivate.href").exists())
                .andExpect(jsonPath("$._links.all-roles.href").exists());

        // VERIFY: Confirma que el método 'crearRol' del servicio fue llamado exactamente una vez.
        Mockito.verify(rolService, Mockito.times(1)).crearRol(any(Rol.class));
    }

    /**
     * Prueba el caso de error al intentar crear un rol con un nombre que ya existe.
     * Se espera un estado HTTP 400 (Bad Request) y un mensaje de error específico.
     */
    @Test
    void testCrearRol_BadRequest_NombreDuplicado() throws Exception {
        // ARRANGE: Se configura el mock para que lance una excepción, simulando la lógica de negocio
        // que impide nombres duplicados.
        Mockito.when(rolService.crearRol(any(Rol.class)))
                .thenThrow(new IllegalArgumentException("Ya existe un rol activo con el nombre: ADMIN"));

        // ACT & ASSERT: Se realiza la petición y se verifica la respuesta de error.
        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolAdmin)))
                .andExpect(status().isBadRequest()) // Verifica que el estado HTTP sea 400.
                .andExpect(jsonPath("$.status").value(400)) // Verifica el campo 'status' en el JSON de error.
                .andExpect(jsonPath("$.error").value("Bad Request")) // Verifica el campo 'error'.
                .andExpect(jsonPath("$.message").value("Ya existe un rol activo con el nombre: ADMIN")); // Verifica el mensaje de error.

        // VERIFY: Asegura que se intentó llamar al servicio.
        Mockito.verify(rolService, Mockito.times(1)).crearRol(any(Rol.class));
    }

    // --- PRUEBAS PARA actualizarRol (PUT /roles/{id}) ---

    /**
     * Prueba la actualización exitosa de un rol existente.
     * Se espera un estado HTTP 200 (OK) y que la respuesta contenga los datos actualizados.
     */
    @Test
    void testActualizarRol_Exito() throws Exception {
        // ARRANGE: Se crea un objeto con los datos que se usarán para actualizar.
        Rol rolActualizado = new Rol(1L, "ADMIN_NUEVO", Arrays.asList(Permiso.ACTUALIZAR_USUARIO), true);

        // Se configura el mock para que devuelva el 'rolActualizado' cuando se intente actualizar el rol con ID 1.
        Mockito.when(rolService.actualizarRol(eq(1L), any(Rol.class))).thenReturn(rolActualizado);

        // ACT & ASSERT: Se realiza una petición PUT a la ruta específica del rol.
        mockMvc.perform(put("/roles/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolActualizado)))
                .andExpect(status().isOk()) // Verifica que el estado HTTP sea 200.
                .andExpect(jsonPath("$.id").value(rolActualizado.getId())) // Verifica que el ID no haya cambiado.
                .andExpect(jsonPath("$.nombreRol").value(rolActualizado.getNombreRol())) // Verifica que el nombre se haya actualizado.
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.deactivate.href").exists())
                .andExpect(jsonPath("$._links.all-roles.href").exists());

        // VERIFY: Confirma que el método 'actualizarRol' del servicio fue invocado correctamente.
        Mockito.verify(rolService, Mockito.times(1)).actualizarRol(eq(1L), any(Rol.class));
    }

    /**
     * Prueba el caso de error al intentar actualizar un rol que no existe.
     * Se espera un estado HTTP 404 (Not Found).
     */
    @Test
    void testActualizarRol_NotFound() throws Exception {
        // ARRANGE: Se configura el mock para que lance 'EntityNotFoundException' al buscar un ID inexistente (99).
        Mockito.when(rolService.actualizarRol(eq(99L), any(Rol.class)))
                .thenThrow(new EntityNotFoundException("Rol no encontrado con id: 99"));

        // ACT & ASSERT: Se realiza la petición y se verifica la respuesta 404.
        mockMvc.perform(put("/roles/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Rol()))) // El contenido no importa, la ID es lo que causa el error.
                .andExpect(status().isNotFound()) // Verifica estado 404.
                .andExpect(jsonPath("$.message").value("Rol no encontrado con id: 99")); // Verifica el mensaje.

        // VERIFY: Asegura que se intentó llamar al servicio con el ID incorrecto.
        Mockito.verify(rolService, Mockito.times(1)).actualizarRol(eq(99L), any(Rol.class));
    }

    // --- PRUEBAS PARA desactivarRol (DELETE /roles/{id}) ---

    /**
     * Prueba la desactivación exitosa de un rol.
     * Se espera un estado HTTP 200 (OK) y que la respuesta muestre el rol con 'estadoRol' en 'false'.
     */
    @Test
    void testDesactivarRol_Exito() throws Exception {
        // ARRANGE: Se crea un objeto que representa el estado final del rol (desactivado).
        Rol rolDesactivado = new Rol(1L, "ADMIN", Collections.emptyList(), false);
        // Se configura el mock para que devuelva este objeto al desactivar el rol con ID 1.
        Mockito.when(rolService.desactivarRol(eq(1L))).thenReturn(rolDesactivado);

        // ACT & ASSERT: Se simula una petición DELETE.
        mockMvc.perform(delete("/roles/{id}", 1L))
                .andExpect(status().isOk()) // Verifica estado 200.
                .andExpect(jsonPath("$.estadoRol").value(false)) // Verifica que el estado del rol ahora es 'false'.
                .andExpect(jsonPath("$._links.reactivate.href").exists()); // Verifica que ahora existe un enlace para reactivar.

        // VERIFY: Confirma la llamada al servicio de desactivación.
        Mockito.verify(rolService, Mockito.times(1)).desactivarRol(eq(1L));
    }

    /**
     * Prueba el intento de desactivar un rol que tiene usuarios activos vinculados.
     * Se espera un estado HTTP 409 (Conflict) para indicar que la operación no se puede realizar.
     */
    @Test
    void testDesactivarRol_Conflict_UsuariosVinculados() throws Exception {
        // ARRANGE: Se configura el mock para simular un conflicto de negocio.
        Mockito.when(rolService.desactivarRol(eq(1L)))
                .thenThrow(new IllegalStateException("No se puede desactivar el rol 'ADMIN' porque está vinculado a usuarios activos."));

        // ACT & ASSERT: Se realiza la petición y se verifica la respuesta 409.
        mockMvc.perform(delete("/roles/{id}", 1L))
                .andExpect(status().isConflict()) // Verifica estado 409.
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("No se puede desactivar el rol 'ADMIN' porque está vinculado a usuarios activos."));

        // VERIFY: Asegura que se intentó llamar al servicio.
        Mockito.verify(rolService, Mockito.times(1)).desactivarRol(eq(1L));
    }

    // --- PRUEBAS PARA reactivarRol (PUT /roles/{id}/reactivar) ---

    /**
     * Prueba la reactivación exitosa de un rol.
     * Se espera un estado HTTP 200 (OK) y que el estado del rol vuelva a ser 'true'.
     */
    @Test
    void testReactivarRol_Exito() throws Exception {
        // ARRANGE: Se crea un objeto que representa el rol reactivado.
        Rol rolReactivado = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        // Se configura el mock para que devuelva este objeto al reactivar el rol con ID 1.
        Mockito.when(rolService.reactivarRol(eq(1L))).thenReturn(rolReactivado);

        // ACT & ASSERT: Se realiza una petición PUT a la sub-ruta de reactivación.
        mockMvc.perform(put("/roles/{id}/reactivar", 1L))
                .andExpect(status().isOk()) // Verifica estado 200.
                .andExpect(jsonPath("$.estadoRol").value(true)) // Verifica que el estado del rol ahora es 'true'.
                .andExpect(jsonPath("$._links.deactivate.href").exists()); // Verifica que el enlace para desactivar está presente de nuevo.

        // VERIFY: Confirma la llamada al servicio de reactivación.
        Mockito.verify(rolService, Mockito.times(1)).reactivarRol(eq(1L));
    }

    // --- PRUEBAS PARA obtenerRolPorId (GET /roles/{id}) ---

    /**
     * Prueba la obtención exitosa de un rol por su ID.
     * Se espera un estado HTTP 200 (OK) y que los datos del rol coincidan.
     */
    @Test
    void testObtenerRolPorId_Exito() throws Exception {
        // ARRANGE: Se configura el mock para que devuelva un 'Optional' con el rol de admin
        // cuando se busque por el ID 1. Optional se usa para manejar casos donde el objeto podría no existir.
        Mockito.when(rolService.obtenerRolPorId(eq(1L))).thenReturn(Optional.of(rolAdmin));

        // ACT & ASSERT: Se realiza una petición GET.
        mockMvc.perform(get("/roles/{id}", 1L))
                .andExpect(status().isOk()) // Verifica estado 200.
                .andExpect(jsonPath("$.nombreRol").value(rolAdmin.getNombreRol())); // Verifica que se devolvió el rol correcto.

        // VERIFY: Confirma la llamada al servicio.
        Mockito.verify(rolService, Mockito.times(1)).obtenerRolPorId(eq(1L));
    }

    /**
     * Prueba el caso de buscar un rol con un ID que no existe.
     * Se espera un estado HTTP 404 (Not Found).
     */
    @Test
    void testObtenerRolPorId_NotFound() throws Exception {
        // ARRANGE: Se configura el mock para que devuelva un 'Optional' vacío,
        // simulando que no se encontró ningún rol con el ID 99.
        Mockito.when(rolService.obtenerRolPorId(eq(99L))).thenReturn(Optional.empty());

        // ACT & ASSERT: Se realiza la petición y se verifica la respuesta 404.
        mockMvc.perform(get("/roles/{id}", 99L))
                .andExpect(status().isNotFound()); // Verifica estado 404.

        // VERIFY: Confirma que se llamó al servicio con el ID inexistente.
        Mockito.verify(rolService, Mockito.times(1)).obtenerRolPorId(eq(99L));
    }

    // --- PRUEBAS PARA obtenerTodosRolesActivos (GET /roles/activos) ---

    /**
     * Prueba la obtención exitosa de una lista de todos los roles activos.
     * Se espera un estado 200 (OK) y una respuesta JSON que contenga una lista de roles.
     */
    @Test
    void testObtenerTodosRolesActivos_Exito() throws Exception {
        // ARRANGE: Se configura el mock para que devuelva una lista con los dos roles de prueba.
        Mockito.when(rolService.obtenerTodosRolesActivos()).thenReturn(Arrays.asList(rolAdmin, rolUsuario));

        // ACT & ASSERT: Se realiza una petición GET a la ruta de roles activos.
        mockMvc.perform(get("/roles/activos"))
                .andExpect(status().isOk()) // Verifica estado 200.
                // Verifica la estructura HATEOAS para colecciones.
                // Se accede a la lista a través de la clave "_embedded.rolList".
                .andExpect(jsonPath("$._embedded.rolList.length()").value(2)) // Verifica que la lista contenga 2 elementos.
                .andExpect(jsonPath("$._embedded.rolList[0].nombreRol").value("ADMIN")) // Verifica el primer elemento.
                .andExpect(jsonPath("$._embedded.rolList[1].nombreRol").value("USUARIO")); // Verifica el segundo elemento.

        // VERIFY: Confirma la llamada al servicio.
        Mockito.verify(rolService, Mockito.times(1)).obtenerTodosRolesActivos();
    }

    /**
     * Prueba la obtención de roles activos cuando no hay ninguno en el sistema.
     * Se espera un estado 200 (OK) y una respuesta que indique una colección vacía.
     */
    @Test
    void testObtenerTodosRolesActivos_ListaVacia() throws Exception {
        // ARRANGE: Se configura el mock para que devuelva una lista vacía.
        Mockito.when(rolService.obtenerTodosRolesActivos()).thenReturn(Collections.emptyList());

        // ACT & ASSERT: Se realiza la petición.
        mockMvc.perform(get("/roles/activos"))
                .andExpect(status().isOk()) // Verifica estado 200.
                // Verifica que la clave "_embedded" no exista, ya que la lista está vacía.
                .andExpect(jsonPath("$._embedded").doesNotExist())
                // Los enlaces de nivel superior de la colección deben seguir existiendo.
                .andExpect(jsonPath("$._links.self.href").exists());

        // VERIFY: Confirma la llamada al servicio.
        Mockito.verify(rolService, Mockito.times(1)).obtenerTodosRolesActivos();
    }
}