package pa.edu.utp.titulos_universitarios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;
import pa.edu.utp.titulos_universitarios.entity.EventoAuditoria;
import pa.edu.utp.titulos_universitarios.repository.EventoAuditoriaRepository;

@Service
@Transactional(readOnly = true)
public class AuditoriaConsultaService {

    private static final int TAMANO_PAGINA = 20;

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
                desde == null ? null : desde.atStartOfDay();

        LocalDateTime fechaHasta =
                hasta == null ? null : hasta.plusDays(1).atStartOfDay();

        Specification<EventoAuditoria> especificacion =
                (root, query, cb) -> {

                    List<Predicate> predicados = new ArrayList<>();

                    if (fechaDesde != null) {
                        predicados.add(
                                cb.greaterThanOrEqualTo(
                                        root.<LocalDateTime>get("fechaHora"),
                                        fechaDesde
                                )
                        );
                    }

                    if (fechaHasta != null) {
                        predicados.add(
                                cb.lessThan(
                                        root.<LocalDateTime>get("fechaHora"),
                                        fechaHasta
                                )
                        );
                    }

                    if (accion != null) {
                        predicados.add(
                                cb.equal(root.get("accion"), accion)
                        );
                    }

                    if (usuarioNormalizado != null) {
                        predicados.add(
                                cb.like(
                                        cb.lower(root.get("usuario")),
                                        "%"
                                                + usuarioNormalizado
                                                .toLowerCase(Locale.ROOT)
                                                + "%"
                                )
                        );
                    }

                    return cb.and(
                            predicados.toArray(new Predicate[0])
                    );
                };

        Page<EventoAuditoria> eventos =
                auditoriaRepository.findAll(
                        especificacion,
                        PageRequest.of(
                                pagina - 1,
                                TAMANO_PAGINA,
                                Sort.by(
                                        Sort.Order.desc("fechaHora"),
                                        Sort.Order.desc("id")
                                )
                        )
                );

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