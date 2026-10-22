package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;
import pa.edu.utp.titulos_universitarios.repository.GraduadoRepository;

@ExtendWith(MockitoExtension.class)
class TituloServiceTest {

    @Mock
    private TituloRepository tituloRepository;

    @Mock
    private GraduadoRepository graduadoRepository;

    @Mock
    private IdentificadorGenerator identificadorGenerator;

    @Mock
    private CredencialPdfService credencialPdfService;

    @InjectMocks
    private TituloService tituloService;

    private Graduado graduadoValido() {
        return new Graduado(
                TipoDocumento.CEDULA,
                "8-123-456",
                "Ana Pérez"
        );
    }

    private Titulo tituloPendiente() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Ana Pérez");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");
        titulo.setEstado(EstadoTitulo.PENDIENTE);
        return titulo;
    }

    private void prepararRegistro(Titulo titulo) {
        titulo.setGraduado(graduadoValido());

        when(graduadoRepository
                .findByTipoDocumentoAndNumeroDocumento(
                        TipoDocumento.CEDULA, "8-123-456"))
                .thenReturn(Optional.empty());

        when(graduadoRepository.save(any(Graduado.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(tituloRepository.save(titulo)).thenReturn(titulo);
    }

    @Test
    void tituloValido_debeGuardarseConFechaDeRegistro() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Ana Pérez");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");

        prepararRegistro(titulo);

        Titulo resultado = tituloService.registrar(titulo);

        assertEquals(titulo, resultado);
        assertNotNull(resultado.getFechaRegistro());
        verify(graduadoRepository).save(any(Graduado.class));
        verify(tituloRepository).save(titulo);
    }

    @Test
    void tituloInvalido_noDebeGuardarse() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado(" ");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");

        assertThrows(
                IllegalArgumentException.class,
                () -> tituloService.registrar(titulo)
        );

        verifyNoInteractions(tituloRepository, graduadoRepository);
    }

    @Test
    void tituloRegistrado_debeQuedarPendienteAunqueVengaConOtroEstado() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Ana Pérez");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");
        titulo.setEstado(EstadoTitulo.APROBADO);
        titulo.setRevisadoPor("intruso");
        titulo.setIdentificador("falso");

        prepararRegistro(titulo);

        Titulo resultado = tituloService.registrar(titulo);

        assertEquals(EstadoTitulo.PENDIENTE, resultado.getEstado());
        assertNull(resultado.getRevisadoPor());
        assertNull(resultado.getFechaRevision());
        assertNull(resultado.getIdentificador());
    }

    @Test
    void aprobarTituloPendiente_debeQuedarAprobadoConRevisorFechaEIdentificador() {
        Titulo titulo = tituloPendiente();

        when(tituloRepository.findById(1L)).thenReturn(Optional.of(titulo));
        when(identificadorGenerator.generar()).thenReturn("ID-123");
        when(tituloRepository.save(titulo)).thenReturn(titulo);

        Titulo resultado = tituloService.aprobar(1L, "aprobador1");

        assertEquals(EstadoTitulo.APROBADO, resultado.getEstado());
        assertEquals("aprobador1", resultado.getRevisadoPor());
        assertNotNull(resultado.getFechaRevision());
        assertEquals("ID-123", resultado.getIdentificador());

        verify(identificadorGenerator).generar();
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
        assertNull(resultado.getIdentificador());

        verify(tituloRepository).save(titulo);
    }

    @Test
    void aprobarTituloYaAprobado_debeFallarSinGuardar() {
        Titulo titulo = tituloPendiente();
        titulo.setEstado(EstadoTitulo.APROBADO);

        when(tituloRepository.findById(1L)).thenReturn(Optional.of(titulo));

        assertThrows(
                IllegalStateException.class,
                () -> tituloService.aprobar(1L, "aprobador1")
        );

        verify(tituloRepository, never()).save(any());
    }

    @Test
    void aprobarTituloInexistente_debeFallarSinGuardar() {
        when(tituloRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tituloService.aprobar(99L, "aprobador1")
        );

        verify(tituloRepository, never()).save(any());
    }

    @Test
    void aprobarSinUsuario_debeFallarSinConsultarLaBase() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tituloService.aprobar(1L, " ")
        );

        verifyNoInteractions(tituloRepository);
    }

    @Test
    void listarPendientes_devuelveSoloLosPendientesDelRepositorio() {
        Titulo pendiente = tituloPendiente();

        when(tituloRepository.findByEstado(EstadoTitulo.PENDIENTE))
                .thenReturn(List.of(pendiente));

        List<Titulo> resultado = tituloService.listarPendientes();

        assertEquals(1, resultado.size());
        assertEquals(EstadoTitulo.PENDIENTE, resultado.get(0).getEstado());
        verify(tituloRepository).findByEstado(EstadoTitulo.PENDIENTE);
    }
    @Test
    void registrar_debeReutilizarGraduadoExistente() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Ana Pérez");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");
        titulo.setGraduado(graduadoValido());

        Graduado graduadoExistente = new Graduado(
                TipoDocumento.CEDULA,
                "8-123-456",
                "Ana Pérez"
        );

        when(graduadoRepository
                .findByTipoDocumentoAndNumeroDocumento(
                        TipoDocumento.CEDULA, "8-123-456"))
                .thenReturn(Optional.of(graduadoExistente));

        when(tituloRepository
                .existsByGraduado_IdAndNombreTituloIgnoreCase(
                        graduadoExistente.getId(),
                        "Licenciatura en Desarrollo de Software"))
                .thenReturn(false);

        when(tituloRepository.save(titulo)).thenReturn(titulo);

        Titulo resultado = tituloService.registrar(titulo);

        assertEquals(graduadoExistente, resultado.getGraduado());

        verify(graduadoRepository, never()).save(any(Graduado.class));
        verify(tituloRepository).save(titulo);
    }

    @Test
    void registrar_mismoDocumentoConOtroNombre_debeFallar() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Carlos Pérez");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");
        titulo.setGraduado(graduadoValido());

        Graduado graduadoExistente = new Graduado(
                TipoDocumento.CEDULA,
                "8-123-456",
                "Ana Pérez"
        );

        when(graduadoRepository
                .findByTipoDocumentoAndNumeroDocumento(
                        TipoDocumento.CEDULA, "8-123-456"))
                .thenReturn(Optional.of(graduadoExistente));

        assertThrows(
                IllegalArgumentException.class,
                () -> tituloService.registrar(titulo)
        );

        verify(graduadoRepository, never()).save(any(Graduado.class));
        verify(tituloRepository, never()).save(any(Titulo.class));
    }

    @Test
    void registrar_tituloDuplicado_debeFallar() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("Ana Pérez");
        titulo.setNombreTitulo("Licenciatura en Desarrollo de Software");
        titulo.setGraduado(graduadoValido());

        Graduado graduadoExistente = new Graduado(
                TipoDocumento.CEDULA,
                "8-123-456",
                "Ana Pérez"
        );

        when(graduadoRepository
                .findByTipoDocumentoAndNumeroDocumento(
                        TipoDocumento.CEDULA, "8-123-456"))
                .thenReturn(Optional.of(graduadoExistente));

        when(tituloRepository
                .existsByGraduado_IdAndNombreTituloIgnoreCase(
                        graduadoExistente.getId(),
                        "Licenciatura en Desarrollo de Software"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> tituloService.registrar(titulo)
        );

        verify(graduadoRepository, never()).save(any(Graduado.class));
        verify(tituloRepository, never()).save(any(Titulo.class));
    }

    @Test
    void contarPendientes_devuelveLaCantidadDelRepositorio() {
        when(tituloRepository.countByEstado(EstadoTitulo.PENDIENTE))
                .thenReturn(3L);

        long resultado = tituloService.contarPendientes();

        assertEquals(3L, resultado);
        verify(tituloRepository).countByEstado(EstadoTitulo.PENDIENTE);
    }

}
