package pa.edu.utp.titulos_universitarios;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TituloValidatorTest {

    @Test
    void datosCompletos_debeSerValido() {
        assertTrue(TituloValidator.esValido("Juan Perez", "Licenciatura en Software"));
    }

    @Test
    void nombreGraduadoVacio_debeSerInvalido() {
        assertFalse(TituloValidator.esValido("", "Licenciatura en Software"));
    }

    @Test
    void nombreTituloNulo_debeSerInvalido() {
        assertFalse(TituloValidator.esValido("Juan Perez", null));
    }
}