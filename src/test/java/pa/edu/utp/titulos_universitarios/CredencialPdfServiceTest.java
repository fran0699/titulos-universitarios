package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

class CredencialPdfServiceTest {

    private final CredencialPdfService servicio =
            new CredencialPdfService("http://localhost:8080/verificar");

    @Test
    void generaPdfConDatosDelGraduadoYConservaTildes() throws Exception {
        byte[] pdf = servicio.generar(tituloAprobado());

        try (var documento = Loader.loadPDF(pdf)) {
            String texto = new PDFTextStripper().getText(documento);

            assertTrue(texto.contains("UNIVERSIDAD TECNOLÓGICA DE PANAMÁ"));
            assertTrue(texto.contains("Secretaría General"));
            assertTrue(texto.contains("José Núñez Agüero"));
            assertTrue(texto.contains("CREDENCIAL DE TÍTULO UNIVERSITARIO"));
            assertTrue(texto.contains("cédula de identidad personal N.º 8-123-456"));
            assertTrue(texto.contains("Licenciatura en Ingeniería de Software"));
            assertTrue(texto.contains("Fecha de emisión: 21 de octubre de 2026"));
            assertTrue(texto.contains("ID-CREDENCIAL-123"));
            assertTrue(texto.contains("No constituye una firma electrónica certificada."));
            assertFalse(texto.contains("SHA-256"));
            assertFalse(texto.toLowerCase().contains("código qr"));
            assertTrue(texto.contains("Verifique este título en http://localhost:8080/verificar"));
        }
    }

    @Test
    void rechazaTituloQueNoEstaAprobado() {
        Titulo titulo = tituloAprobado();
        titulo.setEstado(EstadoTitulo.PENDIENTE);

        assertThrows(IllegalStateException.class, () -> servicio.generar(titulo));
    }

    @Test
    void rechazaTituloAprobadoSinIdentificador() {
        Titulo titulo = tituloAprobado();
        titulo.setIdentificador(" ");

        assertThrows(IllegalArgumentException.class, () -> servicio.generar(titulo));
    }

    @Test
    void generaPdfValidoParaTituloAprobado() {
        assertDoesNotThrow(() -> servicio.generar(tituloAprobado()));
    }

    private Titulo tituloAprobado() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("José Núñez Agüero");
        titulo.setNombreTitulo("Licenciatura en Ingeniería de Software");
        titulo.setEstado(EstadoTitulo.APROBADO);
        titulo.setFechaRevision(LocalDateTime.of(2026, 10, 21, 10, 30));
        titulo.setIdentificador("ID-CREDENCIAL-123");
        titulo.setGraduado(new Graduado(TipoDocumento.CEDULA, "8-123-456", "José Núñez Agüero"));
        return titulo;
    }
}
