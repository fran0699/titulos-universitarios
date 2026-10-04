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

@ExtendWith(MockitoExtension.class)
class TituloServiceTest {

    @Mock
    private TituloRepository tituloRepository;

    @InjectMocks
    private TituloService tituloService;

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
}
