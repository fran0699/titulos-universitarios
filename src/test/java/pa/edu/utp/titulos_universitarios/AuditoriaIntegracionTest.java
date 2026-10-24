package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;
import pa.edu.utp.titulos_universitarios.repository.GraduadoRepository;

/**
 * T-3.3.3 (HU-3.3): prueba de integración con H2. Confirma, contra la tabla
 * real eventos_auditoria (consultada con SQL directo, como se verificaría en
 * pgAdmin), que cada acción deja su evento, que una acción fallida no deja
 * ninguno, y que todo ocurre en la misma transacción: si el evento no se
 * puede guardar, la acción se revierte (el título sigue PENDIENTE).
 */
@SpringBootTest
@ActiveProfiles("test")
class AuditoriaIntegracionTest {

    @Autowired
    private TituloService tituloService;

    @Autowired
    private TituloRepository tituloRepository;

    @Autowired
    private GraduadoRepository graduadoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private AuditoriaService auditoriaService;

    @BeforeEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM eventos_auditoria");
        tituloRepository.deleteAll();
        graduadoRepository.deleteAll();
    }

    private int contarEventos(String accion) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM eventos_auditoria WHERE accion = ?",
                Integer.class, accion);
    }

    private int totalEventos() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM eventos_auditoria", Integer.class);
    }

    private String usuarioDelEvento(String accion) {
        return jdbcTemplate.queryForObject(
                "SELECT usuario FROM eventos_auditoria WHERE accion = ?",
                String.class, accion);
    }

    private Titulo persistir(String numeroDocumento, EstadoTitulo estado) {
        Graduado graduado = graduadoRepository.save(
                new Graduado(TipoDocumento.CEDULA, numeroDocumento, "Ana Pérez"));
        Titulo titulo = new Titulo(
                "Ana Pérez",
                "Licenciatura en Desarrollo de Software",
                LocalDate.now());
        titulo.setEstado(estado);
        titulo.setGraduado(graduado);
        return tituloRepository.save(titulo);
    }

    @Test
    void registrar_persisteUnEventoRegistrarConElUsuarioDeLaSesion() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Ana Pérez");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");
        Graduado graduado = new Graduado();
        graduado.setTipoDocumento(TipoDocumento.CEDULA);
        graduado.setNumeroDocumento("8-123-456");
        titulo.setGraduado(graduado);

        tituloService.registrar(titulo, "registrador1");

        assertEquals(1, contarEventos("REGISTRAR"));
        assertEquals("registrador1", usuarioDelEvento("REGISTRAR"));
    }

    @Test
    void aprobar_persisteUnEventoAprobarConElUsuarioDeLaSesion() {
        Titulo titulo = persistir("8-111-111", EstadoTitulo.PENDIENTE);

        tituloService.aprobar(titulo.getId(), "aprobador1");

        assertEquals(1, contarEventos("APROBAR"));
        assertEquals("aprobador1", usuarioDelEvento("APROBAR"));
    }

    @Test
    void rechazar_persisteUnEventoRechazarConElUsuarioDeLaSesion() {
        Titulo titulo = persistir("8-222-222", EstadoTitulo.PENDIENTE);

        tituloService.rechazar(titulo.getId(), "aprobador1");

        assertEquals(1, contarEventos("RECHAZAR"));
        assertEquals("aprobador1", usuarioDelEvento("RECHAZAR"));
    }

    @Test
    void accionQueFalla_noDejaNingunEvento() {
        // Un título que ya no está PENDIENTE no se puede aprobar.
        Titulo titulo = persistir("8-333-333", EstadoTitulo.APROBADO);

        assertThrows(
                IllegalStateException.class,
                () -> tituloService.aprobar(titulo.getId(), "aprobador1"));

        assertEquals(0, totalEventos());
    }

    @Test
    void siElEventoFalla_laAccionSeRevierteYElTituloSiguePendiente() {
        Titulo titulo = persistir("8-444-444", EstadoTitulo.PENDIENTE);
        Long id = titulo.getId();

        // El guardado del evento falla dentro de la transacción de aprobar.
        doThrow(new RuntimeException("falla al guardar el evento"))
                .when(auditoriaService)
                .registrarEvento(any(), any(), any(), any());

        assertThrows(
                RuntimeException.class,
                () -> tituloService.aprobar(id, "aprobador1"));

        // La transacción se revierte: el título sigue PENDIENTE, sin
        // identificador, y no quedó ningún evento (CA3).
        Titulo recargado = tituloRepository.findById(id).orElseThrow();
        assertEquals(EstadoTitulo.PENDIENTE, recargado.getEstado());
        assertNull(recargado.getIdentificador());
        assertEquals(0, totalEventos());
    }
}
