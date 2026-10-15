 package pa.edu.utp.titulos_universitarios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TituloRepository extends JpaRepository<Titulo, Long> {

    List<Titulo> findByEstado(EstadoTitulo estado);

    long countByEstado(EstadoTitulo estado);

    boolean existsByGraduado_IdAndNombreTituloIgnoreCase(
            Long graduadoId,
            String nombreTitulo);
}