package pa.edu.utp.titulos_universitarios;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;
import pa.edu.utp.titulos_universitarios.repository.GraduadoRepository;

/**
 * T-1.8.2 (HU-1.8): la lista de títulos (P2) con búsqueda, filtro por estado
 * y paginación.
 *
 * Cada prueba usa palabras inventadas (qz...) para no mezclarse con otros
 * datos. Al terminar, @Transactional deshace todo lo guardado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TituloListaBusquedaIntegrationTest {

    private static final LocalDate DIA = LocalDate.of(2030, 3, 10);

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private TituloRepository titulos;

    @Autowired
    private GraduadoRepository graduados;

    // CA1: la búsqueda muestra solo los títulos que coinciden.
    @Test
    void busquedaMuestraSoloLosTitulosQueCoinciden() throws Exception {
        guardar("Qzlista Ana", "HU18L001", EstadoTitulo.PENDIENTE, DIA);
        guardar("Otra Persona", "HU18L002", EstadoTitulo.PENDIENTE, DIA);

        mockMvc.perform(get("/titulos").param("q", "qzlista")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Qzlista Ana")))
                .andExpect(content().string(not(containsString("Otra Persona"))));
    }

    // CA2: el filtro por estado se combina con la búsqueda.
    @Test
    void filtroPorEstadoSeCombinaConLaBusqueda() throws Exception {
        guardar("Qzfiltro Aprobada", "HU18F001", EstadoTitulo.APROBADO, DIA);
        guardar("Qzfiltro Pendiente", "HU18F002", EstadoTitulo.PENDIENTE, DIA);

        mockMvc.perform(get("/titulos")
                        .param("q", "qzfiltro")
                        .param("estado", "APROBADO")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Qzfiltro Aprobada")))
                .andExpect(content().string(not(containsString("Qzfiltro Pendiente"))));
    }

    // CA2: el filtro ofrece «Todos» y los estados de la aplicación.
    @Test
    void elFiltroOfreceTodosYCadaEstado() throws Exception {
        mockMvc.perform(get("/titulos")
                        .with(user("registrador1").roles("REGISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todos")))
                .andExpect(content().string(containsString("PENDIENTE")))
                .andExpect(content().string(containsString("APROBADO")))
                .andExpect(content().string(containsString("RECHAZADO")));
    }

    // CA3: 20 por página, «Mostrando X–Y de N», «Siguiente» y «Anterior»,
    // y la búsqueda se conserva al cambiar de página.
    @Test
    void paginacionMuestraVeinteConEnlacesQueConservanLaBusqueda() throws Exception {
        for (int i = 1; i <= 25; i++) {
            guardar(String.format("Qzpag %02d", i),
                    String.format("HU18P%03d", i),
                    EstadoTitulo.PENDIENTE,
                    DIA.plusDays(i));
        }

        mockMvc.perform(get("/titulos").param("q", "qzpag")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mostrando 1–20 de 25 títulos")))
                .andExpect(content().string(containsString("Siguiente")))
                .andExpect(content().string(containsString("q=qzpag")))
                .andExpect(content().string(not(containsString("Anterior"))));

        mockMvc.perform(get("/titulos")
                        .param("q", "qzpag")
                        .param("pagina", "2")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mostrando 21–25 de 25 títulos")))
                .andExpect(content().string(containsString("Anterior")))
                .andExpect(content().string(not(containsString("Siguiente"))));
    }

    // CA5: sin resultados con búsqueda.
    @Test
    void sinResultadosMuestraElMensajeDeBusqueda() throws Exception {
        mockMvc.perform(get("/titulos").param("q", "qzninguno")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No hay títulos con esa búsqueda")));
    }

    // CA5: sin títulos y sin búsqueda.
    @Test
    void sinTitulosNiBusquedaMuestraElMensajeDeListaVacia() throws Exception {
        titulos.deleteAll();
        graduados.deleteAll();

        mockMvc.perform(get("/titulos")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía no hay títulos registrados")));
    }

    // CA4: búsqueda de 1 carácter: error junto al campo y sin consultar la base de datos.
    @Test
    void busquedaDeUnCaracterMuestraErrorSinConsultarLaBaseDeDatos() throws Exception {
        mockMvc.perform(get("/titulos").param("q", "a")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "La búsqueda debe tener entre 2 y 100 caracteres.")));

        verify(titulos, never()).buscar(any(), any(), any());
    }

    // CA4: búsqueda de más de 100 caracteres.
    @Test
    void busquedaDeMasDeCienCaracteresMuestraErrorSinConsultarLaBaseDeDatos() throws Exception {
        mockMvc.perform(get("/titulos").param("q", "a".repeat(101))
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "La búsqueda debe tener entre 2 y 100 caracteres.")));

        verify(titulos, never()).buscar(any(), any(), any());
    }

    // CA4: un estado que no está en la lista.
    @Test
    void estadoQueNoEstaEnLaListaMuestraErrorSinConsultarLaBaseDeDatos() throws Exception {
        mockMvc.perform(get("/titulos").param("estado", "INVENTADO")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Elige un estado de la lista.")));

        verify(titulos, never()).buscar(any(), any(), any());
    }

    // CA4: una página menor que 1 (o que no es un número).
    @Test
    void paginaMenorQueUnoMuestraErrorSinConsultarLaBaseDeDatos() throws Exception {
        mockMvc.perform(get("/titulos").param("pagina", "0")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "El número de página debe ser un entero mayor o igual a 1.")));

        mockMvc.perform(get("/titulos").param("pagina", "abc")
                        .with(user("aprobador1").roles("APROBADOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "El número de página debe ser un entero mayor o igual a 1.")));

        verify(titulos, never()).buscar(any(), any(), any());
    }

    private Titulo guardar(
            String nombreGraduado,
            String documento,
            EstadoTitulo estado,
            LocalDate fechaRegistro) {

        Graduado graduado = graduados.save(
                new Graduado(TipoDocumento.PASAPORTE, documento, nombreGraduado));

        Titulo titulo = new Titulo(nombreGraduado, "Programa de prueba", fechaRegistro);
        titulo.setGraduado(graduado);
        titulo.setEstado(estado);

        return titulos.save(titulo);
    }
}