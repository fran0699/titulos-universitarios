package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

/**
 * HU-4.7: el controlador ubica cada error en su campo a partir de
 * TituloValidationException.getCampo(), sin buscar palabras en el mensaje.
 */
@ExtendWith(MockitoExtension.class)
class TituloControllerTest {

    @Mock
    private TituloService tituloService;

    @InjectMocks
    private TituloController controller;

    private final Principal principal = () -> "registrador1";

    private TituloForm formularioValido() {
        TituloForm form = new TituloForm();
        form.setTipoDocumento(TipoDocumento.CEDULA);
        form.setNumeroDocumento("8-123-456");
        form.setNombreGraduado("Ana Pérez");
        form.setNombreTitulo("Licenciatura en Desarrollo de Software");
        return form;
    }

    private BindingResult enlazar(TituloForm form) {
        return new BeanPropertyBindingResult(form, "tituloForm");
    }

    private String registrarFallando(
            RuntimeException error, BindingResult resultado, Model model) {
        when(tituloService.registrar(any(Titulo.class), eq("registrador1")))
                .thenThrow(error);
        return controller.registrar(
                formularioValido(), resultado, principal,
                new RedirectAttributesModelMap(), model);
    }

    @Test
    void errorConCampoNombreTitulo_vaBajoElNombreDelTitulo() {
        BindingResult resultado = enlazar(formularioValido());
        Model model = new ExtendedModelMap();

        String vista = registrarFallando(
                new TituloValidationException(
                        "nombreTitulo", "Este graduado ya tiene registrado ese título."),
                resultado, model);

        assertEquals("titulos/nuevo", vista);
        assertNotNull(resultado.getFieldError("nombreTitulo"));
        assertNull(resultado.getFieldError("nombreGraduado"));
        assertNull(model.getAttribute("error"));
    }

    @Test
    void errorConCampoNombreGraduado_vaBajoElNombreDelGraduado() {
        BindingResult resultado = enlazar(formularioValido());

        registrarFallando(
                new TituloValidationException(
                        "nombreGraduado", "El documento ya está registrado con otro nombre."),
                resultado, new ExtendedModelMap());

        assertNotNull(resultado.getFieldError("nombreGraduado"));
        assertNull(resultado.getFieldError("nombreTitulo"));
    }

    @Test
    void errorConCampoNumeroDocumento_vaBajoElDocumento() {
        BindingResult resultado = enlazar(formularioValido());

        registrarFallando(
                new TituloValidationException(
                        "numeroDocumento", "El tipo o número de documento del graduado no es válido."),
                resultado, new ExtendedModelMap());

        assertNotNull(resultado.getFieldError("numeroDocumento"));
        assertNull(resultado.getFieldError("nombreGraduado"));
    }

    @Test
    void elCampoDelErrorNoDependeDelTextoDelMensaje() {
        BindingResult resultado = enlazar(formularioValido());

        // Un mensaje que menciona "título" pero cuyo campo es el documento:
        // antes caía bajo el título; ahora manda el campo.
        registrarFallando(
                new TituloValidationException(
                        "numeroDocumento", "Texto cualquiera sobre el título"),
                resultado, new ExtendedModelMap());

        assertNotNull(resultado.getFieldError("numeroDocumento"));
        assertNull(resultado.getFieldError("nombreTitulo"));
    }

    @Test
    void errorSinCampo_vaAlMensajeGeneral() {
        BindingResult resultado = enlazar(formularioValido());
        Model model = new ExtendedModelMap();

        registrarFallando(
                new TituloValidationException(null, "Mensaje general."),
                resultado, model);

        assertFalse(resultado.hasFieldErrors());
        assertEquals("Mensaje general.", model.getAttribute("error"));
    }

    @Test
    void errorSinTipoTituloValidation_vaAlMensajeGeneral() {
        BindingResult resultado = enlazar(formularioValido());
        Model model = new ExtendedModelMap();

        registrarFallando(
                new IllegalArgumentException("Otro problema."),
                resultado, model);

        assertFalse(resultado.hasFieldErrors());
        assertEquals("Otro problema.", model.getAttribute("error"));
    }

    @Test
    void registroExitoso_redirigeALaLista() {
        when(tituloService.registrar(any(Titulo.class), eq("registrador1")))
                .thenReturn(new Titulo());

        String vista = controller.registrar(
                formularioValido(), enlazar(formularioValido()), principal,
                new RedirectAttributesModelMap(), new ExtendedModelMap());

        assertTrue(vista.startsWith("redirect:"));
    }
}
