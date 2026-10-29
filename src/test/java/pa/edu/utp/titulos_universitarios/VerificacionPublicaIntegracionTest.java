package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;
import pa.edu.utp.titulos_universitarios.repository.GraduadoRepository;

/**
 * T-3.1.1 (HU-3.1): la verificacion publica responde sin sesion, da 200 solo para
 * un titulo APROBADO y 404 en cualquier otro caso, y /titulos sigue protegido.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VerificacionPublicaIntegracionTest {

    private static final String CODIGO_APROBADO = "11111111-1111-4111-8111-111111111111";
    private static final String CODIGO_PENDIENTE = "22222222-2222-4222-8222-222222222222";
    private static final String CODIGO_RECHAZADO = "33333333-3333-4333-8333-333333333333";
    private static final String CODIGO_INEXISTENTE = "44444444-4444-4444-8444-444444444444";

    private static final String REVISOR = "aprobador.secreto";

    private Long aprobadoId;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TituloRepository tituloRepository;

    @Autowired
    private GraduadoRepository graduadoRepository;

    @BeforeEach
    void prepararTitulos() {
        tituloRepository.deleteAll();
        graduadoRepository.deleteAll();

        guardar("8-111-111", EstadoTitulo.APROBADO, CODIGO_APROBADO);
        guardar("8-222-222", EstadoTitulo.PENDIENTE, CODIGO_PENDIENTE);
        guardar("8-333-333", EstadoTitulo.RECHAZADO, CODIGO_RECHAZADO);
    }

    private void guardar(String documento, EstadoTitulo estado, String identificador) {
        Graduado graduado = graduadoRepository.save(
                new Graduado(TipoDocumento.CEDULA, documento, "Graduado " + estado));
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Graduado " + estado);
        titulo.setNombreTitulo("Licenciatura en Ingenieria de Software");
        titulo.setFechaRegistro(LocalDate.of(2026, 10, 1));
        titulo.setEstado(estado);
        titulo.setIdentificador(identificador);
        titulo.setGraduado(graduado);
        if (estado == EstadoTitulo.APROBADO) {
            titulo.setFechaRevision(LocalDateTime.of(2026, 10, 10, 9, 0));
            titulo.setRevisadoPor(REVISOR);
        }
        Long id = tituloRepository.save(titulo).getId();
        if (estado == EstadoTitulo.APROBADO) {
            aprobadoId = id;
        }
    }

    private String cuerpo(String ruta) throws Exception {
        return mockMvc.perform(get(ruta)).andReturn().getResponse().getContentAsString();
    }

    @Test
    void formularioSeAbreSinSesion() throws Exception {
        mockMvc.perform(get("/verificar")).andExpect(status().isOk());
        String html = cuerpo("/verificar");
        assertTrue(html.contains("name=\"codigo\""));
        assertTrue(html.contains("noindex"));
    }

    @Test
    void hojaDeEstilosPublicaSeServeSinSesion() throws Exception {
        mockMvc.perform(get("/css/verificacion.css")).andExpect(status().isOk());
    }

    @Test
    void codigoVacio_muestraElErrorBajoElCampo() throws Exception {
        mockMvc.perform(get("/verificar").param("codigo", "   ")).andExpect(status().isOk());
        String html = mockMvc.perform(get("/verificar").param("codigo", "   "))
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("Escriba el código de verificación"));
        assertTrue(html.contains("Enter the verification code"));
    }

    @Test
    void codigoEscrito_llevaAlResultadoNormalizado() throws Exception {
        mockMvc.perform(get("/verificar").param("codigo", "  " + CODIGO_APROBADO.toUpperCase() + " "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/verificar/" + CODIGO_APROBADO));
    }

    @Test
    void tituloAprobado_da200ConEstadoValido() throws Exception {
        mockMvc.perform(get("/verificar/{c}", CODIGO_APROBADO)).andExpect(status().isOk());
        String html = cuerpo("/verificar/" + CODIGO_APROBADO);
        assertTrue(html.contains("Título válido"));
        assertTrue(html.contains("Valid degree"));
    }

    @Test
    void tituloAprobado_muestraDatosMinimosConDocumentoEnmascarado() throws Exception {
        String html = cuerpo("/verificar/" + CODIGO_APROBADO);

        assertTrue(html.contains("Graduado APROBADO"));
        assertTrue(html.contains("•-••1-111"));
        assertFalse(html.contains("8-111-111\""), "el documento completo no debe aparecer");
        assertFalse(html.contains(">8-111-111<"), "el documento completo no debe aparecer");
        assertTrue(html.contains("10 de octubre de 2026"));
        assertTrue(html.contains("October 10, 2026"));
        assertTrue(html.contains(CODIGO_APROBADO));
        assertTrue(html.contains("hora de Panamá, UTC−5"));
        assertTrue(html.contains("Panama time, UTC−5"));
        assertFalse(html.contains("style="), "sin estilos en línea");
        assertFalse(html.contains("<script"), "sin scripts");
    }

    @Test
    void laInstitucionSeMuestraConSusAcentosEnP7YP8() throws Exception {
        String nombre = "Universidad Tecnológica de Panamá";
        assertTrue(cuerpo("/verificar").contains(nombre));
        assertTrue(cuerpo("/verificar/" + CODIGO_APROBADO).contains(nombre));
        assertTrue(cuerpo("/verificar/" + CODIGO_INEXISTENTE).contains(nombre));
    }

    @Test
    void ca4_laRespuestaNoTraeDocumentoCompletoNiRevisorNiIdInterno() throws Exception {
        String html = cuerpo("/verificar/" + CODIGO_APROBADO);

        assertFalse(html.contains("8-111-111"), "documento completo");
        assertFalse(html.contains("8111111"), "documento completo sin guiones");
        assertFalse(html.contains(REVISOR), "quien aprobo");
        assertFalse(html.toLowerCase().contains("revisad"), "dato de revision");
        assertFalse(html.contains("/titulos/" + aprobadoId), "enlace con el id interno");
        assertFalse(html.contains("value=\"" + aprobadoId + "\""), "id interno en un campo");
        assertFalse(html.contains("data-id"), "id interno en un atributo");
    }

    @Test
    void ca4_elNoEncontradoNoRevelaDatosDelTitulo() throws Exception {
        for (String codigo : new String[] {CODIGO_PENDIENTE, CODIGO_RECHAZADO}) {
            String html = cuerpo("/verificar/" + codigo);
            assertFalse(html.contains("Graduado "), "nombre del graduado");
            assertFalse(html.contains("Licenciatura"), "nombre del titulo");
            assertFalse(html.contains(codigo), "el codigo consultado no se repite");
        }
    }

    @Test
    void ca6_formularioYResultadoMuestranEspanolEIngles() throws Exception {
        String formulario = cuerpo("/verificar");
        assertTrue(formulario.contains("Código de verificación"));
        assertTrue(formulario.contains("Verification code"));
        assertTrue(formulario.contains("Verificar"));
        assertTrue(formulario.contains("Verify"));

        String resultado = cuerpo("/verificar/" + CODIGO_APROBADO);
        assertTrue(resultado.contains("Título válido"));
        assertTrue(resultado.contains("Valid degree"));
        assertFalse(resultado.contains("<select"), "sin selector de idioma");
    }

    @Test
    void ca8_paginasPublicasSinEstilosNiScriptsEnLinea() throws Exception {
        for (String ruta : new String[] {
                "/verificar", "/verificar/" + CODIGO_APROBADO, "/verificar/" + CODIGO_INEXISTENTE}) {
            String html = cuerpo(ruta);
            assertFalse(html.contains("style="), ruta + " sin style=");
            assertFalse(html.contains("<style"), ruta + " sin <style>");
            assertFalse(html.contains("<script"), ruta + " sin scripts");
            assertFalse(html.contains("onclick="), ruta + " sin manejadores en linea");
        }
    }

    @Test
    void sinSesion_lasPaginasPublicasNoRedirigenAlLogin() throws Exception {
        for (String ruta : new String[] {"/verificar", "/verificar/" + CODIGO_APROBADO}) {
            mockMvc.perform(get(ruta)).andExpect(status().isOk());
        }
        mockMvc.perform(get("/verificar/" + CODIGO_INEXISTENTE))
                .andExpect(status().isNotFound());
    }

    @Test
    void pasaporte_seEnmascaraSinGuiones() throws Exception {
        Graduado graduado = graduadoRepository.save(
                new Graduado(TipoDocumento.PASAPORTE, "PA1234567", "Pasaporte Prueba"));
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Pasaporte Prueba");
        titulo.setNombreTitulo("Maestria en Seguridad Informatica");
        titulo.setFechaRegistro(LocalDate.of(2026, 10, 1));
        titulo.setEstado(EstadoTitulo.APROBADO);
        titulo.setIdentificador("55555555-5555-4555-8555-555555555555");
        titulo.setGraduado(graduado);
        titulo.setFechaRevision(LocalDateTime.of(2026, 10, 12, 9, 0));
        titulo.setRevisadoPor(REVISOR);
        tituloRepository.save(titulo);

        String html = cuerpo("/verificar/55555555-5555-4555-8555-555555555555");
        assertTrue(html.contains("Pasaporte"));
        assertTrue(html.contains("•••••4567"));
        assertFalse(html.contains("PA1234567"));
    }

    @Test
    void codigoEnMayusculasYConEspacios_seNormaliza() throws Exception {
        mockMvc.perform(get("/verificar/{c}", CODIGO_APROBADO.toUpperCase()))
                .andExpect(status().isOk());
    }

    @Test
    void codigoInexistente_da404() throws Exception {
        mockMvc.perform(get("/verificar/{c}", CODIGO_INEXISTENTE))
                .andExpect(status().isNotFound());
    }

    @Test
    void tituloPendienteORechazado_dan404ConElMismoMensaje() throws Exception {
        mockMvc.perform(get("/verificar/{c}", CODIGO_PENDIENTE)).andExpect(status().isNotFound());
        mockMvc.perform(get("/verificar/{c}", CODIGO_RECHAZADO)).andExpect(status().isNotFound());

        String inexistente = cuerpo("/verificar/" + CODIGO_INEXISTENTE);
        assertTrue(inexistente.contains("No encontrado"));
        assertTrue(inexistente.contains("Not found"));
        assertFalse(inexistente.contains("Título válido"));
        assertTrue(cuerpo("/verificar/" + CODIGO_PENDIENTE).contains("No encontrado"));
        assertTrue(cuerpo("/verificar/" + CODIGO_RECHAZADO).contains("No encontrado"));
    }

    @Test
    void codigoConFormatoInvalido_da404() throws Exception {
        mockMvc.perform(get("/verificar/{c}", "no-es-un-uuid")).andExpect(status().isNotFound());
    }

    @Test
    void titulosSigueExigiendoSesion() throws Exception {
        mockMvc.perform(get("/titulos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
