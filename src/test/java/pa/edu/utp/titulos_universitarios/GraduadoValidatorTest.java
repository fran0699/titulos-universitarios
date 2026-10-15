
package pa.edu.utp.titulos_universitarios;

import org.junit.jupiter.api.Test;

import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;
import pa.edu.utp.titulos_universitarios.GraduadoValidator;

import static org.junit.jupiter.api.Assertions.*;

class GraduadoValidatorTest {

    @Test
    void aceptaCedulaConGuiones() {
        assertTrue(GraduadoValidator.esDocumentoValido(
                TipoDocumento.CEDULA, "8-123-1234"));
    }

    @Test
    void rechazaCedulaSinGuiones() {
        assertFalse(GraduadoValidator.esDocumentoValido(
                TipoDocumento.CEDULA, "81231234"));
    }

    @Test
    void aceptaPasaporteEnLongitudMinima() {
        assertTrue(GraduadoValidator.esDocumentoValido(
                TipoDocumento.PASAPORTE, "ABC123"));
    }

    @Test
    void aceptaPasaporteEnLongitudMaxima() {
        assertTrue(GraduadoValidator.esDocumentoValido(
                TipoDocumento.PASAPORTE, "ABC123456789"));
    }

    @Test
    void rechazaPasaportePorDebajoDelMinimo() {
        assertFalse(GraduadoValidator.esDocumentoValido(
                TipoDocumento.PASAPORTE, "ABC12"));
    }

    @Test
    void rechazaPasaportePorEncimaDelMaximo() {
        assertFalse(GraduadoValidator.esDocumentoValido(
                TipoDocumento.PASAPORTE, "ABC1234567890"));
    }

    @Test
    void rechazaPasaporteConGuiones() {
        assertFalse(GraduadoValidator.esDocumentoValido(
                TipoDocumento.PASAPORTE, "ABC-123"));
    }

    @Test
    void rechazaDocumentoNulo() {
        assertFalse(GraduadoValidator.esDocumentoValido(
                TipoDocumento.PASAPORTE, null));
    }

    @Test
    void rechazaTipoDocumentoNulo() {
        assertFalse(GraduadoValidator.esDocumentoValido(
                null, "ABC123"));
    }

    @Test
    void aceptaNombreValido() {
        assertTrue(GraduadoValidator.esNombreValido(
                "María Elena González"));
    }

    @Test
    void rechazaNombreVacio() {
        assertFalse(GraduadoValidator.esNombreValido("   "));
    }

    @Test
    void rechazaNombreNulo() {
        assertFalse(GraduadoValidator.esNombreValido(null));
    }

    @Test
    void normalizaEspaciosDelNombre() {
        assertEquals(
                "María Elena González",
                GraduadoValidator.normalizarNombre(
                        "  María   Elena González  "));
    }
}
