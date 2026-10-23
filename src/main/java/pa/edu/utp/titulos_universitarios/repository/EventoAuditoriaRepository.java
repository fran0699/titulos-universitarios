package pa.edu.utp.titulos_universitarios.repository;

import org.springframework.data.repository.Repository;
import pa.edu.utp.titulos_universitarios.entity.EventoAuditoria;

public interface EventoAuditoriaRepository
        extends Repository<EventoAuditoria, Long> {

    // HU-3.3 (CA4): solo agregar. Sin metodos para editar ni borrar.
    // Cuando HU-3.4 necesite consultar, se agregan metodos de lectura
    // (por ejemplo, findByTituloIdOrderByFechaHoraAsc).
    <S extends EventoAuditoria> S save(S evento);
}
