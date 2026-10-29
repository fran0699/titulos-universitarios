package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;
import pa.edu.utp.titulos_universitarios.repository.EventoAuditoriaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditoriaConsultaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private AuditoriaConsultaService auditoriaService;

    @MockitoSpyBean
    private EventoAuditoriaRepository auditoriaRepository;

    @Test
    void accionInvalidaMuestraErrorYNoConsultaElRepositorio()
            throws Exception {

        MvcResult resultado = mockMvc.perform(get("/auditoria")
                        .param("accion", "BORRAR")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andReturn();

        String html = resultado.getResponse().getContentAsString();

        assertTrue(html.contains("Selecciona una acción válida."));
        verifyNoInteractions(auditoriaRepository);
    }

    @Test
    void fechaInvalidaMuestraErrorYNoConsultaElRepositorio()
            throws Exception {

        MvcResult resultado = mockMvc.perform(get("/auditoria")
                        .param("desde", "abc")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andReturn();

        String html = resultado.getResponse().getContentAsString();

        assertTrue(html.contains("Introduce una fecha válida."));
        verifyNoInteractions(auditoriaRepository);
    }

    @Test
    void fechasInvertidasMuestranErrorYNoConsultanElRepositorio()
            throws Exception {

        MvcResult resultado = mockMvc.perform(get("/auditoria")
                        .param("desde", "2026-10-20")
                        .param("hasta", "2026-10-10")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andReturn();

        String html = resultado.getResponse().getContentAsString();

        assertTrue(html.contains(
                "La fecha inicial no puede ser posterior a la fecha final."));
        verifyNoInteractions(auditoriaRepository);
    }

    @Test
    void usuarioDemasiadoCortoMuestraErrorYNoConsultaElRepositorio()
            throws Exception {

        MvcResult resultado = mockMvc.perform(get("/auditoria")
                        .param("usuario", "ab")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andReturn();

        String html = resultado.getResponse().getContentAsString();

        assertTrue(html.contains(
                "El usuario debe tener entre 3 y 50 caracteres."));
        verifyNoInteractions(auditoriaRepository);
    }

    @Test
    void filtrosCombinadosSeEnvíanCorrectamenteAlServicio()
            throws Exception {

        mockMvc.perform(get("/auditoria")
                        .param("desde", "2026-10-01")
                        .param("hasta", "2026-10-25")
                        .param("accion", "APROBAR")
                        .param("usuario", "aprobador1")
                        .param("pagina", "1")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk());

        verify(auditoriaService).buscar(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 25),
                AccionAuditoria.APROBAR,
                "aprobador1",
                1
        );
    }
}
