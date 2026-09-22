package pa.edu.utp.titulos_universitarios.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import pa.edu.utp.titulos_universitarios.entity.Usuario;
import pa.edu.utp.titulos_universitarios.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario u = usuarioRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("No existe" + username));

        return User.builder()
                .username(u.getUsername())
                .password(u.getPassword())
                .roles(u.getRol().name())
                .build();

    }
}
