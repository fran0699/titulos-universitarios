package pa.edu.utp.titulos_universitarios.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * T-1.7.1 (HU-1.7): antes de mostrar cualquier pantalla, agrega al modelo el
 * usuario con sesion ("usuarioActual") para que el encabezado comun lo use
 * sin repetir codigo en cada controlador.
 */
@ControllerAdvice
public class UsuarioActualAdvice {

    @ModelAttribute("usuarioActual")
    public UsuarioActual usuarioActual(Authentication authentication) {
        return UsuarioActual.desde(authentication);
    }
}