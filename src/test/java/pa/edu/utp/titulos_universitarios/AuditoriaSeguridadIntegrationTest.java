
package pa.edu.utp.titulos_universitarios;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditoriaSeguridadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void aprobadorPuedeConsultarAuditoria() throws Exception {
        mockMvc.perform(get("/auditoria")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void registradorNoPuedeConsultarAuditoria() throws Exception {
        mockMvc.perform(get("/auditoria")
                        .with(user("registrador1").roles("REGISTRADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void monitoreoNoPuedeConsultarAuditoria() throws Exception {
        mockMvc.perform(get("/auditoria")
                        .with(user("prometheus").roles("MONITOREO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioSinSesionEsRedirigidoAlLogin() throws Exception {
        mockMvc.perform(get("/auditoria"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
