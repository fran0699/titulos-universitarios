package pa.edu.utp.titulos_universitarios;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;
import pa.edu.utp.titulos_universitarios.entity.EventoAuditoria;
import pa.edu.utp.titulos_universitarios.repository.EventoAuditoriaRepository;

/**
 * T-3.3.1 (HU-3.3): servicio del registro de auditoria.
 *
 * SOLO agrega eventos: no expone operaciones para editar ni borrar, porque los
 * eventos de auditoria son inmutables. La fecha y la hora las pone el servidor.
 * El usuario lo pasa quien llama, tomado de la sesion (nunca del formulario);
 * eso se conecta a registrar/aprobar/rechazar en T-3.3.2.
 */
@Service
public class AuditoriaService {

    private static final int USUARIO_MIN = 3;
    private static final int USUARIO_MAX = 50;
    private static final int DETALLE_MAX = 500;

    private final EventoAuditoriaRepository eventoAuditoriaRepository;

    public AuditoriaService(EventoAuditoriaRepository eventoAuditoriaRepository) {
        this.eventoAuditoriaRepository = eventoAuditoriaRepository;
    }

    /**
     * Agrega un evento de auditoria. La fecha y la hora las pone el servidor.
     *
     * @param accion   accion critica realizada (lista cerrada).
     * @param usuario  usuario de la sesion que realizo la accion (3-50).
     * @param tituloId titulo afectado (obligatorio).
     * @param detalle  descripcion de la accion (maximo 500 caracteres).
     * @return el evento ya guardado.
     */
    public EventoAuditoria registrarEvento(
            AccionAuditoria accion,
            String usuario,
            Long tituloId,
            String detalle) {

        if (accion == null) {
            throw new IllegalArgumentException(
                    "La accion del evento de auditoria es obligatoria.");
        }

        String usuarioNormalizado = usuario == null ? "" : usuario.strip();
        if (usuarioNormalizado.length() < USUARIO_MIN
                || usuarioNormalizado.length() > USUARIO_MAX) {
            throw new IllegalArgumentException(
                    "El usuario del evento debe tener entre 3 y 50 caracteres.");
        }

        if (tituloId == null) {
            throw new IllegalArgumentException(
                    "El titulo afectado por el evento es obligatorio.");
        }

        String detalleNormalizado = detalle == null ? "" : detalle.strip();
        if (detalleNormalizado.length() > DETALLE_MAX) {
            throw new IllegalArgumentException(
                    "El detalle del evento no puede superar los 500 caracteres.");
        }

        EventoAuditoria evento = new EventoAuditoria(
                LocalDateTime.now(),
                accion,
                usuarioNormalizado,
                tituloId,
                detalleNormalizado);
        return eventoAuditoriaRepository.save(evento);
    }
}
