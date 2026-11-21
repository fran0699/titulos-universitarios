package pa.edu.utp.titulos_universitarios;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final TituloConsultaService tituloConsultaService;

    public TituloController(
            TituloService tituloService,
            TituloConsultaService tituloConsultaService) {
        this.tituloService = tituloService;
        this.tituloConsultaService = tituloConsultaService;
    }

    // T-1.8.2 (HU-1.8): lista con búsqueda, filtro por estado y paginación.
    // Mismo diseño que /auditoria (HU-3.4): los datos que no se pueden leer
    // (estado, página) se rechazan aquí; la búsqueda la valida el servicio.
    // En ambos casos el error se muestra junto al campo y no se consulta la
    // base de datos (CA4).
    @GetMapping
    public String mostrarLista(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "1") String pagina,
            Model model) {

        Map<String, String> errores = new HashMap<>();

        EstadoTitulo estadoSeleccionado = convertirEstado(estado, errores);
        int paginaActual = convertirPagina(pagina, errores);

        Page<Titulo> resultados = Page.empty();

        if (errores.isEmpty()) {
            try {
                resultados = tituloConsultaService.buscar(
                        q, estadoSeleccionado, paginaActual);

            } catch (TituloValidationException exception) {
                errores.put(exception.getCampo(), exception.getMessage());
            }
        }

        if (!errores.isEmpty()) {
            resultados = Page.empty();
            paginaActual = 1;
        }

        boolean hayFiltros = (q != null && !q.isBlank())
                || (estado != null && !estado.isBlank());

        model.addAttribute("q", q);
        model.addAttribute("estadoValor", estado);
        model.addAttribute("estados", EstadoTitulo.values());
        model.addAttribute("hayFiltros", hayFiltros);
        model.addAttribute("errores", errores);
        model.addAttribute("resultados", resultados);
        model.addAttribute("paginaActual", paginaActual);

        return "titulos/lista";
    }

    private EstadoTitulo convertirEstado(
            String valor, Map<String, String> errores) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            return EstadoTitulo.valueOf(valor.strip());
        } catch (IllegalArgumentException exception) {
            errores.put("estado", "Elige un estado de la lista.");
            return null;
        }
    }

    private int convertirPagina(String valor, Map<String, String> errores) {
        try {
            int numero = Integer.parseInt(valor.strip());

            if (numero >= 1) {
                return numero;
            }
        } catch (NumberFormatException exception) {
            // Se informa abajo, igual que una página menor que 1.
        }

        errores.put("pagina",
                "El número de página debe ser un entero mayor o igual a 1.");
        return 1;
    }

    /** T-2.4.3 (HU-2.4): descarga la credencial PDF de un título aprobado. */
    @GetMapping("/{id}/credencial")
    public ResponseEntity<byte[]> descargarCredencial(@PathVariable Long id) {
        byte[] pdf = tituloService.generarCredencialPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("credencial-titulo-" + id + ".pdf")
                                .build()
                                .toString())
                .body(pdf);
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

        } catch (TituloValidationException exception) {
            // HU-4.7: el servicio indica el campo; no se busca en el texto del mensaje.
            model.addAttribute("tiposDocumento", TipoDocumento.values());

            if (exception.getCampo() != null) {
                bindingResult.rejectValue(
                        exception.getCampo(),
                        "titulo." + exception.getCampo(),
                        exception.getMessage());
            } else {
                model.addAttribute("error", exception.getMessage());
            }

            return "titulos/nuevo";

        } catch (IllegalArgumentException exception) {
            model.addAttribute("tiposDocumento", TipoDocumento.values());
            model.addAttribute(
                    "error",
                    exception.getMessage() != null
                            ? exception.getMessage()
                            : "No se pudo registrar el título.");
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