
package pa.edu.utp.titulos_universitarios;

public class TituloValidationException extends IllegalArgumentException {

    private final String campo;

    public TituloValidationException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
