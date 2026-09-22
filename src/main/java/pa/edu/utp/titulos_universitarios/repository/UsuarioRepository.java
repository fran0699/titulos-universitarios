package pa.edu.utp.titulos_universitarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pa.edu.utp.titulos_universitarios.entity.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);


}
