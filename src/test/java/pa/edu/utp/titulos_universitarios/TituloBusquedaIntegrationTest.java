package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;
import pa.edu.utp.titulos_universitarios.repository.GraduadoRepository;

/**
 * T-1.8.1 (HU-1.8): la búsqueda devuelve exactamente los títulos esperados.
 *
 * Cada prueba usa palabras inventadas (qz...) para no mezclarse con otros
 * datos. Al terminar, @Transactional deshace todo lo guardado.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TituloBusquedaIntegrationTest {

    private static final LocalDate DIA = LocalDate.of(2030, 3, 10);

    @Autowired
    private TituloConsultaService servicio;

    @Autowired
    private TituloRepository titulos;

    @Autowired
    private GraduadoRepository graduados;

    // CA1: por nombre del graduado, sin distinguir mayúsculas, pero con tildes.
    @Test
    void nombreDelGraduadoNoDistingueMayusculasPeroSiTildes() {
        guardar("LUIS QZMARTINEZ", "HU18N001", "Programa A", EstadoTitulo.PENDIENTE, DIA);
        guardar("Ana Qzmartinez", "HU18N002", "Programa A", EstadoTitulo.PENDIENTE, DIA);
        guardar("Eva Qzmartínez", "HU18N003", "Programa A", EstadoTitulo.PENDIENTE, DIA);

        Page<Titulo> resultado = servicio.buscar("qzmartinez", null, 1);

        assertEquals(
                Set.of("LUIS QZMARTINEZ", "Ana Qzmartinez"),
                nombresGraduado(resultado));
    }

    // CA1: por número de documento, como texto contenido.
    @Test
    void documentoSeBuscaComoTextoContenido() {
        guardar("Graduado Uno", "HU18D-940-1", "Programa B", EstadoTitulo.PENDIENTE, DIA);
        guardar("Graduado Dos", "HU18D-555-2", "Programa B", EstadoTitulo.PENDIENTE, DIA);

        Page<Titulo> resultado = servicio.buscar("d-940", null, 1);

        assertEquals(Set.of("Graduado Uno"), nombresGraduado(resultado));
    }

    // CA1: por nombre del título.
    @Test
    void nombreDelTituloSeBuscaPorTextoContenido() {
        guardar("Graduado Tres", "HU18T001", "Licenciatura en Qzastronomia", EstadoTitulo.PENDIENTE, DIA);
        guardar("Graduado Cuatro", "HU18T002", "Licenciatura en Contabilidad", EstadoTitulo.PENDIENTE, DIA);

        Page<Titulo> resultado = servicio.buscar("QZASTRONOMIA", null, 1);

        assertEquals(Set.of("Graduado Tres"), nombresGraduado(resultado));
    }

    // CA2: el filtro por estado; «Todos» (null) no limita.
    @Test
    void filtroPorEstadoYTodos() {
        guardar("Qzestado Pendiente", "HU18E001", "Programa C", EstadoTitulo.PENDIENTE, DIA);
        guardar("Qzestado Aprobado", "HU18E002", "Programa C", EstadoTitulo.APROBADO, DIA);
        guardar("Qzestado Rechazado", "HU18E003", "Programa C", EstadoTitulo.RECHAZADO, DIA);

        assertEquals(
                Set.of("Qzestado Aprobado"),
                nombresGraduado(servicio.buscar("qzestado", EstadoTitulo.APROBADO, 1)));

        assertEquals(
                3,
                servicio.buscar("qzestado", null, 1).getTotalElements());
    }

    // CA2: búsqueda y estado se combinan.
    @Test
    void busquedaYEstadoSeCombinan() {
        guardar("Qzcombo Rosa", "HU18C001", "Programa D", EstadoTitulo.APROBADO, DIA);
        guardar("Qzcombo Pedro", "HU18C002", "Programa D", EstadoTitulo.PENDIENTE, DIA);
        guardar("Otro Nombre", "HU18C003", "Programa D", EstadoTitulo.APROBADO, DIA);

        Page<Titulo> resultado =
                servicio.buscar("qzcombo", EstadoTitulo.APROBADO, 1);

        assertEquals(Set.of("Qzcombo Rosa"), nombresGraduado(resultado));
    }

    // Los espacios al inicio y al final se ignoran.
    @Test
    void losEspaciosDelExtremoSeIgnoran() {
        guardar("Qzespacio Luna", "HU18S001", "Programa E", EstadoTitulo.PENDIENTE, DIA);

        Page<Titulo> resultado = servicio.buscar("   qzespacio   ", null, 1);

        assertEquals(Set.of("Qzespacio Luna"), nombresGraduado(resultado));
    }

    // CA3 y CA6: 20 por página, de lo más reciente a lo más antiguo, y la
    // consulta trae solo la página pedida.
    @Test
    void veinteTitulosPorPaginaDeLoMasRecienteALoMasAntiguo() {
        for (int i = 1; i <= 25; i++) {
            guardar(
                    String.format("Qzpagina %02d", i),
                    String.format("HU18G%03d", i),
                    "Programa F",
                    EstadoTitulo.PENDIENTE,
                    DIA.plusDays(i));
        }

        Page<Titulo> primera = servicio.buscar("qzpagina", null, 1);
        Page<Titulo> segunda = servicio.buscar("qzpagina", null, 2);

        assertEquals(25, primera.getTotalElements());
        assertEquals(20, primera.getContent().size());   // CA6: como máximo 20 filas
        assertEquals(5, segunda.getContent().size());

        // El más reciente (día 25) va primero.
        assertEquals("Qzpagina 25", primera.getContent().get(0).getNombreGraduado());
        assertEquals("Qzpagina 05", segunda.getContent().get(0).getNombreGraduado());
    }

    // Sin búsqueda ni estado: no se limita nada y no falla.
    @Test
    void sinBusquedaDevuelveLosTitulos() {
        guardar("Qzsinfiltro Uno", "HU18F001", "Programa G", EstadoTitulo.PENDIENTE, DIA);

        Page<Titulo> resultado = servicio.buscar(null, null, 1);

        assertTrue(resultado.getTotalElements() >= 1);
        assertTrue(resultado.getContent().size() <= TituloConsultaService.TAMANO_PAGINA);
    }

    // CA4: entradas inválidas se rechazan antes de consultar.
    @Test
    void busquedaDeUnCaracterEsInvalida() {
        TituloValidationException error = assertThrows(
                TituloValidationException.class,
                () -> servicio.buscar("a", null, 1));

        assertEquals("q", error.getCampo());
    }

    @Test
    void busquedaDeMasDeCienCaracteresEsInvalida() {
        String larga = "a".repeat(101);

        TituloValidationException error = assertThrows(
                TituloValidationException.class,
                () -> servicio.buscar(larga, null, 1));

        assertEquals("q", error.getCampo());
    }

    @Test
    void paginaMenorQueUnoEsInvalida() {
        TituloValidationException error = assertThrows(
                TituloValidationException.class,
                () -> servicio.buscar(null, null, 0));

        assertEquals("pagina", error.getCampo());
    }

    private Titulo guardar(
            String nombreGraduado,
            String documento,
            String nombreTitulo,
            EstadoTitulo estado,
            LocalDate fechaRegistro) {

        Graduado graduado = graduados.save(
                new Graduado(TipoDocumento.PASAPORTE, documento, nombreGraduado));

        Titulo titulo = new Titulo(nombreGraduado, nombreTitulo, fechaRegistro);
        titulo.setGraduado(graduado);
        titulo.setEstado(estado);

        return titulos.save(titulo);
    }

    private Set<String> nombresGraduado(Page<Titulo> pagina) {
        List<Titulo> contenido = pagina.getContent();
        return contenido.stream()
                .map(Titulo::getNombreGraduado)
                .collect(Collectors.toSet());
    }
}