package pa.edu.utp.titulos_universitarios.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/verificar/**").permitAll()
                .requestMatchers("/actuator/**").hasRole("APROBADOR")
                // T-2.1.4 (HU-2.1): la revision de titulos (ver pendientes y
                // aprobar/rechazar) es exclusiva del rol APROBADOR.
                .requestMatchers("/titulos/pendientes",
                        "/titulos/*/aprobar",
                        "/titulos/*/rechazar").hasRole("APROBADOR")
                .anyRequest().authenticated()
        )
                .formLogin(form -> form.permitAll())
                // T-4.3.1 (HU-4.3): cabeceras de seguridad HTTP senaladas por Nikto.
                .headers(headers -> headers
                        // Content-Security-Policy: solo recursos propios. Se permiten estilos
                        // inline porque la vista y la pagina de login usan style="..." inline;
                        // los scripts quedan restringidos a 'self' (la app no usa JS inline).
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; "
                                + "script-src 'self'; "
                                + "style-src 'self' 'unsafe-inline'; "
                                + "img-src 'self' data:; "
                                + "form-action 'self'; "
                                + "frame-ancestors 'none'; "
                                + "base-uri 'self'"))
                        // Referrer-Policy: no filtrar la URL completa a sitios externos.
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        // Permissions-Policy: deshabilita APIs del navegador que la app no usa.
                        .permissionsPolicyHeader(permissions -> permissions.policy(
                                "geolocation=(), camera=(), microphone=(), payment=()"))
                        // HSTS: fuerza HTTPS por un ano (solo se emite sobre conexiones seguras).
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)));

        return http.build();
    }
}
