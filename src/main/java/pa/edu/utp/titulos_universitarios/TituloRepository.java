

package pa.edu.utp.titulos_universitarios;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TituloRepository extends JpaRepository<Titulo, Long> {

    List<Titulo> findByEstado(EstadoTitulo estado);

    long countByEstado(EstadoTitulo estado);

    /** T-3.1.1 (HU-3.1): busca un título por su código de verificación. */
    Optional<Titulo> findByIdentificador(String identificador);

    boolean existsByGraduado_IdAndNombreTituloIgnoreCase(
            Long graduadoId,
            String nombreTitulo
    );

    /**
     * T-1.8.1 (HU-1.8): búsqueda paginada. Igual que en la auditoría (HU-3.4),
     * un filtro vacío llega con un valor que no limita la búsqueda ("%" para
     * el texto y todos los estados), así la consulta no compara con null.
     *
     * El texto llega ya en minúsculas y con los "%" puestos (ver
     * TituloConsultaService). JOIN FETCH trae el graduado en la misma
     * consulta; el conteo no lo necesita.
     */
    @Query(value = """
            SELECT t FROM Titulo t JOIN FETCH t.graduado g
            WHERE (LOWER(t.nombreGraduado) LIKE :texto
                   OR LOWER(g.numeroDocumento) LIKE :texto
                   OR LOWER(t.nombreTitulo) LIKE :texto)
              AND t.estado IN :estados
            ORDER BY t.fechaRegistro DESC, t.id DESC
            """,
            countQuery = """
            SELECT COUNT(t) FROM Titulo t JOIN t.graduado g
            WHERE (LOWER(t.nombreGraduado) LIKE :texto
                   OR LOWER(g.numeroDocumento) LIKE :texto
                   OR LOWER(t.nombreTitulo) LIKE :texto)
              AND t.estado IN :estados
            """)
    Page<Titulo> buscar(
            @Param("texto") String texto,
            @Param("estados") Collection<EstadoTitulo> estados,
            Pageable pageable);
}