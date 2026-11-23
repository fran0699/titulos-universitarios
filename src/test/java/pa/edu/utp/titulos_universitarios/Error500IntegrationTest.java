
package pa.edu.utp.titulos_universitarios;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.servlet.RequestDispatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;
import pa.edu.utp.titulos_universitarios.repository.GraduadoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Error500IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TituloRepository tituloRepository;

    @Autowired
    private GraduadoRepository graduadoRepository;

    @MockitoBean
    private CredencialPdfService credencialPdfService;

    private Long aprobadoId;
    private Long pendienteId;

    @BeforeEach
    void prepararTitulo() {
        tituloRepository.deleteAll();
        graduadoRepository.deleteAll();

        Graduado graduado = graduadoRepository.save(
                new Graduado(
                        TipoDocumento.CEDULA,
                        "8-123-456",
                        "Ana Perez"
                )
        );

        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Ana Perez");
        titulo.setNombreTitulo("Licenciatura en Ingenieria de Software");
        titulo.setFechaRegistro(LocalDate.of(2025, 1, 15));
        titulo.setFechaRevision(LocalDateTime.of(2026, 10, 22, 9, 30));
        titulo.setEstado(EstadoTitulo.APROBADO);
        titulo.setIdentificador("7d972866-4d54-4fcb-9f31-cf430ef2a264");
        titulo.setGraduado(graduado);

        aprobadoId = tituloRepository.save(titulo).getId();

        Titulo tituloPendiente = new Titulo();
        tituloPendiente.setNombreGraduado("Ana Perez");
        tituloPendiente.setNombreTitulo("Licenciatura en Ingenieria de Software");
        tituloPendiente.setFechaRegistro(LocalDate.of(2025, 1, 15));
        tituloPendiente.setEstado(EstadoTitulo.PENDIENTE);
        tituloPendiente.setIdentificador("pendiente-123");
        tituloPendiente.setGraduado(graduado);

        pendienteId = tituloRepository.save(tituloPendiente).getId();
    }

    // Comprueba que el fallo al generar el PDF devuelve HTTP 500.
    @Test
    void errorAlGenerarPdfDevuelveEstado500() throws Exception {

        doThrow(new IOException("FALLO_SIMULADO"))
                .when(credencialPdfService)
                .generar(any(Titulo.class));

        mockMvc.perform(
                        get("/titulos/{id}/credencial", aprobadoId)
                                .with(user("aprobador1").roles("APROBADOR"))
                )
                .andExpect(status().isInternalServerError());
    }

    // Comprueba que la plantilla personalizada 500.html se renderiza.
    @Test
    void paginaPersonalizada500SeRenderizaSinDetallesTecnicos() throws Exception {

        mockMvc.perform(
                        get("/error")
                                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
                                .requestAttr(
                                        RequestDispatcher.ERROR_REQUEST_URI,
                                        "/titulos/" + aprobadoId + "/credencial"
                                )
                                .header("Accept", "text/html")
                                .with(user("aprobador1").roles("APROBADOR"))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("ERROR 500")))
                .andExpect(content().string(
                        containsString("Ha ocurrido un error inesperado")
                ))
                .andExpect(content().string(not(containsString("FALLO_SIMULADO"))))
                .andExpect(content().string(not(containsString("java."))))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("at pa.edu.utp"))))
                .andExpect(content().string(not(containsString("Apache Tomcat"))));
    }

    // Comprueba que un título pendiente no permite descargar su credencial.
    @Test
    void credencialDeTituloPendienteDevuelveEstado404() throws Exception {

        mockMvc.perform(
                        get("/titulos/{id}/credencial", pendienteId)
                                .with(user("aprobador1").roles("APROBADOR"))
                )
                .andExpect(status().isNotFound());
    }

    // Comprueba que una ruta inexistente devuelve HTTP 404.
    @Test
    void rutaInexistenteDevuelveEstado404() throws Exception {

        mockMvc.perform(
                        get("/ruta-que-no-existe")
                                .with(user("aprobador1").roles("APROBADOR"))
                )
                .andExpect(status().isNotFound());
    }

    // Comprueba que un registrador no puede acceder a títulos pendientes.
    @Test
    void registradorNoPuedeAccederATitulosPendientesDevuelve403() throws Exception {

        mockMvc.perform(
                        get("/titulos/pendientes")
                                .with(user("registrador1").roles("REGISTRADOR"))
                )
                .andExpect(status().isForbidden());
    }

    // Comprueba que la página 404 sin sesión no muestra el menú interno.
    @Test
    void pagina404SinSesionMuestraOpcionIniciarSesion() throws Exception {

        mockMvc.perform(
                        get("/error")
                                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                                .requestAttr(
                                        RequestDispatcher.ERROR_REQUEST_URI,
                                        "/ruta-que-no-existe"
                                )
                                .header("Accept", "text/html")
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("ERROR 404")))
                .andExpect(content().string(containsString("Iniciar sesión")))
                .andExpect(content().string(not(containsString("Volver a Títulos"))));
    }

    // Comprueba que MONITOREO recibe 403 sin enlaces a pantallas internas.
    @Test
    void usuarioMonitoreoRecibe403SinEnlacesInternos() throws Exception {

        // Primero comprobamos que el acceso está prohibido.
        mockMvc.perform(
                        get("/titulos")
                                .with(user("prometheus").roles("MONITOREO"))
                )
                .andExpect(status().isForbidden());

        // Después comprobamos el contenido de la página personalizada 403.
        mockMvc.perform(
                        get("/error")
                                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403)
                                .requestAttr(
                                        RequestDispatcher.ERROR_REQUEST_URI,
                                        "/titulos"
                                )
                                .header("Accept", "text/html")
                                .with(user("prometheus").roles("MONITOREO"))
                )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("ERROR 403")))
                .andExpect(content().string(
                        containsString("No tiene permiso para ver esta página")
                ))
                .andExpect(content().string(not(containsString("Volver a Títulos"))))
                .andExpect(content().string(not(containsString("href=\"/titulos\""))));
    }
}
