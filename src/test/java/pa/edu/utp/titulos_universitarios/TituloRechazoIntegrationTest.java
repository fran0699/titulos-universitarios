
package pa.edu.utp.titulos_universitarios;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;
import pa.edu.utp.titulos_universitarios.repository.GraduadoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TituloRechazoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TituloRepository tituloRepository;

    @Autowired
    private GraduadoRepository graduadoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM eventos_auditoria");
        tituloRepository.deleteAll();
        graduadoRepository.deleteAll();
    }

    private Titulo persistir(String numeroDocumento, EstadoTitulo estado) {
        Graduado graduado = graduadoRepository.save(
                new Graduado(
                        TipoDocumento.CEDULA,
                        numeroDocumento,
                        "Ana Pérez"));

        Titulo titulo = new Titulo(
                "Ana Pérez",
                "Licenciatura en Desarrollo de Software",
                LocalDate.now());

        titulo.setEstado(estado);
        titulo.setGraduado(graduado);

        return tituloRepository.save(titulo);
    }

    @Test
    void aprobadorPuedeAbrirConfirmacionYVerDatosDelTitulo() throws Exception {
        Titulo titulo = persistir("8-111-111", EstadoTitulo.PENDIENTE);

        mockMvc.perform(get("/titulos/{id}/rechazar", titulo.getId())
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(view().name("titulos/confirmar-rechazo"))
                .andExpect(model().attributeExists("titulo"))
                .andExpect(content().string(containsString("Ana Pérez")))
                .andExpect(content().string(containsString(
                        "Licenciatura en Desarrollo de Software")))
                .andExpect(content().string(containsString(
                        "¿Rechazar el título «Licenciatura en Desarrollo de Software» de Ana Pérez?")))
                .andExpect(content().string(containsString(
                        "Esta acción no se puede deshacer.")));

        Titulo recargado = tituloRepository.findById(titulo.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(
                EstadoTitulo.PENDIENTE, recargado.getEstado());
    }

    @Test
    void registradorNoPuedeAbrirConfirmacion() throws Exception {
        Titulo titulo = persistir("8-222-222", EstadoTitulo.PENDIENTE);

        mockMvc.perform(get("/titulos/{id}/rechazar", titulo.getId())
                        .with(user("registrador1").roles("REGISTRADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioTecnicoNoPuedeAbrirConfirmacion() throws Exception {
        Titulo titulo = persistir("8-333-333", EstadoTitulo.PENDIENTE);

        mockMvc.perform(get("/titulos/{id}/rechazar", titulo.getId())
                        .with(user("prometheus").roles("MONITOREO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioSinSesionEsEnviadoAlLogin() throws Exception {
        Titulo titulo = persistir("8-444-444", EstadoTitulo.PENDIENTE);

        mockMvc.perform(get("/titulos/{id}/rechazar", titulo.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void tituloInexistenteRegresaALaBandejaConMensaje() throws Exception {
        mockMvc.perform(get("/titulos/{id}/rechazar", 999999L)
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulos/pendientes"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void tituloQueYaNoEstaPendienteRegresaALaBandejaConMensaje() throws Exception {
        Titulo titulo = persistir("8-555-555", EstadoTitulo.APROBADO);

        mockMvc.perform(get("/titulos/{id}/rechazar", titulo.getId())
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulos/pendientes"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void identificadorInvalidoRegresaALaBandejaConMensaje() throws Exception {
        mockMvc.perform(get("/titulos/abc/rechazar")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulos/pendientes"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void bandejaMuestraRechazarComoEnlaceConEstiloDePeligro() throws Exception {
        Titulo titulo = persistir("8-666-666", EstadoTitulo.PENDIENTE);

        mockMvc.perform(get("/titulos/pendientes")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("button button-danger")))
                .andExpect(content().string(containsString(
                        "/titulos/" + titulo.getId() + "/rechazar")));
    }
}
