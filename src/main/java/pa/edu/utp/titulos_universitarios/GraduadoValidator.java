
package pa.edu.utp.titulos_universitarios;

import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

public class GraduadoValidator {

    private static final int PASAPORTE_MIN = 6;
    private static final int PASAPORTE_MAX = 12;

    private GraduadoValidator() {
    }

    public static boolean esDocumentoValido(
            TipoDocumento tipoDocumento,
            String numeroDocumento) {

        if (tipoDocumento == null || numeroDocumento == null) {
            return false;
        }

        String documento = numeroDocumento.trim();

        if (documento.isEmpty()) {
            return false;
        }

        return switch (tipoDocumento) {
            case CEDULA -> documento.matches("\\d+-\\d+-\\d+");

            case PASAPORTE ->
                    documento.length() >= PASAPORTE_MIN
                            && documento.length() <= PASAPORTE_MAX
                            && documento.matches("[A-Za-z0-9]+");
        };
    }

    public static boolean esNombreValido(String nombreCompleto) {
        if (nombreCompleto == null) {
            return false;
        }

        String nombre = nombreCompleto.trim();

        return nombre.matches(
                "[\\p{L}]+(?:[ '\\u2019-][\\p{L}]+)*"
                        + "(?:\\s+[\\p{L}]+(?:[ '\\u2019-][\\p{L}]+)*)*"
        );
    }

    public static String normalizarNombre(String nombreCompleto) {
        if (nombreCompleto == null) {
            return null;
        }

        return nombreCompleto.trim().replaceAll("\\s+", " ");
    }
}
