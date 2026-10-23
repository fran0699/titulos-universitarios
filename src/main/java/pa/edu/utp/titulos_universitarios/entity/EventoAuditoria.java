package pa.edu.utp.titulos_universitarios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * T-3.3.1 (HU-3.3): evento del registro de auditoria de las acciones criticas
 * sobre un titulo (registrar, aprobar, rechazar).
 *
 * Los eventos son inmutables: se construyen una sola vez y no tienen setters.
 * Las columnas son updatable=false para que, aunque alguien modifique la
 * entidad por error, Hibernate nunca emita un UPDATE sobre un evento guardado.
 */
@Entity
@Table(name = "eventos_auditoria")
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, updatable = false, length = 20)
    private AccionAuditoria accion;

    @Column(name = "usuario", nullable = false, updatable = false, length = 50)
    private String usuario;

    @Column(name = "titulo_id", nullable = false, updatable = false)
    private Long tituloId;

    @Column(name = "detalle", nullable = false, updatable = false, length = 500)
    private String detalle;

    protected EventoAuditoria() {
        // Requerido por JPA.
    }

    public EventoAuditoria(
            LocalDateTime fechaHora,
            AccionAuditoria accion,
            String usuario,
            Long tituloId,
            String detalle) {
        this.fechaHora = fechaHora;
        this.accion = accion;
        this.usuario = usuario;
        this.tituloId = tituloId;
        this.detalle = detalle;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public AccionAuditoria getAccion() {
        return accion;
    }

    public String getUsuario() {
        return usuario;
    }

    public Long getTituloId() {
        return tituloId;
    }

    public String getDetalle() {
        return detalle;
    }
}
