package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** T-3.1.1 (HU-3.1): normalizacion, formato del codigo y solo titulos APROBADOS. */
@ExtendWith(MockitoExtension.class)
class VerificacionServiceTest {

    private static final String CODIGO = "3f2b8c1e-9a4d-4e7b-8c21-5d6f7a8b9c0d";

    @Mock
    private TituloRepository tituloRepository;

    @InjectMocks
    private VerificacionService servicio;

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
}
