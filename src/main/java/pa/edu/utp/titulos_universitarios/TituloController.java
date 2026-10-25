package pa.edu.utp.titulos_universitarios;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

@Controller
@RequestMapping("/titulos")
public class TituloController {

    private final TituloService tituloService;

    public TituloController(TituloService tituloService) {
        this.tituloService = tituloService;
    }

    @GetMapping
    public String mostrarLista(Model model) {
        model.addAttribute("titulos", tituloService.listar());
        return "titulos/lista";
    }

    // T-1.9.1 (HU-1.9): el formulario de registro vive en su propia página;
    // /titulos queda solo como lista.
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        if (!model.containsAttribute("tituloForm")) {
            model.addAttribute("tituloForm", new TituloForm());
        }

        model.addAttribute("tiposDocumento", TipoDocumento.values());
        return "titulos/nuevo";
    }

    @PostMapping("/nuevo")
    public String registrar(
            @Valid @ModelAttribute("tituloForm") TituloForm formulario,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposDocumento", TipoDocumento.values());
            return "titulos/nuevo";
        }

        try {
            Titulo titulo = new Titulo();
            titulo.setNombreGraduado(formulario.getNombreGraduado());
            titulo.setNombreTitulo(formulario.getNombreTitulo());

            Graduado graduado = new Graduado();
            graduado.setTipoDocumento(formulario.getTipoDocumento());
            graduado.setNumeroDocumento(formulario.getNumeroDocumento());
            titulo.setGraduado(graduado);

            tituloService.registrar(titulo, principal.getName());

            redirectAttributes.addFlashAttribute(
                    "mensaje", "Título registrado correctamente.");

            return "redirect:/titulos";

        } catch (IllegalArgumentException exception) {
            model.addAttribute("tiposDocumento", TipoDocumento.values());

            String mensaje = exception.getMessage();

            if (mensaje == null) {
                model.addAttribute(
                        "error", "No se pudo registrar el título.");
            } else {
                String texto = mensaje.toLowerCase(java.util.Locale.ROOT);

                if (texto.contains("documento ya está registrado con otro nombre")) {
                    // El documento existe, pero pertenece a otro nombre.
                    bindingResult.rejectValue(
                            "nombreGraduado",
                            "titulo.nombreGraduado",
                            mensaje);

                } else if (texto.contains("tipo de documento")) {
                    bindingResult.rejectValue(
                            "tipoDocumento",
                            "titulo.tipoDocumento",
                            mensaje);

                } else if (texto.contains("documento")) {
                    bindingResult.rejectValue(
                            "numeroDocumento",
                            "titulo.numeroDocumento",
                            mensaje);

                } else if (texto.contains("nombre del graduado")
                        || texto.contains("nombre del estudiante")) {
                    bindingResult.rejectValue(
                            "nombreGraduado",
                            "titulo.nombreGraduado",
                            mensaje);

                } else if (texto.contains("título")
                        || texto.contains("titulo")) {
                    bindingResult.rejectValue(
                            "nombreTitulo",
                            "titulo.nombreTitulo",
                            mensaje);

                } else {
                    model.addAttribute("error", mensaje);
                }
            }

            return "titulos/nuevo";
        }
    }

    // T-2.1.3 (HU-2.1): vista de títulos pendientes con acciones aprobar/rechazar.
    @GetMapping("/pendientes")
    public String mostrarPendientes(Model model) {
        model.addAttribute("pendientes", tituloService.listarPendientes());
        return "titulos/pendientes";
    }

    @PostMapping("/{id}/aprobar")
    public String aprobar(
            @PathVariable Long id,
            Principal principal,
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
    public String rechazar(
            @PathVariable Long id,
            Principal principal,
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
