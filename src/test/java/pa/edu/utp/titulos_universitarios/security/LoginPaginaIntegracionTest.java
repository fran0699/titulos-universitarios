package pa.edu.utp.titulos_universitarios.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * T-1.5.2 (HU-1.5): pagina de inicio de sesion propia (P1).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginPaginaIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    // CA1 y riesgo "nadie puede entrar": /login es publica y trae los elementos de P1.
    @Test
    void loginSeMuestraSinSesionConLosElementosDeP1() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Títulos Universitarios")))
                .andExpect(content().string(containsString("Universidad Tecnológica de Panamá")))
                .andExpect(content().string(containsString(">Usuario</label>")))
                .andExpect(content().string(containsString(">Contraseña</label>")))
                .andExpect(content().string(containsString("Ingresar")))
                .andExpect(content().string(containsString("href=\"/verificar\"")))
                .andExpect(content().string(containsString("Verificar un título")))
                // Sin mensajes cuando no hay ?error ni ?logout.
                .andExpect(content().string(not(containsString("Usuario o contraseña incorrectos"))))
                .andExpect(content().string(not(containsString("Cerraste sesión correctamente"))))
                // Sin el perfil demo no hay franja ni botones de prueba.
                .andExpect(content().string(not(containsString("Modo demostración"))))
                .andExpect(content().string(not(containsString("usuario=registrador"))))
                // Pública y sin encabezado ni menú interno.
                .andExpect(content().string(not(containsString("app-header"))));
    }

    // La hoja de estilos de la pagina tambien es publica (si no, el login saldria sin estilos).
    @Test
    void laHojaDeEstilosEsPublica() throws Exception {
        mockMvc.perform(get("/css/titulos.css"))
                .andExpect(status().isOk());
    }

    // CA2: usuario inexistente y contraseña incorrecta dan el mismo resultado.
    @Test
    void usuarioInexistenteYContrasenaIncorrectaDanLaMismaRespuesta() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "no-existe")
                        .param("password", "cualquiera")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login?error"));
        mockMvc.perform(post("/login")
                        .param("username", "registrador1")
                        .param("password", "incorrecta")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void conErrorMuestraElMensajeFijoSinDecirCualDatoFallo() throws Exception {
        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usuario o contraseña incorrectos")))
                .andExpect(content().string(not(containsString("Cerraste sesión correctamente"))));
    }

    // CA3: despues de cerrar sesion se muestra el aviso.
    @Test
    void cerrarSesionLlevaAlLoginConElAvisoDeSesionCerrada() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(redirectedUrl("/login?logout"));
        mockMvc.perform(get("/login").param("logout", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cerraste sesión correctamente")));
    }

    // Validaciones: nunca se muestra un texto que llegue en la direccion.
    @Test
    void nuncaMuestraUnTextoQueLlegueEnLaDireccion() throws Exception {
        mockMvc.perform(get("/login")
                        .param("error", "<b>hackeado</b>")
                        .param("logout", "texto-propio"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("hackeado"))))
                .andExpect(content().string(not(containsString("texto-propio"))));
    }

    // CA4: un usuario del personal que inicia sesion llega a /titulos.
    @Test
    void alIniciarSesionElPersonalLlegaATitulos() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "registrador1")
                        .param("password", "clave1234")
                        .with(csrf()))
                .andExpect(redirectedUrl("/"));
        mockMvc.perform(get("/")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user("registrador1")
                                .roles("REGISTRADOR")))
                .andExpect(redirectedUrl("/titulos"));
    }

    // CA5: sin estilos ni scripts en linea.
    @Test
    void laPaginaNoTieneEstilosNiScriptsEnLinea() throws Exception {
        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(content().string(not(containsString(" style="))))
                .andExpect(content().string(not(containsString("<style"))))
                .andExpect(content().string(not(containsString("<script"))))
                .andExpect(content().string(containsString("/css/titulos.css")));
    }

    // CA6: POST con token CSRF y autocomplete en los dos campos.
    @Test
    void elFormularioUsaPostConCsrfYAutocomplete() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("action=\"/login\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("autocomplete=\"username\"")))
                .andExpect(content().string(containsString("autocomplete=\"current-password\"")));
    }

    // CA6: el POST sin token CSRF se rechaza.
    @Test
    void elPostSinTokenCsrfSeRechaza() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "registrador1")
                        .param("password", "clave1234"))
                .andExpect(status().isForbidden());
    }
}
