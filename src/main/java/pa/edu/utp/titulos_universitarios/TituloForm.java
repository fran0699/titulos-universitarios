
package pa.edu.utp.titulos_universitarios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

public class TituloForm {

    @NotNull(message = "Seleccione el tipo de documento.")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio.")
    @Size(min = 5, max = 20,
            message = "El documento debe tener entre 5 y 20 caracteres.")
    private String numeroDocumento;

    @NotBlank(message = "El nombre del graduado es obligatorio.")
    @Size(min = 3, max = 120,
            message = "El nombre del graduado debe tener entre 3 y 120 caracteres.")
    @Pattern(
            regexp = "[\\p{L}]+\\.?([ '\\u2019-][\\p{L}]+\\.?)*",
            message = "El nombre contiene caracteres no permitidos."
    )
    private String nombreGraduado;

    @NotBlank(message = "El nombre del título es obligatorio.")
    @Size(min = 5, max = 150,
            message = "El nombre del título debe tener entre 5 y 150 caracteres.")
    private String nombreTitulo;

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNombreGraduado() {
        return nombreGraduado;
    }

    public void setNombreGraduado(String nombreGraduado) {
        this.nombreGraduado = nombreGraduado;
    }

    public String getNombreTitulo() {
        return nombreTitulo;
    }

    public void setNombreTitulo(String nombreTitulo) {
        this.nombreTitulo = nombreTitulo;
    }
}
