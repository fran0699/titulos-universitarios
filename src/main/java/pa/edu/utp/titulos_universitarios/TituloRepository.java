package pa.edu.utp.titulos_universitarios;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TituloRepository extends JpaRepository<Titulo, Long> {

    List<Titulo> findByEstado(EstadoTitulo estado);

    long countByEstado(EstadoTitulo estado);

    /** T-3.1.1 (HU-3.1): busca un título por su código de verificación. */
    Optional<Titulo> findByIdentificador(String identificador);

    boolean existsByGraduado_IdAndNombreTituloIgnoreCase(
            Long graduadoId,
            String nombreTitulo
    );
}