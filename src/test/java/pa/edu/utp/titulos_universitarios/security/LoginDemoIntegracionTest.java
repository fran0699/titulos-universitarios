package pa.edu.utp.titulos_universitarios.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * P1 con el perfil "demo": franja «Modo demostracion» y botones de acceso
 * rapido que son enlaces (sin JavaScript) y devuelven el formulario lleno.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo"})
class LoginDemoIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void conPerfilDemoMuestraLaFranjaYLosDosBotones() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Modo demostración")))
                .andExpect(content().string(containsString("Entrar con un usuario de prueba")))
                .andExpect(content().string(containsString("href=\"/login?usuario=registrador\"")))
                .andExpect(content().string(containsString("href=\"/login?usuario=aprobador\"")))
                .andExpect(content().string(not(containsString("<script"))))
                .andExpect(content().string(not(containsString(" style="))));
    }

    @Test
    void elBotonDevuelveElFormularioYaLleno() throws Exception {
        mockMvc.perform(get("/login").param("usuario", "aprobador"))
                .andExpect(content().string(containsString("value=\"aprobador1\"")))
                .andExpect(content().string(containsString("value=\"clave1234\"")));
    }

    @Test
    void unUsuarioQueNoEstaEnLaListaFijaNoSeCopia() throws Exception {
        mockMvc.perform(get("/login").param("usuario", "<b>otro</b>"))
                .andExpect(content().string(not(containsString("otro"))))
                .andExpect(content().string(not(containsString("value=\"clave1234\""))));
    }
}
