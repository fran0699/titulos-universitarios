package pa.edu.utp.titulos_universitarios;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirige la raiz "/" a la vista de titulos.
 *
 * Tras iniciar sesion, Spring Security envia al usuario a "/" si no habia una
 * peticion guardada; como no hay nada mapeado en la raiz, antes salia una
 * pagina 404 (Whitelabel Error Page). Este controlador evita ese 404 llevando
 * al formulario de titulos. Si mas adelante hay un menu o pagina de inicio,
 * basta cambiar el destino aqui.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/titulos";
    }
}
