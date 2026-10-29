package pa.edu.utp.titulos_universitarios.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

import pa.edu.utp.titulos_universitarios.entity.EventoAuditoria;

public interface EventoAuditoriaRepository
        extends Repository<EventoAuditoria, Long>,
        JpaSpecificationExecutor<EventoAuditoria> {

    <S extends EventoAuditoria> S save(S evento);
}