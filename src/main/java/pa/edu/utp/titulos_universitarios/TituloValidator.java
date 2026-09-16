package pa.edu.utp.titulos_universitarios;

public class TituloValidator {

    public static boolean esValido(String nombreGraduado, String nombreTitulo) {
        return nombreGraduado != null && !nombreGraduado.isBlank()
                && nombreTitulo != null && !nombreTitulo.isBlank();
    }
}