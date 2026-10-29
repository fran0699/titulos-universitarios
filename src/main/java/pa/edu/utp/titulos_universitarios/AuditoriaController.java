package pa.edu.utp.titulos_universitarios;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

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
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String usuario,
            @RequestParam(defaultValue = "1") String pagina,
            Model model) {

        Map<String, String> errores = new HashMap<>();

        LocalDate fechaDesde = convertirFecha(
                desde, "desde", errores);

        LocalDate fechaHasta = convertirFecha(
                hasta, "hasta", errores);

        AccionAuditoria accionSeleccionada =
                convertirAccion(accion, errores);

        int paginaActual = convertirPagina(pagina, errores);

        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("accion", accionSeleccionada);
        model.addAttribute("accionValor", accion);
        model.addAttribute("usuario", usuario);
        model.addAttribute("acciones", AccionAuditoria.values());

        Page<AuditoriaFila> resultados = Page.empty();

        if (errores.isEmpty()) {
            try {
                resultados = auditoriaService.buscar(
                        fechaDesde,
                        fechaHasta,
                        accionSeleccionada,
                        usuario,
                        paginaActual
                );

            } catch (IllegalArgumentException exception) {
                String mensaje = exception.getMessage();

                if (mensaje != null && mensaje.contains("fecha inicial")) {
                    errores.put("hasta", mensaje);
                } else if (mensaje != null
                        && mensaje.toLowerCase().contains("usuario")) {
                    errores.put("usuario", mensaje);
                } else {
                    errores.put("general", mensaje != null
                            ? mensaje
                            : "No se pudieron consultar los eventos.");
                }
            }
        }

        if (!errores.isEmpty()) {
            resultados = Page.empty();
            paginaActual = 1;
        }

        model.addAttribute("errores", errores);
        model.addAttribute("eventos", resultados);
        model.addAttribute("paginaActual", paginaActual);

        return "auditoria";
    }

    private LocalDate convertirFecha(
            String valor,
            String campo,
            Map<String, String> errores) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException exception) {
            errores.put(campo, "Introduce una fecha válida.");
            return null;
        }
    }

    private AccionAuditoria convertirAccion(
            String valor,
            Map<String, String> errores) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            return AccionAuditoria.valueOf(valor);
        } catch (IllegalArgumentException exception) {
            errores.put("accion", "Selecciona una acción válida.");
            return null;
        }
    }

    private int convertirPagina(
            String valor,
            Map<String, String> errores) {

        if (valor == null || valor.isBlank()) {
            return 1;
        }

        try {
            int numero = Integer.parseInt(valor);

            if (numero < 1) {
                errores.put(
                        "general",
                        "El número de página debe ser mayor o igual a 1."
                );
                return 1;
            }

            return numero;

        } catch (NumberFormatException exception) {
            errores.put("general", "El número de página no es válido.");
            return 1;
        }
    }
}