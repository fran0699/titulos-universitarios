package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;
import pa.edu.utp.titulos_universitarios.repository.GraduadoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CredencialDownloadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TituloRepository tituloRepository;

    @Autowired
    private GraduadoRepository graduadoRepository;

    private Long aprobadoId;
    private Long pendienteId;

    @BeforeEach
    void prepararTitulos() {
        tituloRepository.deleteAll();
        graduadoRepository.deleteAll();

        Graduado graduadoAprobado = graduadoRepository.save(
                new Graduado(TipoDocumento.CEDULA, "8-123-456", "Ana Perez"));
        Titulo aprobado = titulo("Ana Perez", graduadoAprobado, EstadoTitulo.APROBADO,
                "ID-CREDENCIAL-PRUEBA-243");
        aprobadoId = tituloRepository.save(aprobado).getId();

        Graduado graduadoPendiente = graduadoRepository.save(
                new Graduado(TipoDocumento.CEDULA, "8-654-321", "Luis Gomez"));
        Titulo pendiente = titulo("Luis Gomez", graduadoPendiente, EstadoTitulo.PENDIENTE, null);
        pendienteId = tituloRepository.save(pendiente).getId();
    }

    @Test
    void aprobadorPuedeDescargarPdfYSeGuardaEvidenciaVisual() throws Exception {
        MvcResult result = mockMvc.perform(get("/titulos/{id}/credencial", aprobadoId)
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")))
                .andReturn();

        byte[] pdf = result.getResponse().getContentAsByteArray();
        assertTrue(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));

        Path evidenceDirectory = Path.of("target", "evidencias-hu-2.4.3");
        Files.createDirectories(evidenceDirectory);
        Path pdfPath = evidenceDirectory.resolve("credencial-descargada.pdf");
        Path pngPath = evidenceDirectory.resolve("credencial-descargada.png");
        Files.write(pdfPath, pdf);
        try (var document = Loader.loadPDF(pdf)) {
            ImageIO.write(new PDFRenderer(document).renderImageWithDPI(0, 130), "png", pngPath.toFile());
        }
    }

    @Test
    void registradorPuedeDescargarPdf() throws Exception {
        mockMvc.perform(get("/titulos/{id}/credencial", aprobadoId)
                        .with(user("registrador1").roles("REGISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void monitoreoRecibe403AlIntentarDescargar() throws Exception {
        mockMvc.perform(get("/titulos/{id}/credencial", aprobadoId)
                        .with(user("prometheus").roles("MONITOREO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinSesionRedirigeAlInicioDeSesion() throws Exception {
        mockMvc.perform(get("/titulos/{id}/credencial", aprobadoId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void tituloNoAprobadoNoPuedeDescargarse() throws Exception {
        mockMvc.perform(get("/titulos/{id}/credencial", pendienteId)
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaMuestraEnlaceSoloParaTituloAprobado() throws Exception {
        String html = mockMvc.perform(get("/titulos")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(html.contains("/titulos/" + aprobadoId + "/credencial"));
        assertTrue(html.contains("Descargar PDF"));
        assertTrue(html.contains("No disponible"));
    }

    private Titulo titulo(String nombre, Graduado graduado, EstadoTitulo estado, String identificador) {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado(nombre);
        titulo.setNombreTitulo("Licenciatura en Ingenieria de Software");
        titulo.setFechaRegistro(LocalDate.of(2025, 1, 15));
        titulo.setFechaRevision(estado == EstadoTitulo.APROBADO
                ? LocalDateTime.of(2026, 10, 22, 9, 30)
                : null);
        titulo.setEstado(estado);
        titulo.setIdentificador(identificador);
        titulo.setGraduado(graduado);
        return titulo;
    }
}
