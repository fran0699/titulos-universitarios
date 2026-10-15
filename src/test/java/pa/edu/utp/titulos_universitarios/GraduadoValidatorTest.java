
package pa.edu.utp.titulos_universitarios;

import org.junit.jupiter.api.Test;

import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                TipoDocumento.PASAPORTE, "A1234"));
    }

    @Test
    void aceptaPasaporteEnLongitudMaxima() {
        assertTrue(GraduadoValidator.esDocumentoValido(
                TipoDocumento.PASAPORTE, "ABC12345678901234567"));
    }

    @Test
    void rechazaPasaportePorDebajoDelMinimo() {
        assertFalse(GraduadoValidator.esDocumentoValido(
                TipoDocumento.PASAPORTE, "A123"));
    }

    @Test
    void rechazaPasaportePorEncimaDelMaximo() {
        assertFalse(GraduadoValidator.esDocumentoValido(
                TipoDocumento.PASAPORTE, "ABCDEFGHIJKLMNOPQRSTU"));
    }

    @Test
    void aceptaPasaporteConEspaciosYGuiones() {
        assertTrue(GraduadoValidator.esDocumentoValido(
                TipoDocumento.PASAPORTE, "c0-234 567"));
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
    void aceptaNombreConInicialYPunto() {
        assertTrue(GraduadoValidator.esNombreValido("Ana M. Pérez"));
    }

    @Test
    void normalizaPasaporteAMayusculasYEliminaEspaciosYGuiones() {
        assertEquals(
                "C0234567",
                GraduadoValidator.normalizarDocumento(
                        TipoDocumento.PASAPORTE, "c0-234 567"));
    }

    @Test
    void normalizaCedulaEliminandoCerosALaIzquierda() {
        assertEquals(
                "8-940-1000",
                GraduadoValidator.normalizarDocumento(
                        TipoDocumento.CEDULA, "08-940-1000"));
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
