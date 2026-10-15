package pa.edu.utp.titulos_universitarios;

import org.junit.jupiter.api.Test;
import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

class GraduadoTest {

    @Test
    void constructor_debeAsignarLosDatosDelGraduado() {
        Graduado graduado = new Graduado(
                TipoDocumento.CEDULA,
                "8-123-456",
                "María López"
        );

        assertEquals(TipoDocumento.CEDULA, graduado.getTipoDocumento());
        assertEquals("8-123-456", graduado.getNumeroDocumento());
        assertEquals("María López", graduado.getNombreCompleto());
        assertNull(graduado.getId());
    }

    @Test
    void setters_debenActualizarLosDatosDelGraduado() {
        Graduado graduado = new Graduado();

        graduado.setTipoDocumento(TipoDocumento.PASAPORTE);
        graduado.setNumeroDocumento("PA123456");
        graduado.setNombreCompleto("Carlos Pérez");

        assertEquals(TipoDocumento.PASAPORTE, graduado.getTipoDocumento());
        assertEquals("PA123456", graduado.getNumeroDocumento());
        assertEquals("Carlos Pérez", graduado.getNombreCompleto());
    }
}
