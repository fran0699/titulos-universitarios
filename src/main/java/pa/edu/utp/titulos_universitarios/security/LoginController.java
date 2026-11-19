package pa.edu.utp.titulos_universitarios.security;

import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import pa.edu.utp.titulos_universitarios.VerificacionService;

/**
 * T-1.5.1 (HU-1.5): pagina de inicio de sesion propia (P1). Es publica: se
 * declara con permitAll() en SecurityConfig. Spring Security procesa el POST
 * a /login; este controlador solo muestra la pagina.
 *
 * Los mensajes son dos textos fijos de la plantilla: aqui solo se decide si
 * se muestran, nunca se copia un texto que llegue en la direccion.
 *
 * Modo demostracion (guia de pantallas, P1): solo con el perfil "demo" se
 * muestran la franja y los botones «Registrador» y «Aprobador». Cada boton es
 * un enlace (/login?usuario=...) que devuelve el formulario ya lleno, sin
 * JavaScript. El valor llega solo como clave de una lista fija: nunca se copia
 * un texto de la direccion.
 */
@Controller
public class LoginController {

    private static final String CLAVE_DEMO = "clave1234";

    /** Usuarios de prueba que ofrecen los botones (clave del enlace -> usuario). */
    private static final Map<String, String> USUARIOS_DEMO = Map.of(
            "registrador", "registrador1",
            "aprobador", "aprobador1");

    private final VerificacionService verificacionService;
    private final Environment environment;

    public LoginController(VerificacionService verificacionService, Environment environment) {
        this.verificacionService = verificacionService;
        this.environment = environment;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "logout", required = false) String logout,
            @RequestParam(name = "usuario", required = false) String usuario,
            Model model) {
        model.addAttribute("institucion", verificacionService.getInstitucion());
        model.addAttribute("credencialesIncorrectas", error != null);
        model.addAttribute("sesionCerrada", logout != null);

        boolean demo = environment.acceptsProfiles(Profiles.of("demo"));
        model.addAttribute("demo", demo);
        if (demo && usuario != null && USUARIOS_DEMO.containsKey(usuario)) {
            model.addAttribute("usuarioDemo", USUARIOS_DEMO.get(usuario));
            model.addAttribute("claveDemo", CLAVE_DEMO);
        }
        return "login";
    }
}
