package pa.edu.utp.titulos_universitarios.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;
import pa.edu.utp.titulos_universitarios.entity.EventoAuditoria;

public interface EventoAuditoriaRepository
        extends Repository<EventoAuditoria, Long> {

    // HU-3.3: permite registrar eventos de auditoría.
    <S extends EventoAuditoria> S save(S evento);

    // HU-3.4: consulta de solo lectura con filtros y paginación.
    @Query(
            value = """
            SELECT e
            FROM EventoAuditoria e
            WHERE (CAST(:desde AS LocalDateTime) IS NULL
                   OR e.fechaHora >= :desde)
              AND (CAST(:hasta AS LocalDateTime) IS NULL
                   OR e.fechaHora < :hasta)
              AND (CAST(:accion AS string) IS NULL
                   OR e.accion = :accion)
              AND (CAST(:usuario AS string) IS NULL
                   OR LOWER(e.usuario) LIKE
                      LOWER(CONCAT('%', CAST(:usuario AS string), '%')))
            ORDER BY e.fechaHora DESC, e.id DESC
            """,
            countQuery = """
            SELECT COUNT(e)
            FROM EventoAuditoria e
            WHERE (CAST(:desde AS LocalDateTime) IS NULL
                   OR e.fechaHora >= :desde)
              AND (CAST(:hasta AS LocalDateTime) IS NULL
                   OR e.fechaHora < :hasta)
              AND (CAST(:accion AS string) IS NULL
                   OR e.accion = :accion)
              AND (CAST(:usuario AS string) IS NULL
                   OR LOWER(e.usuario) LIKE
                      LOWER(CONCAT('%', CAST(:usuario AS string), '%')))
            """
    )
    Page<EventoAuditoria> buscarConFiltros(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("accion") AccionAuditoria accion,
            @Param("usuario") String usuario,
            Pageable pageable
    );
}