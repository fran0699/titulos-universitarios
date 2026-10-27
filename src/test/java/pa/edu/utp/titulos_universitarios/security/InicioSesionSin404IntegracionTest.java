package pa.edu.utp.titulos_universitarios.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * T-4.11.2 (HU-4.11): al iniciar sesión, la aplicación solo vuelve a una
 * página que el navegador pidió como página (HTML). Nunca vuelve a un archivo
 * que el navegador pidió por su cuenta, como el de Chrome en /.well-known/.
 *
 * Cada prueba simula un navegador: todas sus peticiones usan la misma sesión
 * (MockHttpSession), como la cookie JSESSIONID.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InicioSesionSin404IntegracionTest {

    /** Lo que pide Chrome por su cuenta cuando F12 está abierto. */
    private static final String ARCHIVO_DE_CHROME =
            "/.well-known/appspecific/com.chrome.devtools.json";

    /** Lo que envía Chrome cuando la persona abre una página. */
    private static final String ACEPTA_PAGINA =
            "text/html,application/xhtml+xml,application/xml;q=0.9,"
                    + "image/avif,image/webp,image/apng,*/*;q=0.8";

    /** Lo que envía una petición automática que acepta cualquier formato. */
    private static final String ACEPTA_CUALQUIERA = "*/*";

    @Autowired
    private MockMvc mockMvc;

    // CA1: el archivo de Chrome no queda como página a la que volver.
    @Test
    void elArchivoDeChromeNoQuedaComoPaginaAVolver() throws Exception {
        MockHttpSession navegador = new MockHttpSession();
        pedirSinSesion(navegador, ARCHIVO_DE_CHROME, ACEPTA_CUALQUIERA);

        iniciarSesion(navegador, "registrador1")
                .andExpect(redirectedUrl("/"));

        // "/" lleva a la lista de títulos (HomeController).
        mockMvc.perform(get("/").session(navegador))
                .andExpect(redirectedUrl("/titulos"));
    }

    // CA1 y CA3: el archivo de Chrome no reemplaza la página que se abrió antes.
    @Test
    void elArchivoDeChromeNoReemplazaLaPaginaQueSeAbrio() throws Exception {
        MockHttpSession navegador = new MockHttpSession();
        pedirSinSesion(navegador, "/titulos/pendientes", ACEPTA_PAGINA);
        pedirSinSesion(navegador, ARCHIVO_DE_CHROME, ACEPTA_CUALQUIERA);

        iniciarSesion(navegador, "aprobador1")
                .andExpect(header().string("Location", containsString("/titulos/pendientes")));
    }

    // Regla de negocio: nunca se vuelve a /.well-known/, aunque se pida como página.
    @Test
    void nuncaSeVuelveAUnaDireccionDeWellKnown() throws Exception {
        MockHttpSession navegador = new MockHttpSession();
        pedirSinSesion(navegador, "/.well-known/security.txt", ACEPTA_PAGINA);

        iniciarSesion(navegador, "registrador1")
                .andExpect(redirectedUrl("/"));
    }

    // CA2: lo que no es una página (imagen, JSON, cualquier formato) no se recuerda.
    @ParameterizedTest
    @ValueSource(strings = {"image/png", "application/json", ACEPTA_CUALQUIERA})
    void unaPeticionQueNoEsUnaPaginaNoQuedaComoPaginaAVolver(String acepta) throws Exception {
        MockHttpSession navegador = new MockHttpSession();
        pedirSinSesion(navegador, "/titulos", acepta);

        iniciarSesion(navegador, "registrador1")
                .andExpect(redirectedUrl("/"));
    }

    // CA3: una página válida pedida sin sesión sí se recuerda.
    @Test
    void unaPaginaValidaSiQuedaComoPaginaAVolver() throws Exception {
        MockHttpSession navegador = new MockHttpSession();
        pedirSinSesion(navegador, "/titulos/pendientes", ACEPTA_PAGINA);

        iniciarSesion(navegador, "aprobador1")
                .andExpect(header().string("Location", containsString("/titulos/pendientes")));
    }

    // CA4: el usuario técnico sigue recibiendo 403 en las pantallas.
    @ParameterizedTest
    @ValueSource(strings = {"/titulos", "/", "/no-existe"})
    void elUsuarioTecnicoSigueRecibiendo403EnLasPantallas(String ruta) throws Exception {
        mockMvc.perform(get(ruta).with(user("prometheus").roles("MONITOREO")))
                .andExpect(status().isForbidden());
    }

    // CA6: el ícono de la pestaña existe y es público, con y sin sesión.
    @Test
    void elIconoDeLaPestanaSeEntregaConYSinSesion() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/favicon.ico").with(user("registrador1").roles("REGISTRADOR")))
                .andExpect(status().isOk());
    }

    /** El navegador pide una dirección sin haber iniciado sesión. */
    private void pedirSinSesion(MockHttpSession navegador, String ruta, String acepta)
            throws Exception {
        mockMvc.perform(get(ruta).session(navegador).header("Accept", acepta))
                .andExpect(redirectedUrl("/login"));
    }

    /** La persona inicia sesión con el formulario, en el mismo navegador. */
    private ResultActions iniciarSesion(MockHttpSession navegador, String usuario)
            throws Exception {
        return mockMvc.perform(post("/login")
                .session(navegador)
                .param("username", usuario)
                .param("password", "clave1234")
                .with(csrf()));
    }
}