package pa.edu.utp.titulos_universitarios.repository;

import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;
import pa.edu.utp.titulos_universitarios.entity.EventoAuditoria;

/**
 * Registro de auditoría: solo se agregan eventos (HU-3.3) y se consultan
 * (HU-3.4).
 *
 * HU-3.3 (CA4) y HU-3.4 (CA7): no hay ninguna forma de editar ni borrar
 * eventos. Por eso este repositorio hereda de Repository, que no trae
 * métodos, y no de JpaRepository ni de JpaSpecificationExecutor: ambos
 * incluyen métodos delete y, el segundo, también update.
 */
public interface EventoAuditoriaRepository
        extends Repository<EventoAuditoria, Long> {

    // HU-3.3: agregar un evento.
    <S extends EventoAuditoria> S save(S evento);

    // HU-3.4: consulta de solo lectura con filtros y paginación, de lo más
    // reciente a lo más antiguo. Un filtro vacío llega con un valor que no
    // limita la búsqueda (ver AuditoriaConsultaService), así la consulta no
    // necesita comparar parámetros con null.
    @Query(value = """
            SELECT e FROM EventoAuditoria e
            WHERE e.fechaHora >= :desde
              AND e.fechaHora < :hasta
              AND e.accion IN :acciones
              AND LOWER(e.usuario) LIKE :usuario
            ORDER BY e.fechaHora DESC, e.id DESC
            """,
            countQuery = """
            SELECT COUNT(e) FROM EventoAuditoria e
            WHERE e.fechaHora >= :desde
              AND e.fechaHora < :hasta
              AND e.accion IN :acciones
              AND LOWER(e.usuario) LIKE :usuario
            """)
    Page<EventoAuditoria> buscar(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("acciones") Collection<AccionAuditoria> acciones,
            @Param("usuario") String usuario,
            Pageable pageable);
}
