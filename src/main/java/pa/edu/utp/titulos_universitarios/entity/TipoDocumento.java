
package pa.edu.utp.titulos_universitarios.entity;

public enum TipoDocumento {
    CEDULA("Cédula"),
    PASAPORTE("Pasaporte");

    private final String etiqueta;

    TipoDocumento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
