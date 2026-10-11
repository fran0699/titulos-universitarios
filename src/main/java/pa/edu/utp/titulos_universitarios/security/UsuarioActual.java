package pa.edu.utp.titulos_universitarios.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import pa.edu.utp.titulos_universitarios.entity.Rol;

/**
 * T-1.7.1 (HU-1.7): usuario con sesion y su rol, tal como lo muestra el
 * encabezado comun de las pantallas.
 */
public class UsuarioActual {

    private final String nombre;
    private final Rol rol;

    public UsuarioActual(String nombre, Rol rol) {
        this.nombre = nombre;
        this.rol = rol;
    }

    /**
     * Crea el usuario actual a partir de la sesion de Spring Security.
     * Devuelve null si no hay sesion, si es un usuario anonimo o si no tiene
     * un rol conocido.
     */
    public static UsuarioActual desde(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        for (GrantedAuthority autoridad : authentication.getAuthorities()) {
            for (Rol rol : Rol.values()) {
                if (("ROLE_" + rol.name()).equals(autoridad.getAuthority())) {
                    return new UsuarioActual(authentication.getName(), rol);
                }
            }
        }
        return null;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRolTexto() {
        return switch (rol) {
            case APROBADOR -> "Aprobador";
            case REGISTRADOR -> "Registrador";
            default -> rol.name();
        };
    }

    public boolean isAprobador() {
        return rol == Rol.APROBADOR;
    }

    public boolean isRegistrador() {
        return rol == Rol.REGISTRADOR;
    }
}