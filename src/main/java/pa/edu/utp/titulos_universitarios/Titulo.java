    package pa.edu.utp.titulos_universitarios;

    import jakarta.persistence.Entity;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import java.time.LocalDate;
    import jakarta.persistence.EnumType;
    import jakarta.persistence.Enumerated;
    import java.time.LocalDateTime;
    import jakarta.persistence.FetchType;
    import jakarta.persistence.JoinColumn;
    import jakarta.persistence.ManyToOne;
    import pa.edu.utp.titulos_universitarios.entity.Graduado;
    import jakarta.persistence.Column;
    import jakarta.persistence.Table;


    @Entity
    public class Titulo {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "nombre_graduado", nullable = false, length = 120)
        private String nombreGraduado;

        @Column(name = "nombre_titulo", nullable = false, length = 150)
        private String nombreTitulo;

        @Column(name = "fecha_registro", nullable = false)
        private LocalDate fechaRegistro;

        @Column(name = "identificador", length = 100)
        private String identificador;

        public Titulo() {
        }

        public Titulo(String nombreGraduado, String nombreTitulo, LocalDate fechaRegistro) {
            this.nombreGraduado = nombreGraduado;
            this.nombreTitulo = nombreTitulo;
            this.fechaRegistro = fechaRegistro;
        }


        @ManyToOne(fetch = FetchType.LAZY)

        @JoinColumn(name = "graduado_id", nullable = false)
        private Graduado graduado;

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

        public String getIdentificador() { return identificador; }
        public void setIdentificador(String identificador) { this.identificador = identificador; }

        @Enumerated(EnumType.STRING)
        private EstadoTitulo estado;

        private String revisadoPor;
        private LocalDateTime fechaRevision;

        public EstadoTitulo getEstado() {
            return estado;
        }

        public void setEstado(EstadoTitulo estado) {
            this.estado = estado;
        }

        public String getRevisadoPor() {
            return revisadoPor;
        }

        public void setRevisadoPor(String revisadoPor) {
            this.revisadoPor = revisadoPor;
        }

        public LocalDateTime getFechaRevision() {
            return fechaRevision;
        }

        public void setFechaRevision(LocalDateTime fechaRevision) {
            this.fechaRevision = fechaRevision;
        }

        public Graduado getGraduado() { return graduado; }

        public void setGraduado(Graduado graduado) { this.graduado = graduado; }

    }
