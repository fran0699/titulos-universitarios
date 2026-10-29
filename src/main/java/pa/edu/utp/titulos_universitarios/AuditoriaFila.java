
package pa.edu.utp.titulos_universitarios;

import java.time.LocalDateTime;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;

public record AuditoriaFila(
        LocalDateTime fechaHora,
        String usuario,
        AccionAuditoria accion,
        String tituloAfectado,
        String detalle
) {
}
