package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;
import pa.edu.utp.titulos_universitarios.entity.EventoAuditoria;
import pa.edu.utp.titulos_universitarios.repository.EventoAuditoriaRepository;

/**
 * T-3.4.3 (HU-3.4): la consulta devuelve los eventos correctos con H2.
 *
 * Cada prueba guarda sus propios eventos en el año 2030, con usuarios que
 * empiezan con "hu34-", para no mezclarse con los eventos de otras pruebas.
 * Al terminar, @Transactional deshace todo lo guardado.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditoriaConsultaFiltrosIntegrationTest {

    private static final LocalDate DIA = LocalDate.of(2030, 3, 10);

    @Autowired
    private AuditoriaConsultaService servicio;

    @Autowired
    private EventoAuditoriaRepository repositorio;

    // CA4: los filtros se combinan; solo aparecen los eventos que cumplen todos.
    @Test
    void filtrosCombinadosDevuelvenSoloLosEventosQueCumplenTodos() {
        guardar("e1", DIA.atTime(9, 0), AccionAuditoria.APROBAR, "hu34-aprobador");
        guardar("e2", DIA.atTime(10, 0), AccionAuditoria.RECHAZAR, "hu34-aprobador");
        guardar("e3", DIA.atTime(11, 0), AccionAuditoria.APROBAR, "hu34-registrador");
        guardar("e4", DIA.plusDays(2).atTime(9, 0), AccionAuditoria.APROBAR, "hu34-aprobador");

        Page<AuditoriaFila> resultado =
                servicio.buscar(DIA, DIA, AccionAuditoria.APROBAR, "hu34-aprob", 1);

        assertEquals(List.of("e1"), detalles(resultado));
    }

    // Valor acordado: «hasta» incluye todo ese día.
    @Test
    void hastaIncluyeTodoElDia() {
        guardar("ultimo-minuto", DIA.atTime(23, 59, 59), AccionAuditoria.REGISTRAR, "hu34-registrador");
        guardar("dia-siguiente", DIA.plusDays(1).atStartOfDay(), AccionAuditoria.REGISTRAR, "hu34-registrador");

        Page<AuditoriaFila> resultado = servicio.buscar(DIA, DIA, null, null, 1);

        assertEquals(List.of("ultimo-minuto"), detalles(resultado));
    }

    // Valor acordado: el usuario se busca por texto contenido, sin distinguir mayúsculas.
    @Test
    void usuarioSeBuscaPorTextoContenidoSinDistinguirMayusculas() {
        guardar("aprobador", DIA.atTime(9, 0), AccionAuditoria.APROBAR, "hu34-aprobador");
        guardar("registrador", DIA.atTime(10, 0), AccionAuditoria.REGISTRAR, "hu34-registrador");

        Page<AuditoriaFila> resultado = servicio.buscar(DIA, DIA, null, "APROB", 1);

        assertEquals(List.of("aprobador"), detalles(resultado));
    }

    // Un filtro vacío no limita la búsqueda (aquí, las fechas).
    @Test
    void sinFechasNoSeLimitaLaBusqueda() {
        guardar("antiguo", LocalDateTime.of(2030, 1, 1, 8, 0), AccionAuditoria.REGISTRAR, "hu34-unico");
        guardar("reciente", LocalDateTime.of(2030, 12, 31, 8, 0), AccionAuditoria.APROBAR, "hu34-unico");

        Page<AuditoriaFila> resultado = servicio.buscar(null, null, null, "hu34-unico", 1);

        assertEquals(List.of("reciente", "antiguo"), detalles(resultado));
    }

    // CA3: de lo más reciente a lo más antiguo.
    @Test
    void ordenDeLoMasRecienteALoMasAntiguo() {
        guardar("ocho", DIA.atTime(8, 0), AccionAuditoria.REGISTRAR, "hu34-registrador");
        guardar("doce", DIA.atTime(12, 0), AccionAuditoria.APROBAR, "hu34-aprobador");
        guardar("diez", DIA.atTime(10, 0), AccionAuditoria.RECHAZAR, "hu34-aprobador");

        Page<AuditoriaFila> resultado = servicio.buscar(DIA, DIA, null, null, 1);

        assertEquals(List.of("doce", "diez", "ocho"), detalles(resultado));
    }

    // CA6: 20 eventos por página.
    @Test
    void veinteEventosPorPagina() {
        for (int minuto = 0; minuto < 25; minuto++) {
            guardar("evento-" + minuto, DIA.atTime(9, minuto),
                    AccionAuditoria.REGISTRAR, "hu34-registrador");
        }

        Page<AuditoriaFila> primera = servicio.buscar(DIA, DIA, null, null, 1);
        Page<AuditoriaFila> segunda = servicio.buscar(DIA, DIA, null, null, 2);

        assertEquals(20, primera.getNumberOfElements());
        assertEquals(25, primera.getTotalElements());
        assertEquals(2, primera.getTotalPages());
        assertEquals(5, segunda.getNumberOfElements());
    }

    // CA7 de HU-3.4 y CA4 de HU-3.3: el repositorio no tiene ninguna forma de
    // editar ni borrar eventos. getMethods() incluye los métodos heredados.
    @Test
    void elRepositorioNoTieneMetodosParaEditarNiBorrar() {
        List<String> prohibidos = Arrays.stream(EventoAuditoriaRepository.class.getMethods())
                .map(Method::getName)
                .filter(nombre -> nombre.startsWith("delete")
                        || nombre.startsWith("update")
                        || nombre.startsWith("remove"))
                .toList();

        assertTrue(prohibidos.isEmpty(), "Métodos para editar o borrar: " + prohibidos);
    }

    private void guardar(String detalle, LocalDateTime fechaHora,
                         AccionAuditoria accion, String usuario) {
        repositorio.save(new EventoAuditoria(fechaHora, accion, usuario, 999_000L, detalle));
    }

    private List<String> detalles(Page<AuditoriaFila> pagina) {
        return pagina.getContent().stream()
                .map(AuditoriaFila::detalle)
                .toList();
    }
}
