package pa.edu.utp.titulos_universitarios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;
import pa.edu.utp.titulos_universitarios.entity.EventoAuditoria;
import pa.edu.utp.titulos_universitarios.repository.EventoAuditoriaRepository;

/**
 * HU-3.4: consulta de solo lectura del registro de auditoría, con filtros
 * por fechas, acción y usuario, 20 eventos por página.
 */
@Service
@Transactional(readOnly = true)
public class AuditoriaConsultaService {

    private static final int TAMANO_PAGINA = 20;

    // Valores que no limitan la búsqueda cuando un filtro llega vacío.
    private static final LocalDateTime SIN_FECHA_INICIAL = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime SIN_FECHA_FINAL = LocalDateTime.of(9999, 1, 1, 0, 0);
    private static final String CUALQUIER_USUARIO = "%";

    private final EventoAuditoriaRepository auditoriaRepository;
    private final TituloRepository tituloRepository;

    public AuditoriaConsultaService(
            EventoAuditoriaRepository auditoriaRepository,
            TituloRepository tituloRepository) {

        this.auditoriaRepository = auditoriaRepository;
        this.tituloRepository = tituloRepository;
    }

    public Page<AuditoriaFila> buscar(
            LocalDate desde,
            LocalDate hasta,
            AccionAuditoria accion,
            String usuario,
            int pagina) {

        validarFechas(desde, hasta);

        if (pagina < 1) {
            throw new IllegalArgumentException(
                    "El número de página debe ser mayor o igual a 1.");
        }

        String usuarioNormalizado = normalizarUsuario(usuario);

        LocalDateTime fechaDesde =
                desde == null ? SIN_FECHA_INICIAL : desde.atStartOfDay();

        // «Hasta» incluye todo ese día: se busca antes del inicio del día siguiente.
        LocalDateTime fechaHasta =
                hasta == null ? SIN_FECHA_FINAL : hasta.plusDays(1).atStartOfDay();

        List<AccionAuditoria> acciones =
                accion == null ? List.of(AccionAuditoria.values()) : List.of(accion);

        // El usuario se busca por texto contenido, sin distinguir mayúsculas.
        String patronUsuario = usuarioNormalizado == null
                ? CUALQUIER_USUARIO
                : "%" + usuarioNormalizado.toLowerCase(Locale.ROOT) + "%";

        Page<EventoAuditoria> eventos = auditoriaRepository.buscar(
                fechaDesde,
                fechaHasta,
                acciones,
                patronUsuario,
                PageRequest.of(pagina - 1, TAMANO_PAGINA));

        List<Long> idsTitulos = eventos.getContent().stream()
                .map(EventoAuditoria::getTituloId)
                .distinct()
                .toList();

        Map<Long, Titulo> titulosPorId =
                tituloRepository.findAllById(idsTitulos).stream()
                        .collect(Collectors.toMap(
                                Titulo::getId,
                                Function.identity()
                        ));

        return eventos.map(evento -> {
            Titulo titulo = titulosPorId.get(evento.getTituloId());

            String tituloAfectado = titulo == null
                    ? "Título no disponible"
                    : titulo.getNombreGraduado() + " · "
                    + titulo.getNombreTitulo();

            return new AuditoriaFila(
                    evento.getFechaHora(),
                    evento.getUsuario(),
                    evento.getAccion(),
                    tituloAfectado,
                    evento.getDetalle()
            );
        });
    }

    private void validarFechas(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final.");
        }
    }

    private String normalizarUsuario(String usuario) {
        if (usuario == null || usuario.isBlank()) {
            return null;
        }

        String valor = usuario.strip();

        if (valor.length() < 3 || valor.length() > 50) {
            throw new IllegalArgumentException(
                    "El usuario debe tener entre 3 y 50 caracteres.");
        }

        return valor;
    }
}
