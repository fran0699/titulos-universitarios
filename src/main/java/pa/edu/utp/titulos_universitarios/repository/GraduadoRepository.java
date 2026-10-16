
package pa.edu.utp.titulos_universitarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

public interface GraduadoRepository
        extends JpaRepository<Graduado, Long> {

    Optional<Graduado> findByTipoDocumentoAndNumeroDocumento(
            TipoDocumento tipoDocumento,
            String numeroDocumento);

    boolean existsByTipoDocumentoAndNumeroDocumento(
            TipoDocumento tipoDocumento,
            String numeroDocumento);
}
