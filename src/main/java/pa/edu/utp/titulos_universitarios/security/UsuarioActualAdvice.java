package pa.edu.utp.titulos_universitarios.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pa.edu.utp.titulos_universitarios.TituloService;

/**
 * T-1.7.1 (HU-1.7): antes de mostrar cualquier pantalla, agrega al modelo el
 * usuario con sesion ("usuarioActual") para que el encabezado comun lo use
 * sin repetir codigo en cada controlador.
 * T-1.7.2 (HU-1.7): agrega tambien el contador de pendientes del menu.
 */
@ControllerAdvice
public class UsuarioActualAdvice {

    private final TituloService tituloService;

    public UsuarioActualAdvice(TituloService tituloService) {
        this.tituloService = tituloService;
    }

    @ModelAttribute("usuarioActual")
    public UsuarioActual usuarioActual(Authentication authentication) {
        return UsuarioActual.desde(authentication);
    }

    /**
     * Cantidad de titulos pendientes para el menu. Solo se consulta la base
     * de datos si el usuario es APROBADOR; para los demas devuelve null.
     */
    @ModelAttribute("pendientesPorRevisar")
    public Long pendientesPorRevisar(Authentication authentication) {
        UsuarioActual usuario = UsuarioActual.desde(authentication);
        if (usuario == null || !usuario.isAprobador()) {
            return null;
        }
        return tituloService.contarPendientes();
    }
}