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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioRepository usuarioRepository; // Mockeamos también el UsuarioRepository

    @InjectMocks
    private RolServiceImpl rolService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- PRUEBAS PARA crearRol ---

    @Test
    void testCrearRol_Exito() {
        // Datos de prueba
        Rol nuevoRol = new Rol(null, "ADMIN", Arrays.asList(Permiso.CREAR_USUARIO), false); // Estado inicial no importa, se pondrá a true
        Rol rolGuardado = new Rol(1L, "ADMIN", Arrays.asList(Permiso.CREAR_USUARIO), true);

        // Comportamiento esperado de los mocks
        when(rolRepository.findByNombreRolAndEstadoRol(anyString(), eq(true))).thenReturn(Optional.empty()); // No existe rol activo con ese nombre
        when(rolRepository.save(any(Rol.class))).thenReturn(rolGuardado);

        // Ejecutar el método del servicio
        Rol resultado = rolService.crearRol(nuevoRol);

        // Verificar aserciones
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombreRol()).isEqualTo("ADMIN");
        assertThat(resultado.getEstadoRol()).isTrue(); // Debe ser activo por defecto
        assertThat(resultado.getPermisosRol()).contains(Permiso.CREAR_USUARIO);

        // Verificar interacciones con los mocks
        verify(rolRepository).findByNombreRolAndEstadoRol("ADMIN", true);
        verify(rolRepository).save(any(Rol.class));
    }

    @Test
    void testCrearRol_NombreYaEnUsoActivo() {
        // Datos de prueba
        Rol existingRol = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Rol nuevoRolConNombreDuplicado = new Rol(null, "ADMIN", Collections.emptyList(), false);

        // Comportamiento esperado de los mocks
        when(rolRepository.findByNombreRolAndEstadoRol(anyString(), eq(true))).thenReturn(Optional.of(existingRol)); // Ya existe un rol activo con ese nombre

        // Ejecutar el método del servicio y esperar la excepción
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            rolService.crearRol(nuevoRolConNombreDuplicado);
        });

        // Verificar el mensaje de la excepción
        assertThat(thrown.getMessage()).contains("Ya existe un rol activo con el nombre: ADMIN");

        // Verificar interacciones con los mocks (save no debería llamarse)
        verify(rolRepository).findByNombreRolAndEstadoRol("ADMIN", true);
        verify(rolRepository, never()).save(any(Rol.class));
    }

    // --- PRUEBAS PARA actualizarRol ---

    @Test
    void testActualizarRol_Exito() {
        // Datos de prueba
        Rol existingRol = new Rol(1L, "VIEJO_NOMBRE", Arrays.asList(Permiso.VER_USUARIO), true);
        Rol rolDetails = new Rol(null, "NUEVO_NOMBRE", Arrays.asList(Permiso.CREAR_USUARIO, Permiso.ELIMINAR_USUARIO), false); // Nuevo estado

        // Comportamiento esperado de los mocks
        when(rolRepository.findById(1L)).thenReturn(Optional.of(existingRol)); // El rol existe
        when(rolRepository.findByNombreRolAndEstadoRol(eq("NUEVO_NOMBRE"), eq(true))).thenReturn(Optional.empty()); // Nuevo nombre disponible
        when(rolRepository.save(any(Rol.class))).thenReturn(rolDetails); // Simular el guardado

        // Ejecutar el método del servicio
        Rol resultado = rolService.actualizarRol(1L, rolDetails);

        // Verificar aserciones
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombreRol()).isEqualTo("NUEVO_NOMBRE");
        assertThat(resultado.getPermisosRol()).hasSize(2).containsExactlyInAnyOrder(Permiso.CREAR_USUARIO, Permiso.ELIMINAR_USUARIO);
        assertThat(resultado.getEstadoRol()).isFalse(); // El estado se actualizó

        // Verificar interacciones
        verify(rolRepository).findById(1L);
        verify(rolRepository).findByNombreRolAndEstadoRol("NUEVO_NOMBRE", true);
        verify(rolRepository).save(existingRol); // Se llama save con el objeto existingRol modificado
    }

    @Test
    void testActualizarRol_RolNoEncontrado() {
        Rol rolDetails = new Rol(null, "CualquierNombre", null, null);

        when(rolRepository.findById(99L)).thenReturn(Optional.empty()); // Rol no existe

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            rolService.actualizarRol(99L, rolDetails);
        });

        assertThat(thrown.getMessage()).contains("Rol no encontrado con id: 99");
        verify(rolRepository).findById(99L);
        verify(rolRepository, never()).findByNombreRolAndEstadoRol(anyString(), anyBoolean());
        verify(rolRepository, never()).save(any(Rol.class));
    }

    @Test
    void testActualizarRol_NombreDuplicadoParaOtroRolActivo() {
        // Datos de prueba
        Rol existingRol = new Rol(1L, "ORIGINAL", Collections.emptyList(), true);
        Rol otroRolConNombre = new Rol(2L, "DUPLICADO", Collections.emptyList(), true);
        Rol rolDetails = new Rol(null, "DUPLICADO", Collections.emptyList(), null); // Intenta cambiar a un nombre ya en uso

        // Comportamiento esperado de los mocks
        when(rolRepository.findById(1L)).thenReturn(Optional.of(existingRol));
        // Simular que findByNombreRolAndEstadoRol encuentra el otro rol (con ID diferente)
        when(rolRepository.findByNombreRolAndEstadoRol(eq("DUPLICADO"), eq(true))).thenReturn(Optional.of(otroRolConNombre));

        // Ejecutar el método y esperar la excepción
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            rolService.actualizarRol(1L, rolDetails);
        });

        // Verificar el mensaje de la excepción
        assertThat(thrown.getMessage()).contains("El nombre de rol 'DUPLICADO' ya está en uso por otro rol activo.");
        verify(rolRepository).findById(1L);
        verify(rolRepository).findByNombreRolAndEstadoRol("DUPLICADO", true);
        verify(rolRepository, never()).save(any(Rol.class));
    }

    @Test
    void testActualizarRol_NombreDuplicadoPorMismoRol() {
        // Este test asegura que si se envía el mismo nombre de rol, no hay problema
        Rol existingRol = new Rol(1L, "MISMO_NOMBRE", Arrays.asList(Permiso.VER_USUARIO), true);
        Rol rolDetails = new Rol(null, "MISMO_NOMBRE", Arrays.asList(Permiso.CREAR_USUARIO), true); // Mismo nombre, otros cambios

        when(rolRepository.findById(1L)).thenReturn(Optional.of(existingRol));
        // Cuando el mismo rol se busca por nombre, debe ser ignorado si el ID coincide.
        // Aquí no necesitamos mockear findByNombreRolAndEstadoRol porque el if del servicio lo maneja.
        when(rolRepository.save(any(Rol.class))).thenReturn(rolDetails);

        Rol resultado = rolService.actualizarRol(1L, rolDetails);

        assertThat(resultado.getNombreRol()).isEqualTo("MISMO_NOMBRE");
        assertThat(resultado.getPermisosRol()).contains(Permiso.CREAR_USUARIO); // Permisos actualizados
        verify(rolRepository).findById(1L);
        // findByNombreRolAndEstadoRol NO debería ser llamado si el nombre no cambió
        verify(rolRepository, never()).findByNombreRolAndEstadoRol(anyString(), anyBoolean());
        verify(rolRepository).save(existingRol);
    }


    // --- PRUEBAS PARA desactivarRol ---

    @Test
    void testDesactivarRol_Exito() {
        // Datos de prueba
        Rol rolActivo = new Rol(1L, "ROL_ACTIVO", Collections.emptyList(), true);
        Rol rolInactivo = new Rol(1L, "ROL_ACTIVO", Collections.emptyList(), false);

        // Comportamiento esperado de los mocks
        when(rolRepository.findByIdAndEstadoRol(1L, true)).thenReturn(Optional.of(rolActivo)); // Rol activo existe
        when(usuarioRepository.findByRolAndEstadoUsuario(eq(rolActivo), eq(true))).thenReturn(Collections.emptyList()); // No hay usuarios activos vinculados
        when(rolRepository.save(any(Rol.class))).thenReturn(rolInactivo);

        // Ejecutar el método del servicio
        Rol resultado = rolService.desactivarRol(1L);

        // Verificar aserciones
        assertThat(resultado.getEstadoRol()).isFalse();

        // Verificar interacciones
        verify(rolRepository).findByIdAndEstadoRol(1L, true);
        verify(usuarioRepository).findByRolAndEstadoUsuario(rolActivo, true);
        verify(rolRepository).save(rolActivo); // Se llama save con el objeto rolActivo modificado
    }

    @Test
    void testDesactivarRol_RolActivoNoEncontrado() {
        when(rolRepository.findByIdAndEstadoRol(99L, true)).thenReturn(Optional.empty()); // Rol no existe

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            rolService.desactivarRol(99L);
        });

        assertThat(thrown.getMessage()).contains("Rol activo no encontrado con id: 99");
        verify(rolRepository).findByIdAndEstadoRol(99L, true);
        verifyNoInteractions(usuarioRepository); // No debería interactuar con usuarioRepository
        verify(rolRepository, never()).save(any(Rol.class));
    }

    @Test
    void testDesactivarRol_ConUsuariosActivosVinculados() {
        // Datos de prueba
        Rol rolConUsuarios = new Rol(1L, "ROL_CON_USUARIOS", Collections.emptyList(), true);
        Usuario usuarioVinculado = new Usuario(1, "User", "Test", rolConUsuarios, "user@test.com", "pass", 1, true);

        // Comportamiento esperado de los mocks
        when(rolRepository.findByIdAndEstadoRol(1L, true)).thenReturn(Optional.of(rolConUsuarios));
        when(usuarioRepository.findByRolAndEstadoUsuario(eq(rolConUsuarios), eq(true))).thenReturn(Arrays.asList(usuarioVinculado)); // Hay usuarios activos vinculados

        // Ejecutar el método del servicio y esperar la excepción
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            rolService.desactivarRol(1L);
        });

        // Verificar el mensaje de la excepción
        assertThat(thrown.getMessage()).contains("No se puede desactivar el rol 'ROL_CON_USUARIOS' porque está vinculado a usuarios activos.");

        // Verificar interacciones
        verify(rolRepository).findByIdAndEstadoRol(1L, true);
        verify(usuarioRepository).findByRolAndEstadoUsuario(rolConUsuarios, true);
        verify(rolRepository, never()).save(any(Rol.class)); // El save NO debe ser llamado
    }

    // --- PRUEBAS PARA reactivarRol ---

    @Test
    void testReactivarRol_Exito() {
        // Datos de prueba
        Rol rolInactivo = new Rol(1L, "ROL_INACTIVO", Collections.emptyList(), false);
        Rol rolActivo = new Rol(1L, "ROL_INACTIVO", Collections.emptyList(), true);

        // Comportamiento esperado de los mocks
        when(rolRepository.findByIdAndEstadoRol(1L, false)).thenReturn(Optional.of(rolInactivo));
        when(rolRepository.save(any(Rol.class))).thenReturn(rolActivo);

        // Ejecutar el método del servicio
        Rol resultado = rolService.reactivarRol(1L);

        // Verificar aserciones
        assertThat(resultado.getEstadoRol()).isTrue();

        // Verificar interacciones
        verify(rolRepository).findByIdAndEstadoRol(1L, false);
        verify(rolRepository).save(rolInactivo); // Se llama save con el objeto rolInactivo modificado
        verifyNoInteractions(usuarioRepository); // No debería interactuar con usuarioRepository en este caso
    }

    @Test
    void testReactivarRol_RolInactivoNoEncontrado() {
        when(rolRepository.findByIdAndEstadoRol(99L, false)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            rolService.reactivarRol(99L);
        });

        assertThat(thrown.getMessage()).contains("Rol inactivo no encontrado con id: 99");
        verify(rolRepository).findByIdAndEstadoRol(99L, false);
        verify(rolRepository, never()).save(any(Rol.class));
        verifyNoInteractions(usuarioRepository);
    }

    // --- PRUEBAS PARA obtenerRolPorId ---

    @Test
    void testObtenerRolPorId_ActivoExistente() {
        Rol rol = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        when(rolRepository.findByIdAndEstadoRol(1L, true)).thenReturn(Optional.of(rol));

        Optional<Rol> resultado = rolService.obtenerRolPorId(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
        assertThat(resultado.get().getEstadoRol()).isTrue();
        verify(rolRepository).findByIdAndEstadoRol(1L, true);
    }

    @Test
    void testObtenerRolPorId_NoExistenteOInactivo() {
        when(rolRepository.findByIdAndEstadoRol(1L, true)).thenReturn(Optional.empty());

        Optional<Rol> resultado = rolService.obtenerRolPorId(1L);

        assertThat(resultado).isEmpty();
        verify(rolRepository).findByIdAndEstadoRol(1L, true);
    }

    // --- PRUEBAS PARA obtenerTodosRolesActivos ---

    @Test
    void testObtenerTodosRolesActivos_Exito() {
        Rol r1 = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Rol r2 = new Rol(2L, "USUARIO", Collections.emptyList(), true);
        when(rolRepository.findByEstadoRol(true)).thenReturn(Arrays.asList(r1, r2));

        List<Rol> resultado = rolService.obtenerTodosRolesActivos();

        assertThat(resultado).hasSize(2).containsExactly(r1, r2);
        verify(rolRepository).findByEstadoRol(true);
    }

    @Test
    void testObtenerTodosRolesActivos_ListaVacia() {
        when(rolRepository.findByEstadoRol(true)).thenReturn(Collections.emptyList());

        List<Rol> resultado = rolService.obtenerTodosRolesActivos();

        assertThat(resultado).isEmpty();
        verify(rolRepository).findByEstadoRol(true);
    }

    // --- PRUEBAS PARA obtenerTodosRoles ---

    @Test
    void testObtenerTodosRoles_Exito() {
        Rol r1 = new Rol(1L, "ADMIN", Collections.emptyList(), true);
        Rol r2 = new Rol(2L, "INACTIVO", Collections.emptyList(), false);
        when(rolRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

        List<Rol> resultado = rolService.obtenerTodosRoles();

        assertThat(resultado).hasSize(2).containsExactly(r1, r2);
        verify(rolRepository).findAll();
    }

    @Test
    void testObtenerTodosRoles_ListaVacia() {
        when(rolRepository.findAll()).thenReturn(Collections.emptyList());

        List<Rol> resultado = rolService.obtenerTodosRoles();

        assertThat(resultado).isEmpty();
        verify(rolRepository).findAll();
    }
}
