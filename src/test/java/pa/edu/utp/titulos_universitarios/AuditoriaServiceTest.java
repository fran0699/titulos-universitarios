package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;
import pa.edu.utp.titulos_universitarios.entity.EventoAuditoria;
import pa.edu.utp.titulos_universitarios.repository.EventoAuditoriaRepository;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private EventoAuditoriaRepository eventoAuditoriaRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @Test
    void registrarEvento_guardaUnEventoConLosDatosYLaFechaDelServidor() {
        when(eventoAuditoriaRepository.save(any(EventoAuditoria.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        EventoAuditoria resultado = auditoriaService.registrarEvento(
                AccionAuditoria.APROBAR, "aprobador1", 7L, "Título aprobado");

        ArgumentCaptor<EventoAuditoria> captor =
                ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(eventoAuditoriaRepository).save(captor.capture());
        EventoAuditoria guardado = captor.getValue();

        assertEquals(AccionAuditoria.APROBAR, guardado.getAccion());
        assertEquals("aprobador1", guardado.getUsuario());
        assertEquals(7L, guardado.getTituloId());
        assertEquals("Título aprobado", guardado.getDetalle());
        assertNotNull(guardado.getFechaHora());
        assertSame(guardado, resultado);
    }

    @Test
    void registrarEvento_sinAccion_noGuarda() {
        assertThrows(IllegalArgumentException.class, () ->
                auditoriaService.registrarEvento(null, "aprobador1", 7L, "x"));
        verifyNoInteractions(eventoAuditoriaRepository);
    }

    @Test
    void registrarEvento_usuarioMuyCorto_noGuarda() {
        assertThrows(IllegalArgumentException.class, () ->
                auditoriaService.registrarEvento(
                        AccionAuditoria.REGISTRAR, "ab", 7L, "x"));
        verifyNoInteractions(eventoAuditoriaRepository);
    }

    @Test
    void registrarEvento_sinTitulo_noGuarda() {
        assertThrows(IllegalArgumentException.class, () ->
                auditoriaService.registrarEvento(
                        AccionAuditoria.REGISTRAR, "registrador1", null, "x"));
        verifyNoInteractions(eventoAuditoriaRepository);
    }

    @Test
    void registrarEvento_detalleMuyLargo_noGuarda() {
        String detalleLargo = "x".repeat(501);
        assertThrows(IllegalArgumentException.class, () ->
                auditoriaService.registrarEvento(
                        AccionAuditoria.RECHAZAR, "aprobador1", 7L, detalleLargo));
        verifyNoInteractions(eventoAuditoriaRepository);
    }

    @Test
    void registrarEvento_usuarioEnLimiteMinimo_seGuarda() {
        when(eventoAuditoriaRepository.save(any(EventoAuditoria.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        auditoriaService.registrarEvento(
                AccionAuditoria.REGISTRAR, "abc", 7L, "x");

        verify(eventoAuditoriaRepository).save(any(EventoAuditoria.class));
    }

    @Test
    void registrarEvento_usuarioEnLimiteMaximo_seGuarda() {
        when(eventoAuditoriaRepository.save(any(EventoAuditoria.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        auditoriaService.registrarEvento(
                AccionAuditoria.REGISTRAR, "u".repeat(50), 7L, "x");

        verify(eventoAuditoriaRepository).save(any(EventoAuditoria.class));
    }

    @Test
    void registrarEvento_usuarioMuyLargo_noGuarda() {
        assertThrows(IllegalArgumentException.class, () ->
                auditoriaService.registrarEvento(
                        AccionAuditoria.REGISTRAR, "u".repeat(51), 7L, "x"));
        verifyNoInteractions(eventoAuditoriaRepository);
    }

    @Test
    void registrarEvento_detalleEnLimiteMaximo_seGuarda() {
        when(eventoAuditoriaRepository.save(any(EventoAuditoria.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        auditoriaService.registrarEvento(
                AccionAuditoria.APROBAR, "aprobador1", 7L, "d".repeat(500));

        verify(eventoAuditoriaRepository).save(any(EventoAuditoria.class));
    }

    @Test
    void registrarEvento_usuarioConEspacios_seGuardaRecortado() {
        when(eventoAuditoriaRepository.save(any(EventoAuditoria.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        EventoAuditoria guardado = auditoriaService.registrarEvento(
                AccionAuditoria.APROBAR, "  aprobador1  ", 7L, "x");

        assertEquals("aprobador1", guardado.getUsuario());
    }
}
