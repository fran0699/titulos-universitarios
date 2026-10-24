package pa.edu.utp.titulos_universitarios.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import pa.edu.utp.titulos_universitarios.entity.Rol;
import pa.edu.utp.titulos_universitarios.entity.Usuario;
import pa.edu.utp.titulos_universitarios.repository.UsuarioRepository;

/** T-4.10.1 (HU-4.10): creación del usuario técnico de Prometheus. */
@ExtendWith(MockitoExtension.class)
class UsuarioMonitoreoInitializerTest {

    private static final String USUARIO = "prometheus";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioMonitoreoInitializer conPassword(String password) {
        return new UsuarioMonitoreoInitializer(usuarioRepository, passwordEncoder, USUARIO, password);
    }

    @Test
    void creaElUsuarioTecnicoAunqueLaTablaTengaOtrosUsuarios() {
        when(usuarioRepository.findByUsername(USUARIO)).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave-de-prueba-123")).thenReturn("hash-bcrypt");

        assertTrue(conPassword("clave-de-prueba-123").crearSiNoExiste());

        // No depende de que la tabla esté vacía: nunca cuenta los usuarios.
        verify(usuarioRepository, never()).count();
        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertEquals(USUARIO, guardado.getValue().getUsername());
        assertEquals("hash-bcrypt", guardado.getValue().getPassword());
        assertEquals(Rol.MONITOREO, guardado.getValue().getRol());
    }

    @Test
    void noLoCreaDosVecesSiYaExiste() {
        when(usuarioRepository.findByUsername(USUARIO)).thenReturn(Optional.of(new Usuario()));

        assertFalse(conPassword("clave-de-prueba-123").crearSiNoExiste());

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void noLoCreaSiFaltaLaVariableDeEntorno() {
        assertFalse(conPassword("").crearSiNoExiste());

        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    void noLoCreaConUnaContrasenaDe11Caracteres() {
        assertFalse(conPassword("12345678901").crearSiNoExiste());

        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    void aceptaUnaContrasenaDe12Caracteres() {
        when(usuarioRepository.findByUsername(USUARIO)).thenReturn(Optional.empty());

        assertTrue(conPassword("123456789012").crearSiNoExiste());

        verify(usuarioRepository).save(any(Usuario.class));
    }
}