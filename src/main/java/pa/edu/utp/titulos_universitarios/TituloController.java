package pa.edu.utp.titulos_universitarios;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // T-2.1.3 (HU-2.1): vista de titulos pendientes con acciones aprobar/rechazar.
    @GetMapping("/pendientes")
    public String mostrarPendientes(Model model) {
        model.addAttribute("pendientes", tituloService.listarPendientes());
        return "titulos/pendientes";
    }

    @PostMapping("/{id}/aprobar")
    public String aprobar(@PathVariable Long id, Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            tituloService.aprobar(id, principal.getName());
            redirectAttributes.addFlashAttribute("mensaje", "Título aprobado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/titulos/pendientes";
    }

    @PostMapping("/{id}/rechazar")
    public String rechazar(@PathVariable Long id, Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            tituloService.rechazar(id, principal.getName());
            redirectAttributes.addFlashAttribute("mensaje", "Título rechazado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/titulos/pendientes";
    }
}
