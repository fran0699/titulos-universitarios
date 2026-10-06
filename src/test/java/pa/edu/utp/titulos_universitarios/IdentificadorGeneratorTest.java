package pa.edu.utp.titulos_universitarios;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IdentificadorGeneratorTest {

    @Test
    void generar_debeCrearIdentificadorNoNulo() {
        IdentificadorGenerator generator = new IdentificadorGenerator();

        String identificador = generator.generar();

        assertNotNull(identificador);
    }

    @Test
    void generar_debeCrearIdentificadoresDiferentes() {
        IdentificadorGenerator generator = new IdentificadorGenerator();

        String identificador1 = generator.generar();
        String identificador2 = generator.generar();

        assertNotEquals(identificador1, identificador2);
    }
}