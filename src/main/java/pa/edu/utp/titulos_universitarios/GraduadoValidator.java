
package pa.edu.utp.titulos_universitarios;

import java.util.Locale;
import java.util.regex.Pattern;

import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

public class GraduadoValidator {

    private static final int DOCUMENTO_MIN = 5;
    private static final int DOCUMENTO_MAX = 20;
    private static final int NOMBRE_MIN = 3;
    private static final int NOMBRE_MAX = 120;

    private static final Pattern CEDULA =
            Pattern.compile("[A-Z0-9]+-[A-Z0-9]+-[A-Z0-9]+");

    private static final Pattern PASAPORTE =
            Pattern.compile("[A-Z0-9]+");

    private GraduadoValidator() {
    }

    public static boolean esDocumentoValido(
            TipoDocumento tipoDocumento,
            String numeroDocumento) {

        if (tipoDocumento == null || numeroDocumento == null) {
            return false;
        }

        String documento = numeroDocumento.trim()
                .toUpperCase(Locale.ROOT);

        if (documento.length() < DOCUMENTO_MIN
                || documento.length() > DOCUMENTO_MAX) {
            return false;
        }

        return switch (tipoDocumento) {
            case CEDULA -> {
                if (!CEDULA.matcher(documento).matches()) {
                    yield false;
                }

                String normalizado = normalizarCedula(documento);
                yield normalizado != null
                        && normalizado.length() >= DOCUMENTO_MIN
                        && normalizado.length() <= DOCUMENTO_MAX;
            }
            case PASAPORTE -> {
                String normalizado = documento.replaceAll("[\\s-]", "");
                yield PASAPORTE.matcher(normalizado).matches()
                        && normalizado.length() >= DOCUMENTO_MIN
                        && normalizado.length() <= DOCUMENTO_MAX;
            }
        };
    }

    public static String normalizarDocumento(
            TipoDocumento tipoDocumento,
            String numeroDocumento) {

        if (tipoDocumento == null || numeroDocumento == null) {
            return null;
        }

        String documento = numeroDocumento.trim()
                .toUpperCase(Locale.ROOT);

        return switch (tipoDocumento) {
            case CEDULA -> normalizarCedula(documento);
            case PASAPORTE -> documento.replaceAll("[\\s-]", "");
        };
    }

    private static String normalizarCedula(String documento) {
        String[] bloques = documento.split("-", -1);

        if (bloques.length != 3) {
            return null;
        }

        for (int i = 0; i < bloques.length; i++) {
            if (bloques[i].matches("[0-9]+")) {
                bloques[i] = bloques[i].replaceFirst("^0+(?!$)", "");
            } else if (!bloques[i].matches("[A-Z]+")) {
                return null;
            }
        }

        return String.join("-", bloques);
    }


    public static boolean esNombreValido(String nombreCompleto) {
        if (nombreCompleto == null) {
            return false;
        }

        String nombre = normalizarNombre(nombreCompleto);

        if (nombre.length() < 3 || nombre.length() > 120) {
            return false;
        }

        return nombre.matches("[\\p{L}]+\\.?(?:[ '\\u2019-][\\p{L}]+\\.?)*");
    }



    public static String normalizarNombre(String nombreCompleto) {
        if (nombreCompleto == null) {
            return null;
        }

        return nombreCompleto.trim().replaceAll("\\s+", " ");
    }
}
