package pa.edu.utp.titulos_universitarios.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioActualTest {

    @Test
    void aprobadorConSesion_debeMostrarseComoAprobador() {
        UsuarioActual usuario = UsuarioActual.desde(sesion("aprobador1", "ROLE_APROBADOR"));

        assertEquals("aprobador1", usuario.getNombre());
        assertEquals("Aprobador", usuario.getRolTexto());
        assertTrue(usuario.isAprobador());
        assertFalse(usuario.isRegistrador());
    }

    @Test
    void registradorConSesion_debeMostrarseComoRegistrador() {
        UsuarioActual usuario = UsuarioActual.desde(sesion("registrador1", "ROLE_REGISTRADOR"));

        assertEquals("registrador1", usuario.getNombre());
        assertEquals("Registrador", usuario.getRolTexto());
        assertTrue(usuario.isRegistrador());
        assertFalse(usuario.isAprobador());
    }

    @Test
    void sinSesion_noHayUsuarioActual() {
        assertNull(UsuarioActual.desde(null));
    }

    @Test
    void usuarioAnonimo_noHayUsuarioActual() {
        AnonymousAuthenticationToken anonimo = new AnonymousAuthenticationToken(
                "clave", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        assertNull(UsuarioActual.desde(anonimo));
    }

    private UsernamePasswordAuthenticationToken sesion(String usuario, String rol) {
        return UsernamePasswordAuthenticationToken.authenticated(
                usuario, null, List.of(new SimpleGrantedAuthority(rol)));
    }
}