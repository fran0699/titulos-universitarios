package pa.edu.utp.titulos_universitarios.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import pa.edu.utp.titulos_universitarios.entity.Rol;
import pa.edu.utp.titulos_universitarios.entity.Usuario;
import pa.edu.utp.titulos_universitarios.repository.UsuarioRepository;

/**
 * T-4.10.1 (HU-4.10): crea el usuario técnico que Prometheus usa para leer
 * las métricas. La contraseña llega por la variable de entorno
 * PROMETHEUS_PASSWORD y nunca se guarda en el repositorio.
 */
@Component
public class UsuarioMonitoreoInitializer {

    private static final Logger log = LoggerFactory.getLogger(UsuarioMonitoreoInitializer.class);

    /** Longitud mínima acordada para la contraseña del usuario técnico. */
    static final int LONGITUD_MINIMA_PASSWORD = 12;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String usuario;
    private final String password;

    public UsuarioMonitoreoInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${titulos.monitoreo.usuario:prometheus}") String usuario,
            @Value("${titulos.monitoreo.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuario = usuario;
        this.password = password;
    }

    /**
     * Crea el usuario técnico si todavía no existe, aunque la tabla ya tenga
     * otros usuarios. Devuelve true solo si lo creó.
     */
    public boolean crearSiNoExiste() {
        if (password == null || password.length() < LONGITUD_MINIMA_PASSWORD) {
            log.warn("No se creó el usuario técnico de monitoreo: defina la variable de entorno "
                    + "PROMETHEUS_PASSWORD con al menos {} caracteres.", LONGITUD_MINIMA_PASSWORD);
            return false;
        }
        if (usuarioRepository.findByUsername(usuario).isPresent()) {
            return false;
        }
        Usuario monitoreo = new Usuario();
        monitoreo.setUsername(usuario);
        monitoreo.setPassword(passwordEncoder.encode(password));
        monitoreo.setRol(Rol.MONITOREO);
        usuarioRepository.save(monitoreo);
        log.info("Usuario técnico de monitoreo '{}' creado.", usuario);
        return true;
    }
}