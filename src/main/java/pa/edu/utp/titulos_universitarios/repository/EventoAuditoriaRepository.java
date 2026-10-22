package pa.edu.utp.titulos_universitarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pa.edu.utp.titulos_universitarios.entity.EventoAuditoria;

public interface EventoAuditoriaRepository
        extends JpaRepository<EventoAuditoria, Long> {
}
