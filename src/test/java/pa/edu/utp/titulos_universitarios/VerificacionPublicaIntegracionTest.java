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
                new Graduado(TipoDocumento.CEDULA, documento, "Graduado " + documento));
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado(graduado.getNombreCompleto());
        titulo.setNombreTitulo("Licenciatura en Ingenieria de Software");
        titulo.setFechaRegistro(LocalDate.of(2026, 10, 1));
        titulo.setEstado(estado);
        titulo.setIdentificador(identificador);
        titulo.setGraduado(graduado);
        if (estado == EstadoTitulo.APROBADO) {
            titulo.setFechaRevision(LocalDateTime.of(2026, 10, 10, 9, 0));
        }
        tituloRepository.save(titulo);
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
