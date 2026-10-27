package pa.edu.utp.titulos_universitarios.security;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * T-4.10.2 (HU-4.10): cadena 1, solo para /actuator/**. Prometheus entra
     * con HTTP Basic y el usuario técnico (rol MONITOREO). No crea sesión:
     * cada petición trae sus credenciales. Ningún rol del personal lee
     * métricas (mínimo privilegio).
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/actuator/**")
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasRole("MONITOREO"))
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        aplicarCabecerasDeSeguridad(http);
        return http.build();
    }

    /**
     * Cadena 2: todas las demás rutas (las pantallas). Solo inicio de sesión
     * con formulario; desde T-4.10.2 ya no acepta HTTP Basic.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/verificar/**").permitAll()
                        // T-4.10.2 (HU-4.10): /error queda accesible para que el código
                        // original (401 o 403) llegue tal cual al cliente. Spring Boot no
                        // muestra detalles técnicos en esa página por defecto.
                        .requestMatchers("/error").permitAll()
                        // T-4.11.1 (HU-4.11, CA6): el ícono de la pestaña es público.
                        // Sin esta regla, con sesión el navegador recibía 404 y la
                        // consola (F12) mostraba un error en cada pantalla.
                        .requestMatchers("/favicon.ico").permitAll()
                        // T-2.1.4 (HU-2.1): la revision de titulos (ver pendientes y
                        // aprobar/rechazar) es exclusiva del rol APROBADOR.
                        .requestMatchers("/titulos/pendientes",
                                "/titulos/*/aprobar",
                                "/titulos/*/rechazar").hasRole("APROBADOR")
                        // T-1.9.2 (HU-1.9): solo el REGISTRADOR abre y envia el formulario
                        // de registro. /titulos/nuevo cubre tanto el GET como el POST, asi
                        // un aprobador no puede registrar ni enviando el POST directamente.
                        .requestMatchers("/titulos/nuevo").hasRole("REGISTRADOR")
                        // T-4.10.2 (HU-4.10): denegar por defecto. Toda ruta que no
                        // aparezca arriba (/titulos, "/", los estilos y las pantallas
                        // que se agreguen en el futuro) es solo para el personal; el
                        // usuario técnico (MONITOREO) recibe 403. Una ruta pública
                        // nueva debe declararse arriba con permitAll().
                        .anyRequest().hasAnyRole("REGISTRADOR", "APROBADOR")
                )
                .formLogin(form -> form.permitAll())
                // T-4.11.1 (HU-4.11): al iniciar sesión solo se vuelve a una página.
                .requestCache(cache -> cache.requestCache(paginaGuardada()));
        aplicarCabecerasDeSeguridad(http);
        return http.build();
    }

    /**
     * T-4.11.1 (HU-4.11): decide qué petición sin sesión se recuerda como
     * «página a la que volver» después de iniciar sesión.
     *
     * Solo se recuerda una página: una petición GET que el navegador pide como
     * HTML (encabezado Accept con text/html), y nunca una dirección de
     * /.well-known/. Con F12 abierto, Chrome pide por su cuenta
     * /.well-known/appspecific/com.chrome.devtools.json aceptando cualquier
     * formato; el criterio por defecto de Spring Security la recordaba y, al
     * iniciar sesión, la persona terminaba en un error 404.
     */
    private RequestCache paginaGuardada() {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(request ->
                "GET".equals(request.getMethod())
                        && !esDeWellKnown(request)
                        && pideHtml(request));
        return requestCache;
    }

    private static boolean esDeWellKnown(HttpServletRequest request) {
        return request.getRequestURI().startsWith(request.getContextPath() + "/.well-known/");
    }

    private static boolean pideHtml(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.toLowerCase(Locale.ROOT).contains("text/html");
    }

    /**
     * T-4.3.1 (HU-4.3): cabeceras de seguridad HTTP señaladas por Nikto. Desde
     * T-4.10.2 se aplican a las dos cadenas, para que /actuator/** también
     * las envíe.
     */
    private void aplicarCabecerasDeSeguridad(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
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
    }
}