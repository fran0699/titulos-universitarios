
package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

class TituloFormTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void prepararValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        factory.close();
    }

    private TituloForm formularioValido() {
        TituloForm form = new TituloForm();
        form.setTipoDocumento(TipoDocumento.CEDULA);
        form.setNumeroDocumento("8-123-456");
        form.setNombreGraduado("Ana Pérez");
        form.setNombreTitulo("Licenciatura en Software");
        return form;
    }

    private boolean tieneErrorEn(TituloForm form, String campo) {
        Set<ConstraintViolation<TituloForm>> errores =
                validator.validate(form);

        return errores.stream().anyMatch(error ->
                error.getPropertyPath().toString().equals(campo));
    }

    @Test
    void nombreGraduado_debeRespetarLimitesDeLongitud() {
        TituloForm form = formularioValido();

        form.setNombreGraduado("A".repeat(2));
        assertTrue(tieneErrorEn(form, "nombreGraduado"));

        form.setNombreGraduado("A".repeat(3));
        assertFalse(tieneErrorEn(form, "nombreGraduado"));

        form.setNombreGraduado("A".repeat(120));
        assertFalse(tieneErrorEn(form, "nombreGraduado"));

        form.setNombreGraduado("A".repeat(121));
        assertTrue(tieneErrorEn(form, "nombreGraduado"));
    }

    @Test
    void nombreTitulo_debeRespetarLimitesDeLongitud() {
        TituloForm form = formularioValido();

        form.setNombreTitulo("A".repeat(4));
        assertTrue(tieneErrorEn(form, "nombreTitulo"));

        form.setNombreTitulo("A".repeat(5));
        assertFalse(tieneErrorEn(form, "nombreTitulo"));

        form.setNombreTitulo("A".repeat(150));
        assertFalse(tieneErrorEn(form, "nombreTitulo"));

        form.setNombreTitulo("A".repeat(151));
        assertTrue(tieneErrorEn(form, "nombreTitulo"));
    }

    @Test
    void numeroDocumento_debeRespetarLimitesDeLongitud() {
        TituloForm form = formularioValido();

        form.setNumeroDocumento("1".repeat(4));
        assertTrue(tieneErrorEn(form, "numeroDocumento"));

        form.setNumeroDocumento("1".repeat(5));
        assertFalse(tieneErrorEn(form, "numeroDocumento"));

        form.setNumeroDocumento("1".repeat(20));
        assertFalse(tieneErrorEn(form, "numeroDocumento"));

        form.setNumeroDocumento("1".repeat(21));
        assertTrue(tieneErrorEn(form, "numeroDocumento"));
    }
}
