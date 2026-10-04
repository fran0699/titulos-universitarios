package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TituloServiceTest {

    @Mock
    private TituloRepository tituloRepository;

    @InjectMocks
    private TituloService tituloService;

    private Titulo tituloPendiente() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Ana Pérez");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");
        titulo.setEstado(EstadoTitulo.PENDIENTE);
        return titulo;
    }

    @Test
    void tituloValido_debeGuardarseConFechaDeRegistro() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Ana Pérez");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");
        when(tituloRepository.save(titulo)).thenReturn(titulo);

        Titulo resultado = tituloService.registrar(titulo);

        assertEquals(titulo, resultado);
        assertNotNull(resultado.getFechaRegistro());
        verify(tituloRepository).save(titulo);
    }

    @Test
    void tituloInvalido_noDebeGuardarse() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado(" ");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");

        assertThrows(IllegalArgumentException.class, () -> tituloService.registrar(titulo));
        verifyNoInteractions(tituloRepository);
    }

    @Test
    void tituloRegistrado_debeQuedarPendienteAunqueVengaConOtroEstado() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Ana Pérez");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");
        titulo.setEstado(EstadoTitulo.APROBADO);
        titulo.setRevisadoPor("intruso");
        when(tituloRepository.save(titulo)).thenReturn(titulo);

        Titulo resultado = tituloService.registrar(titulo);

        assertEquals(EstadoTitulo.PENDIENTE, resultado.getEstado());
        assertNull(resultado.getRevisadoPor());
        assertNull(resultado.getFechaRevision());
    }

    @Test
    void aprobarTituloPendiente_debeQuedarAprobadoConRevisorYFecha() {
        Titulo titulo = tituloPendiente();
        when(tituloRepository.findById(1L)).thenReturn(Optional.of(titulo));
        when(tituloRepository.save(titulo)).thenReturn(titulo);

        Titulo resultado = tituloService.aprobar(1L, "aprobador1");

        assertEquals(EstadoTitulo.APROBADO, resultado.getEstado());
        assertEquals("aprobador1", resultado.getRevisadoPor());
        assertNotNull(resultado.getFechaRevision());
        verify(tituloRepository).save(titulo);
    }

    @Test
    void rechazarTituloPendiente_debeQuedarRechazadoConRevisorYFecha() {
        Titulo titulo = tituloPendiente();
        when(tituloRepository.findById(1L)).thenReturn(Optional.of(titulo));
        when(tituloRepository.save(titulo)).thenReturn(titulo);

        Titulo resultado = tituloService.rechazar(1L, "aprobador1");

        assertEquals(EstadoTitulo.RECHAZADO, resultado.getEstado());
        assertEquals("aprobador1", resultado.getRevisadoPor());
        assertNotNull(resultado.getFechaRevision());
        verify(tituloRepository).save(titulo);
    }

    @Test
    void aprobarTituloYaAprobado_debeFallarSinGuardar() {
        Titulo titulo = tituloPendiente();
        titulo.setEstado(EstadoTitulo.APROBADO);
        when(tituloRepository.findById(1L)).thenReturn(Optional.of(titulo));

        assertThrows(IllegalStateException.class, () -> tituloService.aprobar(1L, "aprobador1"));
        verify(tituloRepository, never()).save(any());
    }

    @Test
    void aprobarTituloInexistente_debeFallarSinGuardar() {
        when(tituloRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> tituloService.aprobar(99L, "aprobador1"));
        verify(tituloRepository, never()).save(any());
    }

    @Test
    void aprobarSinUsuario_debeFallarSinConsultarLaBase() {
        assertThrows(IllegalArgumentException.class, () -> tituloService.aprobar(1L, " "));
        verifyNoInteractions(tituloRepository);
    }
}
