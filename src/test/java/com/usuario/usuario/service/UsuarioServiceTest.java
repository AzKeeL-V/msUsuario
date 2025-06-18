package com.usuario.usuario.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService; // Usamos UsuarioServiceImpl para probar la implementación concreta

    // Constructor o método para inicializar los mocks
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- PRUEBAS PARA crearUsuario ---

    @Test
    void testCrearUsuario_Exito() {
        // Datos de prueba
        Rol rolAdmin = new Rol(1L, "ADMIN", Arrays.asList(Permiso.CREAR_USUARIO), true);
        Usuario nuevoUsuario = new Usuario(null, "Juan", "Perez", rolAdmin, "juan.perez@example.com", "pass123", 1, true);
        Usuario usuarioGuardado = new Usuario(1, "Juan", "Perez", rolAdmin, "juan.perez@example.com", "pass123", 1, true);

        // Comportamiento esperado de los mocks
        when(usuarioRepository.findByCorreoUsuarioAndEstadoUsuario(anyString(), eq(true))).thenReturn(Optional.empty()); // No existe usuario activo con ese correo
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolAdmin)); // El rol existe
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado); // Se guarda y devuelve el usuario con ID

        // Ejecutar el método del servicio
        Usuario resultado = usuarioService.crearUsuario(nuevoUsuario);

        // Verificar aserciones
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdUsuario()).isEqualTo(1);
        assertThat(resultado.getCorreoUsuario()).isEqualTo("juan.perez@example.com");
        assertThat(resultado.getEstadoUsuario()).isTrue(); // Debe ser activo por defecto
        assertThat(resultado.getRol().getNombreRol()).isEqualTo("ADMIN");

        // Verificar interacciones con los mocks
        verify(usuarioRepository).findByCorreoUsuarioAndEstadoUsuario("juan.perez@example.com", true);
        verify(rolRepository).findById(1L);
        verify(usuarioRepository).save(any(Usuario.class)); // Verificamos que save fue llamado con cualquier instancia de Usuario
    }

    @Test
    void testCrearUsuario_CorreoYaEnUso() {
        // Datos de prueba
        Rol rolAdmin = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Usuario usuarioExistente = new Usuario(1, "Ana", "Gomez", rolAdmin, "ana.gomez@example.com", "pass456", 1, true);
        Usuario nuevoUsuarioConCorreoDuplicado = new Usuario(null, "Pedro", "Lopez", rolAdmin, "ana.gomez@example.com", "newpass", 1, true);

        // Comportamiento esperado de los mocks
        // AHORA MOCKEAMOS EL ROL PARA QUE EXISTA, YA QUE EL SERVICIO LO BUSCA PRIMERO
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolAdmin)); // Simula que el rol con ID 1 existe

        when(usuarioRepository.findByCorreoUsuarioAndEstadoUsuario(anyString(), eq(true))).thenReturn(Optional.of(usuarioExistente)); // Ya existe un usuario activo con ese correo

        // Ejecutar el método del servicio y esperar la excepción
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario(nuevoUsuarioConCorreoDuplicado);
        });

        // Verificar el mensaje de la excepción
        assertThat(thrown.getMessage()).contains("El correo electrónico 'ana.gomez@example.com' ya está en uso por un usuario activo.");

        // Verificar interacciones con los mocks (save no debería llamarse)
        verify(usuarioRepository).findByCorreoUsuarioAndEstadoUsuario("ana.gomez@example.com", true);
        // CAMBIO AQUÍ: Ahora se espera que se llame a rolRepository.findById
        verify(rolRepository).findById(1L);
        verify(usuarioRepository, never()).save(any(Usuario.class)); // El save no debe ser llamado
    }

    @Test
    void testCrearUsuario_RolNoEncontrado() {
        // Datos de prueba
        Rol rolInexistente = new Rol(99L, "INEXISTENTE", Collections.emptyList(), true); // Un rol con un ID que no existe
        Usuario nuevoUsuario = new Usuario(null, "Luis", "Ruiz", rolInexistente, "luis.ruiz@example.com", "pass789", 1, true);

        // Comportamiento esperado de los mocks
        // No necesitamos mockear usuarioRepository.findByCorreoUsuarioAndEstadoUsuario aquí porque
        // el servicio lanzará EntityNotFoundException antes de llegar a esa línea,
        // dado que el rol no se encuentra primero.
        when(rolRepository.findById(99L)).thenReturn(Optional.empty()); // El rol NO existe

        // Ejecutar el método del servicio y esperar la excepción
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.crearUsuario(nuevoUsuario);
        });

        // Verificar el mensaje de la excepción
        assertThat(thrown.getMessage()).contains("Rol no encontrado con id: 99");

        // Verificar interacciones
        // CAMBIO AQUÍ: Eliminamos la verificación de findByCorreoUsuarioAndEstadoUsuario
        // porque ya no se espera que se llame si el rol no se encuentra primero.
        verify(usuarioRepository, never()).findByCorreoUsuarioAndEstadoUsuario(anyString(), eq(true));
        verify(rolRepository).findById(99L);
        verify(usuarioRepository, never()).save(any(Usuario.class)); // No se debe guardar
    }

    @Test
    void testCrearUsuario_RolEsNulo() {
        Usuario nuevoUsuario = new Usuario(null, "Sara", "Diaz", null, "sara.diaz@example.com", "passABC", 1, true);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.crearUsuario(nuevoUsuario);
        });

        assertThat(thrown.getMessage()).contains("El rol es obligatorio para crear un usuario.");
        verifyNoInteractions(usuarioRepository); // No debería interactuar con repositorios
        verifyNoInteractions(rolRepository);
    }

    // --- PRUEBAS PARA actualizarUsuario ---

    @Test
    void testActualizarUsuario_Exito() {
        // Datos de prueba
        Rol rolOriginal = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Rol rolNuevo = new Rol(2L, "USUARIO", Collections.emptyList(), true);
        Usuario existingUsuario = new Usuario(1, "Original", "Usuario", rolOriginal, "original@example.com", "pass1", 1, true);
        Usuario usuarioDetails = new Usuario(null, "Actualizado", "Apellido", rolNuevo, "actualizado@example.com", "newpass", 2, false); // Estado también se puede actualizar

        // Comportamiento esperado de los mocks
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existingUsuario)); // Usuario existente encontrado
        when(usuarioRepository.findByCorreoUsuarioAndEstadoUsuario(eq("actualizado@example.com"), eq(true))).thenReturn(Optional.empty()); // Nuevo correo disponible
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolNuevo)); // Nuevo rol encontrado
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioDetails); // Simular guardado

        // Ejecutar el método del servicio
        Usuario resultado = usuarioService.actualizarUsuario(1, usuarioDetails);

        // Verificar aserciones
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNomUsuario()).isEqualTo("Actualizado");
        assertThat(resultado.getCorreoUsuario()).isEqualTo("actualizado@example.com");
        assertThat(resultado.getRol().getNombreRol()).isEqualTo("USUARIO");
        assertThat(resultado.getPassUsuario()).isEqualTo("newpass");
        assertThat(resultado.getIdTienda()).isEqualTo(2);
        assertThat(resultado.getEstadoUsuario()).isFalse(); // El estado se actualizó a false

        // Verificar interacciones
        verify(usuarioRepository).findById(1);
        verify(usuarioRepository).findByCorreoUsuarioAndEstadoUsuario("actualizado@example.com", true);
        verify(rolRepository).findById(2L);
        verify(usuarioRepository).save(existingUsuario); // save fue llamado con el objeto existingUsuario modificado
    }

    @Test
    void testActualizarUsuario_UsuarioNoEncontrado() {
        Usuario usuarioDetails = new Usuario(null, "Actualizado", "Apellido", null, null, null, null, null);

        when(usuarioRepository.findById(99)).thenReturn(Optional.empty()); // Usuario no existe

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.actualizarUsuario(99, usuarioDetails);
        });

        assertThat(thrown.getMessage()).contains("Usuario no encontrado con id: 99");
        verify(usuarioRepository).findById(99);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testActualizarUsuario_CorreoDuplicadoParaOtroUsuarioActivo() {
        Rol rol = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Usuario existingUsuario = new Usuario(1, "Original", "Usuario", rol, "original@example.com", "pass1", 1, true);
        Usuario otroUsuarioConCorreo = new Usuario(2, "Otro", "Usuario", rol, "otro@example.com", "pass2", 1, true);
        Usuario usuarioDetails = new Usuario(null, null, null, null, "otro@example.com", null, null, null); // Intenta cambiar a un correo ya en uso

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existingUsuario));
        when(usuarioRepository.findByCorreoUsuarioAndEstadoUsuario(eq("otro@example.com"), eq(true))).thenReturn(Optional.of(otroUsuarioConCorreo)); // Otro usuario ya usa el correo

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.actualizarUsuario(1, usuarioDetails);
        });

        assertThat(thrown.getMessage()).contains("El correo electrónico 'otro@example.com' ya está en uso por otro usuario activo.");
        verify(usuarioRepository).findById(1);
        verify(usuarioRepository).findByCorreoUsuarioAndEstadoUsuario("otro@example.com", true);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testActualizarUsuario_CambioDeRolNoEncontrado() {
        Rol rolOriginal = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Rol rolInexistente = new Rol(99L, "INEXISTENTE", Collections.emptyList(), true);
        Usuario existingUsuario = new Usuario(1, "Original", "Usuario", rolOriginal, "original@example.com", "pass1", 1, true);
        Usuario usuarioDetails = new Usuario(null, null, null, rolInexistente, null, null, null, null);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existingUsuario));
        when(rolRepository.findById(99L)).thenReturn(Optional.empty()); // El rol no existe

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.actualizarUsuario(1, usuarioDetails);
        });

        assertThat(thrown.getMessage()).contains("Rol no encontrado con id: 99");
        verify(usuarioRepository).findById(1);
        verify(rolRepository).findById(99L);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // --- PRUEBAS PARA desactivarUsuario ---

    @Test
    void testDesactivarUsuario_Exito() {
        Usuario usuarioActivo = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, true);
        Usuario usuarioInactivo = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, false);

        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuarioActivo));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioInactivo);

        Usuario resultado = usuarioService.desactivarUsuario(1);

        assertThat(resultado.getEstadoUsuario()).isFalse();
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(usuarioRepository).save(usuarioActivo); // Se guarda el usuario modificado
    }

    @Test
    void testDesactivarUsuario_UsuarioActivoNoEncontrado() {
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.desactivarUsuario(1);
        });

        assertThat(thrown.getMessage()).contains("Usuario activo no encontrado con id: 1");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // PRUEBAS PARA reactivar un usuario

    @Test
    void testReactivarUsuario_Exito() {
        Usuario usuarioInactivo = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, false);
        Usuario usuarioActivo = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, true);

        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, false)).thenReturn(Optional.of(usuarioInactivo));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActivo);

        Usuario resultado = usuarioService.reactivarUsuario(1);

        assertThat(resultado.getEstadoUsuario()).isTrue();
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, false);
        verify(usuarioRepository).save(usuarioInactivo); // Se guarda el usuario modificado
    }

    @Test
    void testReactivarUsuario_UsuarioInactivoNoEncontrado() {
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, false)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.reactivarUsuario(1);
        });

        assertThat(thrown.getMessage()).contains("Usuario inactivo no encontrado con id: 1");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, false);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // --- Pruebas para obtenerUsuarioPorId ---

    @Test
    void testObtenerUsuarioPorId_ActivoExistente() {
        Usuario usuario = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, true);
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.obtenerUsuarioPorId(1);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getIdUsuario()).isEqualTo(1);
        assertThat(resultado.get().getEstadoUsuario()).isTrue();
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }

    @Test
    void testObtenerUsuarioPorId_NoExistenteOInactivo() {
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.obtenerUsuarioPorId(1);

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }

    // Pruebas para obtener todos los usuarios 

    @Test
    void testObtenerTodosUsuariosActivos() {
        Usuario u1 = new Usuario(1, "Juan", "Perez", null, "j@e.com", "p", 1, true);
        Usuario u2 = new Usuario(2, "Ana", "Gomez", null, "a@e.com", "p", 1, true);
        when(usuarioRepository.findByEstadoUsuario(true)).thenReturn(Arrays.asList(u1, u2));

        List<Usuario> resultado = usuarioService.obtenerTodosUsuariosActivos();

        assertThat(resultado).hasSize(2).containsExactly(u1, u2);
        verify(usuarioRepository).findByEstadoUsuario(true);
    }

    @Test
    void testObtenerTodosUsuariosActivos_ListaVacia() {
        when(usuarioRepository.findByEstadoUsuario(true)).thenReturn(Collections.emptyList());

        List<Usuario> resultado = usuarioService.obtenerTodosUsuariosActivos();

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findByEstadoUsuario(true);
    }

    // --- PRUEBAS PARA obtenerTodosUsuarios ---

    @Test
    void testObtenerTodosUsuarios() {
        Usuario u1 = new Usuario(1, "Juan", "Activo", null, "j@e.com", "p", 1, true);
        Usuario u2 = new Usuario(2, "Ana", "Inactivo", null, "a@e.com", "p", 1, false);
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<Usuario> resultado = usuarioService.obtenerTodosUsuarios();

        assertThat(resultado).hasSize(2).containsExactly(u1, u2);
        verify(usuarioRepository).findAll();
    }

    @Test
    void testObtenerTodosUsuarios_ListaVacia() {
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

        List<Usuario> resultado = usuarioService.obtenerTodosUsuarios();

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findAll();
    }

    // --- PRUEBAS PARA obtenerPermisosUsuario ---

    @Test
    void testObtenerPermisosUsuario_Exito() {
        Rol rolAdmin = new Rol(1L, "ADMIN", Arrays.asList(Permiso.CREAR_USUARIO, Permiso.VER_USUARIO), true);
        Usuario usuario = new Usuario(1, "Juan", "Perez", rolAdmin, "juan@example.com", "pass", 1, true);

        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuario));

        List<Permiso> resultado = usuarioService.obtenerPermisosUsuario(1);

        assertThat(resultado).hasSize(2).containsExactlyInAnyOrder(Permiso.CREAR_USUARIO, Permiso.VER_USUARIO);
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }

    @Test
    void testObtenerPermisosUsuario_UsuarioNoEncontrado() {
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.obtenerPermisosUsuario(1);
        });

        assertThat(thrown.getMessage()).contains("Usuario activo no encontrado con id: 1");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }

    @Test
    void testObtenerPermisosUsuario_RolNulo() {
        Usuario usuario = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, true); // Rol nulo
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuario));

        List<Permiso> resultado = usuarioService.obtenerPermisosUsuario(1);

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }

    @Test
    void testObtenerPermisosUsuario_PermisosRolNulos() {
        Rol rolSinPermisos = new Rol(1L, "VISITANTE", null, true); // Permisos nulos
        Usuario usuario = new Usuario(1, "Juan", "Perez", rolSinPermisos, "juan@example.com", "pass", 1, true);
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuario));

        List<Permiso> resultado = usuarioService.obtenerPermisosUsuario(1);

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
    }


    // --- PRUEBAS PARA asignarRol ---

    @Test
    void testAsignarRol_Exito() {
        Rol rolAntiguo = new Rol(1L, "ANTIGUO", Collections.emptyList(), true);
        Rol nuevoRol = new Rol(2L, "NUEVO", Collections.emptyList(), true);
        Usuario usuario = new Usuario(1, "Juan", "Perez", rolAntiguo, "juan@example.com", "pass", 1, true);
        Usuario usuarioConNuevoRol = new Usuario(1, "Juan", "Perez", nuevoRol, "juan@example.com", "pass", 1, true);

        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(nuevoRol));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioConNuevoRol);

        Usuario resultado = usuarioService.asignarRol(1, 2L);

        assertThat(resultado.getRol().getNombreRol()).isEqualTo("NUEVO");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(rolRepository).findById(2L);
        verify(usuarioRepository).save(usuario); // Verifica que se guardó el usuario modificado
    }

    @Test
    void testAsignarRol_UsuarioActivoNoEncontrado() {
        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.asignarRol(1, 2L);
        });

        assertThat(thrown.getMessage()).contains("Usuario activo no encontrado con id: 1");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(rolRepository, never()).findById(anyLong()); // No debe buscar el rol
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testAsignarRol_RolNoEncontrado() {
        Usuario usuario = new Usuario(1, "Juan", "Perez", null, "juan@example.com", "pass", 1, true);

        when(usuarioRepository.findByIdUsuarioAndEstadoUsuario(1, true)).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(99L)).thenReturn(Optional.empty()); // Rol no existe

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.asignarRol(1, 99L);
        });

        assertThat(thrown.getMessage()).contains("Rol no encontrado con id: 99");
        verify(usuarioRepository).findByIdUsuarioAndEstadoUsuario(1, true);
        verify(rolRepository).findById(99L);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // --- PRUEBAS PARA listarUsuariosPorTienda ---

    @Test
    void testListarUsuariosPorTienda_Exito() {
        Usuario u1 = new Usuario(1, "Juan", "Perez", null, "j@e.com", "p", 101, true);
        Usuario u2 = new Usuario(2, "Ana", "Gomez", null, "a@e.com", "p", 101, true);
        when(usuarioRepository.findByIdTiendaAndEstadoUsuario(101L, true)).thenReturn(Arrays.asList(u1, u2));

        List<Usuario> resultado = usuarioService.listarUsuariosPorTienda(101);

        assertThat(resultado).hasSize(2).containsExactly(u1, u2);
        verify(usuarioRepository).findByIdTiendaAndEstadoUsuario(101L, true);
    }

    @Test
    void testListarUsuariosPorTienda_Vacio() {
        when(usuarioRepository.findByIdTiendaAndEstadoUsuario(102L, true)).thenReturn(Collections.emptyList());

        List<Usuario> resultado = usuarioService.listarUsuariosPorTienda(102);

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findByIdTiendaAndEstadoUsuario(102L, true);
    }

}