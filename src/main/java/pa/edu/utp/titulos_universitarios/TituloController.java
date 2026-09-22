package pa.edu.utp.titulos_universitarios;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/titulos")
public class TituloController {

    private final TituloService tituloService;

    public TituloController(TituloService tituloService) {
        this.tituloService = tituloService;
    }

    @GetMapping
    public String mostrarRegistro(Model model) {
        if (!model.containsAttribute("titulo")) {
            model.addAttribute("titulo", new Titulo());
        }
        model.addAttribute("titulos", tituloService.listar());
        return "titulos/lista";
    }

    @PostMapping
    public String registrar(Titulo titulo, RedirectAttributes redirectAttributes) {
        try {
            tituloService.registrar(titulo);
            redirectAttributes.addFlashAttribute("mensaje", "Título registrado correctamente.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            redirectAttributes.addFlashAttribute("titulo", titulo);
        }
        return "redirect:/titulos";
    }
}
