package pa.edu.utp.titulos_universitarios;

import org.springframework.stereotype.Service;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;

@Service
public class AuditoriaService {

    public void registrarEvento(
            AccionAuditoria accion,
            String usuario,
            Long tituloId,
            String detalle) {
        // Se mantiene como operación de auditoría no bloqueante.
    }
}
