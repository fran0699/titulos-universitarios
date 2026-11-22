package pa.edu.utp.titulos_universitarios;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * T-1.8.1 (HU-1.8): búsqueda de títulos con filtro por estado, 20 por página.
 *
 * Sigue el diseño de AuditoriaConsultaService (HU-3.4): primero valida y,
 * solo si todo es válido, consulta la base de datos (CA4). La consulta trae
 * únicamente la página pedida (CA6).
 */
@Service
@Transactional(readOnly = true)
public class TituloConsultaService {

    public static final int TAMANO_PAGINA = 20;

    private static final int LONGITUD_MINIMA_BUSQUEDA = 2;
    private static final int LONGITUD_MAXIMA_BUSQUEDA = 100;
    private static final String CUALQUIER_TEXTO = "%";

    private final TituloRepository tituloRepository;

    public TituloConsultaService(TituloRepository tituloRepository) {
        this.tituloRepository = tituloRepository;
    }

    /**
     * @param texto  texto a buscar (nombre del graduado, documento o nombre
     *               del título); vacío o null = sin búsqueda
     * @param estado estado a filtrar; null = «Todos»
     * @param pagina número de página, desde 1
     * @throws TituloValidationException si la búsqueda o la página no son válidas
     */
    public Page<Titulo> buscar(String texto, EstadoTitulo estado, int pagina) {

        // Se valida todo antes de tocar la base de datos.
        String busqueda = normalizarBusqueda(texto);

        if (pagina < 1) {
            throw new TituloValidationException(
                    "pagina",
                    "El número de página debe ser mayor o igual a 1.");
        }

        // No distingue mayúsculas (se compara en minúsculas), pero tildes si la chequea.
        String patron = busqueda == null
                ? CUALQUIER_TEXTO
                : "%" + busqueda.toLowerCase(Locale.ROOT) + "%";

        List<EstadoTitulo> estados = estado == null
                ? List.of(EstadoTitulo.values())
                : List.of(estado);

        return tituloRepository.buscar(
                patron,
                estados,
                PageRequest.of(pagina - 1, TAMANO_PAGINA));
    }

    private String normalizarBusqueda(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        String valor = texto.strip();

        if (valor.length() < LONGITUD_MINIMA_BUSQUEDA
                || valor.length() > LONGITUD_MAXIMA_BUSQUEDA) {
            throw new TituloValidationException(
                    "q",
                    "La búsqueda debe tener entre 2 y 100 caracteres.");
        }

        return valor;
    }
}