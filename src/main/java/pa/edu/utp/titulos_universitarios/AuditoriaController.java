
package pa.edu.utp.titulos_universitarios;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;

@Controller
public class AuditoriaController {

    private final AuditoriaConsultaService auditoriaService;

    public AuditoriaController(AuditoriaConsultaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/auditoria")
    public String mostrarAuditoria(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) AccionAuditoria accion,
            @RequestParam(required = false) String usuario,
            @RequestParam(defaultValue = "1") int pagina,
            Model model) {

        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("accion", accion);
        model.addAttribute("usuario", usuario);
        model.addAttribute("acciones", AccionAuditoria.values());

        try {
            Page<AuditoriaFila> resultados = auditoriaService.buscar(
                    desde, hasta, accion, usuario, pagina);

            model.addAttribute("eventos", resultados);
            model.addAttribute("paginaActual", pagina);

        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            model.addAttribute("eventos", Page.empty());
            model.addAttribute("paginaActual", 1);
        }

        return "auditoria";
    }
}
