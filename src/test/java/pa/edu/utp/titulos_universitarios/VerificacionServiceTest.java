package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

/** T-3.1.1 (HU-3.1): normalizacion, formato del codigo y solo titulos APROBADOS. */
@ExtendWith(MockitoExtension.class)
class VerificacionServiceTest {

    private static final String CODIGO = "3f2b8c1e-9a4d-4e7b-8c21-5d6f7a8b9c0d";

    @Mock
    private TituloRepository tituloRepository;

    private VerificacionService servicio;

    @BeforeEach
    void crearServicio() {
        // 26/10/2026 19:35 UTC = 14:35 en Panama (UTC-5).
        Clock reloj = Clock.fixed(Instant.parse("2026-10-26T19:35:00Z"), ZoneOffset.UTC);
        servicio = new VerificacionService(tituloRepository, reloj, "Institucion de Prueba");
    }

    private Titulo titulo(EstadoTitulo estado) {
        Titulo titulo = new Titulo();
        titulo.setIdentificador(CODIGO);
        titulo.setEstado(estado);
        return titulo;
    }

    @Test
    void normalizar_quitaEspaciosYPasaAMinusculas() {
        assertEquals(CODIGO, VerificacionService.normalizar("  " + CODIGO.toUpperCase() + "  "));
        assertEquals("", VerificacionService.normalizar(null));
        assertEquals("", VerificacionService.normalizar("   "));
    }

    @Test
    void esFormatoValido_aceptaUnUuidDe36Caracteres() {
        assertTrue(VerificacionService.esFormatoValido(CODIGO));
    }

    @Test
    void esFormatoValido_rechazaLoQueNoEsUuid() {
        assertFalse(VerificacionService.esFormatoValido(""));
        assertFalse(VerificacionService.esFormatoValido("abc"));
        assertFalse(VerificacionService.esFormatoValido(CODIGO + "0"));
        assertFalse(VerificacionService.esFormatoValido(CODIGO.replace('3', 'g')));
        assertFalse(VerificacionService.esFormatoValido(CODIGO.replace("-", "")));
        assertFalse(VerificacionService.esFormatoValido("' OR '1'='1"));
    }

    @Test
    void buscarAprobado_formatoInvalido_noConsultaLaBaseDeDatos() {
        assertTrue(servicio.buscarAprobado("no-es-un-uuid").isEmpty());
        assertTrue(servicio.buscarAprobado(null).isEmpty());
        verifyNoInteractions(tituloRepository);
    }

    @Test
    void buscarAprobado_tituloAprobado_loDevuelve() {
        Titulo aprobado = titulo(EstadoTitulo.APROBADO);
        when(tituloRepository.findByIdentificador(CODIGO)).thenReturn(Optional.of(aprobado));

        assertEquals(Optional.of(aprobado), servicio.buscarAprobado(CODIGO));
    }

    @Test
    void buscarAprobado_normalizaAntesDeBuscar() {
        Titulo aprobado = titulo(EstadoTitulo.APROBADO);
        when(tituloRepository.findByIdentificador(CODIGO)).thenReturn(Optional.of(aprobado));

        assertTrue(servicio.buscarAprobado("  " + CODIGO.toUpperCase() + " ").isPresent());
        verify(tituloRepository).findByIdentificador(CODIGO);
    }

    @Test
    void buscarAprobado_pendienteORechazado_danVacio() {
        when(tituloRepository.findByIdentificador(CODIGO))
                .thenReturn(Optional.of(titulo(EstadoTitulo.PENDIENTE)))
                .thenReturn(Optional.of(titulo(EstadoTitulo.RECHAZADO)));

        assertTrue(servicio.buscarAprobado(CODIGO).isEmpty());
        assertTrue(servicio.buscarAprobado(CODIGO).isEmpty());
    }

    @Test
    void buscarAprobado_codigoInexistente_danVacio() {
        when(tituloRepository.findByIdentificador(CODIGO)).thenReturn(Optional.empty());

        assertTrue(servicio.buscarAprobado(CODIGO).isEmpty());
    }

    @Test
    void enmascarar_dejaSoloLosUltimos4CaracteresQueNoSonGuion() {
        assertEquals("•-•••-4567", VerificacionService.enmascarar("8-123-4567"));
        assertEquals("••-5-614", VerificacionService.enmascarar("PE-5-614"));
        assertEquals("•••••4567", VerificacionService.enmascarar("PA1234567"));
        assertEquals("", VerificacionService.enmascarar(null));
        assertEquals("1234", VerificacionService.enmascarar("1234"));
    }

    @Test
    void consultar_devuelveSoloLosDatosMinimosConFechasEnDosIdiomas() {
        Titulo titulo = titulo(EstadoTitulo.APROBADO);
        titulo.setNombreGraduado("Ana Perez");
        titulo.setNombreTitulo("Licenciatura en Ingenieria de Software");
        titulo.setFechaRevision(LocalDateTime.of(2026, 10, 10, 9, 0));
        titulo.setGraduado(new Graduado(TipoDocumento.CEDULA, "8-123-4567", "Ana Perez"));
        when(tituloRepository.findByIdentificador(CODIGO)).thenReturn(Optional.of(titulo));

        ResultadoVerificacion r = servicio.consultar(CODIGO).orElseThrow();

        assertEquals("Ana Perez", r.graduado());
        assertEquals("Cédula", r.tipoDocumento());
        assertEquals("•-•••-4567", r.documentoEnmascarado());
        assertEquals("Licenciatura en Ingenieria de Software", r.titulo());
        assertEquals("Institucion de Prueba", r.institucion());
        assertEquals("10 de octubre de 2026", r.fechaEmisionEs());
        assertEquals("October 10, 2026", r.fechaEmisionEn());
        assertEquals(CODIGO, r.codigo());
        assertEquals("26 de octubre de 2026, 14:35", r.fechaConsultaEs());
        assertEquals("October 26, 2026, 14:35", r.fechaConsultaEn());
    }
}
