package pa.edu.utp.titulos_universitarios;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Titulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreGraduado;
    private String nombreTitulo;
    private LocalDate fechaRegistro;

    public Titulo() {
    }

    public Titulo(String nombreGraduado, String nombreTitulo, LocalDate fechaRegistro) {
        this.nombreGraduado = nombreGraduado;
        this.nombreTitulo = nombreTitulo;
        this.fechaRegistro = fechaRegistro;
    }

    public Long getId() {
        return id;
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

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
