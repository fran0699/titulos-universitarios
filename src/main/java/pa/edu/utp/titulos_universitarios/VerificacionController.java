package pa.edu.utp.titulos_universitarios;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletResponse;

/**
 * T-3.1.1 (HU-3.1): verificacion publica (P7 y P8). No requiere sesion; la ruta
 * /verificar/** ya es publica en SecurityConfig.
 */
@Controller
public class VerificacionController {

    private final VerificacionService verificacionService;

    public VerificacionController(VerificacionService verificacionService) {
        this.verificacionService = verificacionService;
    }

    /**
     * P7: formulario del codigo. Sin parametro solo muestra el formulario; con el
     * parametro lleva a /verificar/{codigo}, o muestra el error bajo el campo si
     * esta vacio.
     */
    @GetMapping("/verificar")
    public String formulario(
            @RequestParam(name = "codigo", required = false) String codigo,
            Model model) {

        model.addAttribute("institucion", verificacionService.getInstitucion());

        if (codigo == null) {
            return "verificacion/formulario";
        }

        String normalizado = VerificacionService.normalizar(codigo);
        if (normalizado.isEmpty()) {
            model.addAttribute("errorCodigo", true);
            return "verificacion/formulario";
        }

        // El codigo viaja codificado como un solo segmento de la ruta.
        return "redirect:" + UriComponentsBuilder.fromPath("/verificar/{codigo}")
                .buildAndExpand(normalizado)
                .encode()
                .toUriString();
    }

    /** P8: resultado. 200 si el titulo es valido; 404 en cualquier otro caso. */
    @GetMapping("/verificar/{codigo}")
    public String resultado(
            @PathVariable String codigo,
            Model model,
            HttpServletResponse response) {

        model.addAttribute("institucion", verificacionService.getInstitucion());
        Optional<ResultadoVerificacion> resultado = verificacionService.consultar(codigo);

        if (resultado.isEmpty()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            model.addAttribute("valido", false);
            return "verificacion/resultado";
        }

        model.addAttribute("valido", true);
        model.addAttribute("resultado", resultado.get());
        return "verificacion/resultado";
    }
}
