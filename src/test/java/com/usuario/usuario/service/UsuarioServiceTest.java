package com.usuario.usuario.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName; // Importar para nombres descriptivos
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.usuario.usuario.model.Permiso;
import com.usuario.usuario.model.Rol;
import com.usuario.usuario.model.Usuario;
import com.usuario.usuario.repository.RolRepository;
import com.usuario.usuario.repository.UsuarioRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para la lógica de negocio de UsuarioService.
 * Se utiliza Mockito para simular las dependencias (repositorios) y probar
 * el servicio de forma aislada.
 */
class UsuarioServiceTest {

    // Crea un 'mock' (objeto simulado) del Repositorio de Usuarios.
    // Esto nos permite controlar cómo se comporta la base de datos en las pruebas.
    @Mock
    private UsuarioRepository usuarioRepository;

    // Crea un 'mock' del Repositorio de Roles.
    @Mock
    private RolRepository rolRepository;

    // Inyecta los mocks creados (@Mock) dentro de esta instancia de UsuarioServiceImpl.
    // Así, cuando el servicio llame a los repositorios, estará usando nuestros mocks.
    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    // Este método se ejecuta antes de cada prueba (@Test).
    // Inicializa los mocks para asegurar que cada prueba comience en un estado limpio.
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ========== PRUEBAS PARA EL MÉTODO crearUsuario ==========

    /**
     * Prueba el "camino feliz": la creación de un usuario cuando todos los datos son válidos
     * y no existen conflictos (como un correo duplicado).
     */
    @Test
    @DisplayName("crearUsuario - Exito al crear un nuevo usuario")
    void testCrearUsuario_Exito() {
        // --- 1. Preparación (Arrange) ---
        // Se definen los datos de prueba: un rol y el usuario a crear.
        Rol rolAdmin = new Rol(1L, "ADMIN", Arrays.asList(Permiso.CREAR_USUARIO), true);
        Usuario nuevoUsuario = new Usuario(null, "Juan", "Perez", rolAdmin, "juan.perez@example.com", "pass123", 1, true);
        Usuario usuarioGuardado = new Usuario(1, "Juan", "Perez", rolAdmin, "juan.perez@example.com", "pass123", 1, true);

        // Se configura el comportamiento de los mocks:
        // - Simula que no hay ningún usuario activo con ese correo.
        when(usuarioRepository.findByCorreoUsuarioAndEstadoUsuario(anyString(), eq(true))).thenReturn(Optional.empty());
        // - Simula que el rol con ID 1 sí existe en la base de datos.
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolAdmin));
        // - Simula la acción de guardar, devolviendo el usuario ya con su ID asignado.
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // --- 2. Actuación (Act) ---
        // Se ejecuta el método del servicio que se quiere probar.
        Usuario resultado = usuarioService.crearUsuario(nuevoUsuario);

        // --- 3. Aserción (Assert) ---
        // Se verifica que el resultado es el esperado.
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdUsuario()).isEqualTo(1);
        assertThat(resultado.getRol().getNombreRol()).isEqualTo("ADMIN");
        assertThat(resultado.getEstadoUsuario()).isTrue(); // Asegura que se establece a true por defecto

        // Se verifica que los métodos de los mocks fueron llamados como se esperaba.
        verify(usuarioRepository).findByCorreoUsuarioAndEstadoUsuario("juan.perez@example.com", true);
        verify(rolRepository).findById(1L);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    /**
     * Prueba que el sistema lanza una excepción cuando se intenta crear un usuario
     * con un correo electrónico que ya está siendo utilizado por otro usuario activo.
     */
    @Test
    @DisplayName("crearUsuario - Falla si el correo ya está en uso por un usuario activo")
    void testCrearUsuario_CorreoYaEnUso() {
        // Preparación: Datos de prueba y configuración de mocks para simular un correo duplicado.
        Rol rolAdmin = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Usuario usuarioExistente = new Usuario(1, "Ana", "Gomez", rolAdmin, "ana.gomez@example.com", "pass456", 1, true);
        Usuario nuevoUsuarioConCorreoDuplicado = new Usuario(null, "Pedro", "Lopez", rolAdmin, "ana.gomez@example.com", "newpass", 1, true);

        // Simula que el rol existe para pasar esa validación.
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolAdmin));
        // Simula que SÍ se encuentra un usuario activo con ese correo.
        when(usuarioRepository.findByCorreoUsuarioAndEstadoUsuario(anyString(), eq(true))).thenReturn(Optional.of(usuarioExistente));

        // Actuación y Aserción: Se verifica que al llamar al método, se lanza la excepción esperada.
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario(nuevoUsuarioConCorreoDuplicado);
        });

        // Se verifica que el mensaje de la excepción es el correcto.
        assertThat(thrown.getMessage()).contains("El correo electrónico 'ana.gomez@example.com' ya está en uso por un usuario activo.");

        // Se verifica que el método 'save' NUNCA fue llamado.
        verify(usuarioRepository).findByCorreoUsuarioAndEstadoUsuario("ana.gomez@example.com", true);
        verify(rolRepository).findById(1L); // Se verifica que intentó buscar el rol
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    /**
     * Prueba que el sistema lanza una excepción si se intenta crear un usuario
     * asignándole un ID de rol que no existe en la base de datos.
     */
    @Test
    @DisplayName("crearUsuario - Falla si el rol no es encontrado")
    void testCrearUsuario_RolNoEncontrado() {
        // Preparación: Se crea un usuario con un rol que sabemos que no existirá.
        Rol rolInexistente = new Rol(99L, "INEXISTENTE", Collections.emptyList(), true);
        Usuario nuevoUsuario = new Usuario(null, "Luis", "Ruiz", rolInexistente, "luis.ruiz@example.com", "pass789", 1, true);

        // Simula que al buscar el rol por ID, no se encuentra nada.
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        // Actuación y Aserción: Se verifica que se lanza la excepción de "Entidad no encontrada".
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.crearUsuario(nuevoUsuario);
        });

        assertThat(thrown.getMessage()).contains("Rol no encontrado con id: 99");
        verify(rolRepository).findById(99L);
        verify(usuarioRepository, never()).findByCorreoUsuarioAndEstadoUsuario(anyString(), anyBoolean()); // No debería llegar a esta verificación
        verify(usuarioRepository, never()).save(any(Usuario.class)); // No se debe intentar guardar.
    }

    /**
     * Prueba la validación de que un usuario no puede ser creado si no se le asigna ningún rol (rol es nulo).
     */
    @Test
    @DisplayName("crearUsuario - Falla si el rol es nulo")
    void testCrearUsuario_RolEsNulo() {
        // Preparación: Usuario sin rol.
        Usuario nuevoUsuario = new Usuario(null, "Sara", "Diaz", null, "sara.diaz@example.com", "passABC", 1, true);

        // Actuación y Aserción: Se espera una excepción de argumento ilegal.
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario(nuevoUsuario);
        });

        assertThat(thrown.getMessage()).contains("El rol es obligatorio para crear un usuario.");
        // Se verifica que no hubo ninguna interacción con los repositorios, ya que la validación ocurre antes.
        verifyNoInteractions(usuarioRepository, rolRepository);
    }

    /**
     * Prueba la validación de que un usuario no puede ser creado si el rol tiene un ID nulo.
     */
    @Test
    @DisplayName("crearUsuario - Falla si el ID del rol es nulo")
    void testCrearUsuario_RolIdEsNulo() {
        // Preparación: Usuario con un objeto Rol, pero con su ID nulo.
        Rol rolConIdNulo = new Rol();
        rolConIdNulo.setNombreRol("TEMP");
        Usuario nuevoUsuario = new Usuario(null, "Sara", "Diaz", rolConIdNulo, "sara.diaz2@example.com", "passABC", 1, true);

        // Actuación y Aserción: Se espera una excepción de argumento ilegal.
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario(nuevoUsuario);
        });

        assertThat(thrown.getMessage()).contains("El rol es obligatorio para crear un usuario.");
        // Se verifica que no hubo ninguna interacción con los repositorios, ya que la validación ocurre antes.
        verifyNoInteractions(usuarioRepository, rolRepository);
    }


    // ========== PRUEBAS PARA EL MÉTODO actualizarUsuario ==========

    /**
     * Prueba la actualización exitosa de un usuario, modificando varios de sus campos.
     */
    @Test
    @DisplayName("actualizarUsuario - Exito al actualizar todos los campos")
    void testActualizarUsuario_Exito() {
        // Preparación: Se definen el usuario original, el rol original, el rol nuevo y los datos de la actualización.
        Rol rolOriginal = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Rol rolNuevo = new Rol(2L, "USUARIO", Collections.emptyList(), true);
        Usuario existingUsuario = new Usuario(1, "Original", "Usuario", rolOriginal, "original@example.com", "pass1", 1, true);
        // Detalles de actualización con todos los campos posibles a cambiar
        Usuario usuarioDetails = new Usuario(null, "ActualizadoNombre", "ActualizadoApellido", rolNuevo, "actualizado@example.com", "newpassHash", 2, false);

        // Configuración de mocks:
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existingUsuario)); // El usuario a actualizar existe.
        when(usuarioRepository.findByCorreoUsuarioAndEstadoUsuario(eq("actualizado@example.com"), eq(true))).thenReturn(Optional.empty()); // El nuevo correo está disponible.
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolNuevo)); // El nuevo rol existe.
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Devuelve el objeto modificado.

        // Actuación: Se ejecuta el método de actualización.
        Usuario resultado = usuarioService.actualizarUsuario(1, usuarioDetails);

        // Aserción: Se verifica que todos los campos se actualizaron correctamente.
        assertThat(resultado.getNomUsuario()).isEqualTo("ActualizadoNombre");
        assertThat(resultado.getApUsuario()).isEqualTo("ActualizadoApellido");
        assertThat(resultado.getCorreoUsuario()).isEqualTo("actualizado@example.com");
        assertThat(resultado.getRol().getNombreRol()).isEqualTo("USUARIO");
        assertThat(resultado.getPassUsuario()).isEqualTo("newpassHash");
        assertThat(resultado.getIdTienda()).isEqualTo(2);
        assertThat(resultado.getEstadoUsuario()).isFalse();

        // Verificar interacciones
        verify(usuarioRepository).findById(1);
        verify(usuarioRepository).findByCorreoUsuarioAndEstadoUsuario("actualizado@example.com", true);
        verify(rolRepository).findById(2L);
        verify(usuarioRepository).save(existingUsuario); // Se guarda el existingUsuario modificado
    }

    /**
     * Prueba la actualización de un usuario sin cambiar el correo electrónico.
     */
    @Test
    @DisplayName("actualizarUsuario - Exito al actualizar sin cambiar correo")
    void testActualizarUsuario_SinCambiarCorreo() {
        Rol rolOriginal = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Usuario existingUsuario = new Usuario(1, "Original", "Usuario", rolOriginal, "original@example.com", "pass1", 1, true);
        Usuario usuarioDetails = new Usuario(null, "SoloNombre", null, null, "original@example.com", null, null, null); // Mismo correo

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existingUsuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.actualizarUsuario(1, usuarioDetails);

        assertThat(resultado.getNomUsuario()).isEqualTo("SoloNombre");
        assertThat(resultado.getCorreoUsuario()).isEqualTo("original@example.com"); // El correo no debería haber cambiado
        verify(usuarioRepository, never()).findByCorreoUsuarioAndEstadoUsuario(anyString(), anyBoolean()); // No se debe verificar el correo si no cambia
        verify(usuarioRepository).save(existingUsuario);
    }

    /**
     * Prueba la actualización de un usuario, cambiando el correo a uno que ya está en uso
     * por el MISMO usuario (no debería lanzar excepción).
     */
    @Test
    @DisplayName("actualizarUsuario - Exito al cambiar correo a sí mismo (no debería lanzar error)")
    void testActualizarUsuario_CambiarCorreoAMismoUsuario() {
        Rol rolOriginal = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Usuario existingUsuario = new Usuario(1, "Original", "Usuario", rolOriginal, "original@example.com", "pass1", 1, true);
        Usuario usuarioDetails = new Usuario(null, "Updated", null, null, "original@example.com", null, null, null); // Mismo correo que el existente

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existingUsuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.actualizarUsuario(1, usuarioDetails);

        assertThat(resultado.getNomUsuario()).isEqualTo("Updated");
        assertThat(resultado.getCorreoUsuario()).isEqualTo("original@example.com");
        // No debería haber llamado a findByCorreoUsuarioAndEstadoUsuario porque el correo no cambió realmente para efectos de unicidad.
        verify(usuarioRepository, never()).findByCorreoUsuarioAndEstadoUsuario(anyString(), anyBoolean());
        verify(usuarioRepository).save(existingUsuario);
    }


    /**
     * Prueba que se lanza una excepción al intentar actualizar un usuario con un ID que no existe.
     */
    @Test
    @DisplayName("actualizarUsuario - Falla si el usuario no es encontrado")
    void testActualizarUsuario_UsuarioNoEncontrado() {
        Usuario usuarioDetails = new Usuario();
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty()); // Simula que el usuario no se encuentra.

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.actualizarUsuario(99, usuarioDetails);
        });

        assertThat(thrown.getMessage()).contains("Usuario no encontrado con id: 99");
        verify(usuarioRepository).findById(99);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    /**
     * Prueba que se lanza una excepción al intentar cambiar el correo de un usuario
     * a uno que ya está siendo utilizado por OTRO usuario activo.
     */
    @Test
    @DisplayName("actualizarUsuario - Falla si el correo está duplicado por otro usuario activo")
    void testActualizarUsuario_CorreoDuplicadoParaOtroUsuarioActivo() {
        // Preparación: Se necesita un usuario a actualizar y otro usuario que ya posee el correo deseado.
        Rol rol = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Usuario existingUsuario = new Usuario(1, "Original", "Usuario", rol, "original@example.com", "pass1", 1, true);
        Usuario otroUsuarioConCorreo = new Usuario(2, "Otro", "Usuario", rol, "otro@example.com", "pass2", 1, true);
        Usuario usuarioDetails = new Usuario(null, null, null, null, "otro@example.com", null, null, null); // Intenta usar el correo del otro.

        // Configuración de mocks:
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existingUsuario));
        // Simula que al buscar el nuevo correo, se encuentra que pertenece a otro usuario.
        when(usuarioRepository.findByCorreoUsuarioAndEstadoUsuario(eq("otro@example.com"), eq(true))).thenReturn(Optional.of(otroUsuarioConCorreo));

        // Actuación y Aserción.
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.actualizarUsuario(1, usuarioDetails);
        });

        assertThat(thrown.getMessage()).contains("El correo electrónico 'otro@example.com' ya está en uso por otro usuario activo.");
        verify(usuarioRepository).findById(1);
        verify(usuarioRepository).findByCorreoUsuarioAndEstadoUsuario("otro@example.com", true);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    /**
     * Prueba que se lanza una excepción si el rol proporcionado para la actualización no existe.
     */
    @Test
    @DisplayName("actualizarUsuario - Falla si el nuevo rol no es encontrado")
    void testActualizarUsuario_NuevoRolNoEncontrado() {
        Rol rolOriginal = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Usuario existingUsuario = new Usuario(1, "Original", "Usuario", rolOriginal, "original@example.com", "pass1", 1, true);
        Rol rolInexistente = new Rol(99L, "INEXISTENTE", Collections.emptyList(), true);
        Usuario usuarioDetails = new Usuario(null, null, null, rolInexistente, null, null, null, null);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existingUsuario));
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.actualizarUsuario(1, usuarioDetails);
        });

        assertThat(thrown.getMessage()).contains("Rol no encontrado con id: 99");
        verify(usuarioRepository).findById(1);
        verify(rolRepository).findById(99L);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ========== PRUEBAS PARA LOS MÉTODOS desactivarUsuario y reactivarUsuario ==========

    /**
     * Prueba la desactivación (borrado lógico) exitosa de un usuario.
     */
    @Test
    @DisplayName("desactivarUsuario - Exito al desactivar un usuario activo")
    void testDesactivarUsuario_Exito() {
        Usuario usuarioActivo = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, true);
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuarioActivo));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Devuelve el mismo objeto que se le pasa

        Usuario resultado = usuarioService.desactivarUsuario(1);

        assertThat(resultado.getEstadoUsuario()).isFalse(); // El estado debe ser ahora 'false'.
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(usuarioRepository).save(usuarioActivo);
    }

    /**
     * Prueba que se lanza una excepción si se intenta desactivar un usuario que no existe o ya está inactivo.
     */
    @Test
    @DisplayName("desactivarUsuario - Falla si el usuario activo no es encontrado")
    void testDesactivarUsuario_UsuarioActivoNoEncontrado() {
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.desactivarUsuario(1);
        });

        assertThat(thrown.getMessage()).contains("Usuario activo no encontrado con id: 1");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
    
    /**
     * Prueba la reactivación exitosa de un usuario que estaba inactivo.
     */
    @Test
    @DisplayName("reactivarUsuario - Exito al reactivar un usuario inactivo")
    void testReactivarUsuario_Exito() {
        Usuario usuarioInactivo = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, false);
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, false)).thenReturn(Optional.of(usuarioInactivo));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.reactivarUsuario(1);

        assertThat(resultado.getEstadoUsuario()).isTrue(); // El estado debe ser ahora 'true'.
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, false);
        verify(usuarioRepository).save(usuarioInactivo);
    }

    /**
     * Prueba que se lanza una excepción si se intenta reactivar un usuario que no se encuentra inactivo.
     */
    @Test
    @DisplayName("reactivarUsuario - Falla si el usuario inactivo no es encontrado")
    void testReactivarUsuario_UsuarioInactivoNoEncontrado() {
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, false)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.reactivarUsuario(1);
        });

        assertThat(thrown.getMessage()).contains("Usuario inactivo no encontrado con id: 1");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, false);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ========== PRUEBAS PARA LOS MÉTODOS DE CONSULTA (obtener/listar) ==========

    /**
     * Prueba que se puede obtener un usuario activo por su ID.
     */
    @Test
    @DisplayName("obtenerUsuarioPorId - Retorna usuario activo existente")
    void testObtenerUsuarioPorId_ActivoExistente() {
        Usuario usuario = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, true);
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.obtenerUsuarioPorId(1);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getIdUsuario()).isEqualTo(1);
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }

    /**
     * Prueba que no se obtiene ningún usuario si el ID no corresponde a un usuario activo.
     */
    @Test
    @DisplayName("obtenerUsuarioPorId - Retorna vacío si usuario no existe o está inactivo")
    void testObtenerUsuarioPorId_NoExistenteOInactivo() {
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.obtenerUsuarioPorId(1);

        assertThat(resultado).isNotPresent();
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }
    
    /**
     * Prueba que se obtiene una lista de todos los usuarios activos.
     */
    @Test
    @DisplayName("obtenerTodosUsuariosActivos - Retorna lista de usuarios activos")
    void testObtenerTodosUsuariosActivos_Exito() {
        Usuario u1 = new Usuario(1, "Juan", "Activo", null, "j@e.com", "p", 1, true);
        Usuario u2 = new Usuario(2, "Ana", "Activa", null, "a@e.com", "p", 1, true);
        when(usuarioRepository.findByEstadoUsuario(true)).thenReturn(Arrays.asList(u1, u2));

        List<Usuario> resultado = usuarioService.obtenerTodosUsuariosActivos();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getEstadoUsuario()).isTrue();
        assertThat(resultado.get(1).getEstadoUsuario()).isTrue();
        verify(usuarioRepository).findByEstadoUsuario(true);
    }

    /**
     * Prueba que se obtiene una lista vacía si no hay usuarios activos.
     */
    @Test
    @DisplayName("obtenerTodosUsuariosActivos - Retorna lista vacía si no hay activos")
    void testObtenerTodosUsuariosActivos_ListaVacia() {
        when(usuarioRepository.findByEstadoUsuario(true)).thenReturn(Collections.emptyList());

        List<Usuario> resultado = usuarioService.obtenerTodosUsuariosActivos();

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findByEstadoUsuario(true);
    }

    /**
     * Prueba que se obtiene una lista de todos los usuarios (activos e inactivos).
     */
    @Test
    @DisplayName("obtenerTodosUsuarios - Retorna lista de todos los usuarios (activos e inactivos)")
    void testObtenerTodosUsuarios() {
        Usuario u1 = new Usuario(1, "Juan", "Activo", null, "j@e.com", "p", 1, true);
        Usuario u2 = new Usuario(2, "Ana", "Inactivo", null, "a@e.com", "p", 1, false);
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<Usuario> resultado = usuarioService.obtenerTodosUsuarios();

        assertThat(resultado).hasSize(2);
        verify(usuarioRepository).findAll();
    }
    
    /**
     * Prueba que se obtiene una lista vacía si no hay usuarios en el sistema.
     */
    @Test
    @DisplayName("obtenerTodosUsuarios - Retorna lista vacía si no hay usuarios")
    void testObtenerTodosUsuarios_ListaVacia() {
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

        List<Usuario> resultado = usuarioService.obtenerTodosUsuarios();

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findAll();
    }

    // ========== PRUEBAS PARA LA LÓGICA DE PERMISOS Y ROLES ==========

    /**
     * Prueba que se obtienen correctamente los permisos asociados al rol de un usuario.
     */
    @Test
    @DisplayName("obtenerPermisosUsuario - Exito al obtener permisos de un usuario con rol y permisos")
    void testObtenerPermisosUsuario_Exito() {
        Permiso permiso1 = Permiso.CREAR_USUARIO;
        Permiso permiso2 = Permiso.VER_USUARIO;
        Rol rolAdmin = new Rol(1L, "ADMIN", Arrays.asList(permiso1, permiso2), true);
        Usuario usuario = new Usuario(1, "Juan", "Perez", rolAdmin, "juan@example.com", "pass", 1, true);

        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuario));

        List<Permiso> resultado = usuarioService.obtenerPermisosUsuario(1);

        assertThat(resultado).hasSize(2).containsExactlyInAnyOrder(permiso1, permiso2);
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }

    /**
     * Prueba que se lanza una excepción si se intenta obtener permisos de un usuario que no existe o está inactivo.
     */
    @Test
    @DisplayName("obtenerPermisosUsuario - Falla si el usuario no es encontrado o está inactivo")
    void testObtenerPermisosUsuario_UsuarioNoEncontrado() {
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.obtenerPermisosUsuario(1);
        });

        assertThat(thrown.getMessage()).contains("Usuario activo no encontrado con id: 1");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }
    
    /**
     * Prueba que el método de obtener permisos devuelve una lista vacía si el usuario tiene un rol pero este no tiene permisos.
     */
    @Test
    @DisplayName("obtenerPermisosUsuario - Retorna lista vacía si el rol no tiene permisos (lista vacía)")
    void testObtenerPermisosUsuario_RolConListaPermisosVacia() {
        Rol rolSinPermisos = new Rol(1L, "VISITANTE", Collections.emptyList(), true); // La lista de permisos está vacía
        Usuario usuario = new Usuario(1, "Juan", "Perez", rolSinPermisos, "juan@example.com", "pass", 1, true);
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuario));

        List<Permiso> resultado = usuarioService.obtenerPermisosUsuario(1);

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }

    /**
     * Prueba que el método de obtener permisos devuelve una lista vacía si el rol del usuario es nulo.
     */
    @Test
    @DisplayName("obtenerPermisosUsuario - Retorna lista vacía si el usuario no tiene rol")
    void testObtenerPermisosUsuario_UsuarioSinRol() {
        Usuario usuarioSinRol = new Usuario(1, "Pedro", "Gomez", null, "pedro@example.com", "pass", 1, true); // Rol nulo
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuarioSinRol));

        List<Permiso> resultado = usuarioService.obtenerPermisosUsuario(1);

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }


    /**
     * Prueba la asignación exitosa de un nuevo rol a un usuario existente.
     */
    @Test
    @DisplayName("asignarRol - Exito al asignar un rol a un usuario")
    void testAsignarRol_Exito() {
        Rol rolAntiguo = new Rol(1L, "ANTIGUO", Collections.emptyList(), true);
        Rol nuevoRol = new Rol(2L, "NUEVO", Collections.emptyList(), true);
        Usuario usuario = new Usuario(1, "Juan", "Perez", rolAntiguo, "juan@example.com", "pass", 1, true);
        
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(nuevoRol));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.asignarRol(1, 2L);

        assertThat(resultado.getRol().getNombreRol()).isEqualTo("NUEVO");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(rolRepository).findById(2L);
        verify(usuarioRepository).save(usuario);
    }

    /**
     * Prueba que se lanza una excepción si el usuario al que se intenta asignar un rol no existe o está inactivo.
     */
    @Test
    @DisplayName("asignarRol - Falla si el usuario no es encontrado o está inactivo")
    void testAsignarRol_UsuarioNoEncontrado() {
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.asignarRol(1, 2L);
        });

        assertThat(thrown.getMessage()).contains("Usuario activo no encontrado con id: 1");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(rolRepository, never()).findById(anyLong()); // No debería intentar buscar el rol
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    /**
     * Prueba que se lanza una excepción si el rol que se intenta asignar no existe.
     */
    @Test
    @DisplayName("asignarRol - Falla si el rol a asignar no es encontrado")
    void testAsignarRol_RolNoEncontrado() {
        Usuario usuario = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, true);
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.asignarRol(1, 99L);
        });

        assertThat(thrown.getMessage()).contains("Rol no encontrado con id: 99");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(rolRepository).findById(99L);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // --- NUEVOS TESTS PARA quitarRol ---
    /**
     * Prueba que se puede quitar el rol de un usuario exitosamente.
     */
    @Test
    @DisplayName("quitarRol - Exito al quitar el rol de un usuario")
    void testQuitarRol_Exito() {
        Rol rolAdmin = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Usuario usuarioConRol = new Usuario(1, "Juan", "Perez", rolAdmin, "juan@example.com", "pass", 1, true);
        Usuario usuarioSinRol = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, true); // Expected result

        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuarioConRol));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.quitarRol(1);

        assertThat(resultado.getRol()).isNull();
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(usuarioRepository).save(usuarioConRol); // Verifica que se guardó el objeto modificado
    }

    /**
     * Prueba que se lanza una excepción si se intenta quitar el rol de un usuario que no existe o está inactivo.
     */
    @Test
    @DisplayName("quitarRol - Falla si el usuario activo no es encontrado")
    void testQuitarRol_UsuarioActivoNoEncontrado() {
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.quitarRol(1);
        });

        assertThat(thrown.getMessage()).contains("Usuario activo no encontrado con id: 1");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }


    // ========== PRUEBAS PARA MÉTODOS DE CONSULTA ESPECÍFICOS ==========
    
    /**
     * Prueba que se puede listar correctamente a todos los usuarios activos que pertenecen a una tienda específica.
     */
    @Test
    @DisplayName("listarUsuariosPorTienda - Exito al listar usuarios activos por tienda")
    void testListarUsuariosPorTienda_Exito() {
        Usuario u1 = new Usuario(1, "Juan", "Perez", null, "j@e.com", "p", 101, true);
        Usuario u2 = new Usuario(2, "Ana", "Gomez", null, "a@e.com", "p", 101, true);
        when(usuarioRepository.findByIdTiendaAndEstadoUsuario(101, true)).thenReturn(Arrays.asList(u1, u2)); // Changed to int

        List<Usuario> resultado = usuarioService.listarUsuariosPorTienda(101);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getIdTienda()).isEqualTo(101);
        assertThat(resultado.get(1).getIdTienda()).isEqualTo(101);
        assertThat(resultado.get(0).getEstadoUsuario()).isTrue();
        assertThat(resultado.get(1).getEstadoUsuario()).isTrue();
        verify(usuarioRepository).findByIdTiendaAndEstadoUsuario(101, true);
    }

    /**
     * Prueba que el método de listar por tienda devuelve una lista vacía si no hay usuarios en esa tienda o activos.
     */
    @Test
    @DisplayName("listarUsuariosPorTienda - Retorna lista vacía si no hay usuarios activos en la tienda")
    void testListarUsuariosPorTienda_Vacio() {
        when(usuarioRepository.findByIdTiendaAndEstadoUsuario(102, true)).thenReturn(Collections.emptyList()); // Changed to int

        List<Usuario> resultado = usuarioService.listarUsuariosPorTienda(102);

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findByIdTiendaAndEstadoUsuario(102, true);
    }
}