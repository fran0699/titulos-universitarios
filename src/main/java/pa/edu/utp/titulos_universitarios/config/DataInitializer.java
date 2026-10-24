package pa.edu.utp.titulos_universitarios.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import pa.edu.utp.titulos_universitarios.entity.Usuario;
import pa.edu.utp.titulos_universitarios.entity.Rol;
import pa.edu.utp.titulos_universitarios.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsurios(
            UsuarioRepository repo,
            PasswordEncoder encoder,
            UsuarioMonitoreoInitializer usuarioMonitoreo) {
        return args -> {
            if(repo.count() == 0) {
                Usuario aprobador = new Usuario();
                aprobador.setUsername("aprobador1");
                aprobador.setPassword(encoder.encode("clave1234"));
                aprobador.setRol(Rol.APROBADOR);
                repo.save(aprobador);

                Usuario registrador = new Usuario();
                registrador.setUsername("registrador1");
                registrador.setPassword(encoder.encode("clave1234"));
                registrador.setRol(Rol.REGISTRADOR);
                repo.save(registrador);

            }

            // T-4.10.1 (HU-4.10): el usuario técnico se crea después de los
            // usuarios de prueba (ese bloque solo corre con la tabla vacía) y
            // se crea aunque la tabla ya tenga usuarios.
            usuarioMonitoreo.crearSiNoExiste();
        };
    }

}