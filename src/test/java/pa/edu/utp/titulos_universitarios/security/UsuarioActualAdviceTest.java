package pa.edu.utp.titulos_universitarios.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pa.edu.utp.titulos_universitarios.TituloService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioActualAdviceTest {

    @Mock
    private TituloService tituloService;

    @InjectMocks
    private UsuarioActualAdvice advice;

    @Test
    void aprobador_recibeLaCantidadDePendientes() {
        when(tituloService.contarPendientes()).thenReturn(3L);

        Long resultado = advice.pendientesPorRevisar(sesion("aprobador1", "ROLE_APROBADOR"));

        assertEquals(3L, resultado);
    }

    @Test
    void registrador_noRecibeContadorNiConsultaLaBase() {
        Long resultado = advice.pendientesPorRevisar(sesion("registrador1", "ROLE_REGISTRADOR"));

        assertNull(resultado);
        verifyNoInteractions(tituloService);
    }

    @Test
    void sinSesion_noRecibeContadorNiConsultaLaBase() {
        assertNull(advice.pendientesPorRevisar(null));
        verifyNoInteractions(tituloService);
    }

    private UsernamePasswordAuthenticationToken sesion(String usuario, String rol) {
        return UsernamePasswordAuthenticationToken.authenticated(
                usuario, null, List.of(new SimpleGrantedAuthority(rol)));
    }
}