package pa.edu.utp.titulos_universitarios;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pa.edu.utp.titulos_universitarios.entity.Graduado;

/**
 * T-3.1.1 y T-3.1.2 (HU-3.1): consulta publica de un titulo por su codigo.
 *
 * Es de solo lectura. Un codigo con formato invalido no llega a la base de datos,
 * y un codigo inexistente y el de un titulo que no esta APROBADO dan el mismo
 * resultado (vacio), para no revelar que codigos existen.
 */
@Service
public class VerificacionService {

    /** UUID de 36 caracteres (8-4-4-4-12 hexadecimales, en minusculas). */
    private static final Pattern UUID_36 = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    private static final char OCULTO = '•'; // •
    private static final int VISIBLES = 4;

    private static final ZoneId PANAMA = ZoneId.of("America/Panama");
    private static final Locale ES = Locale.forLanguageTag("es-PA");

    // Misma forma que la credencial: «10 de octubre de 2026».
    private static final DateTimeFormatter FECHA_ES =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", ES);
    private static final DateTimeFormatter FECHA_EN =
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter FECHA_HORA_ES =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, HH:mm", ES);
    private static final DateTimeFormatter FECHA_HORA_EN =
            DateTimeFormatter.ofPattern("MMMM d, yyyy, HH:mm", Locale.ENGLISH);

    private final TituloRepository tituloRepository;
    private final Clock reloj;
    private final String institucion;

    public VerificacionService(
            TituloRepository tituloRepository,
            Clock reloj,
            @Value("${titulos.institucion.nombre}") String institucion) {
        this.tituloRepository = tituloRepository;
        this.reloj = reloj;
        this.institucion = institucion;
    }

    public String getInstitucion() {
        return institucion;
    }

    /** Quita espacios al inicio y al final y pasa a minusculas. Nunca devuelve null. */
    public static String normalizar(String codigo) {
        return codigo == null ? "" : codigo.strip().toLowerCase(Locale.ROOT);
    }

    /** Indica si el codigo, ya normalizado, tiene forma de UUID de 36 caracteres. */
    public static boolean esFormatoValido(String codigoNormalizado) {
        return codigoNormalizado != null
                && codigoNormalizado.length() == 36
                && UUID_36.matcher(codigoNormalizado).matches();
    }

    /**
     * Oculta todos los caracteres del documento salvo los ultimos 4 que no sean
     * guion; los guiones quedan en su lugar.
     * 8-123-4567 -> •-•••-4567, PE-5-614 -> ••-5-614, PA1234567 -> •••••4567.
     */
    public static String enmascarar(String documento) {
        if (documento == null) {
            return "";
        }
        char[] caracteres = documento.toCharArray();
        int visibles = 0;
        for (int i = caracteres.length - 1; i >= 0; i--) {
            if (caracteres[i] == '-') {
                continue;
            }
            if (visibles < VISIBLES) {
                visibles++;
            } else {
                caracteres[i] = OCULTO;
            }
        }
        return new String(caracteres);
    }

    /**
     * Busca el titulo APROBADO que corresponde al codigo.
     *
     * @return el titulo, o vacio si el formato es invalido, el codigo no existe
     *         o el titulo no esta APROBADO.
     */
    public Optional<Titulo> buscarAprobado(String codigo) {
        String normalizado = normalizar(codigo);
        if (!esFormatoValido(normalizado)) {
            return Optional.empty();
        }
        return tituloRepository.findByIdentificador(normalizado)
                .filter(titulo -> titulo.getEstado() == EstadoTitulo.APROBADO);
    }

    /**
     * Resultado publico de la consulta: solo los datos minimos, con el documento
     * enmascarado y las fechas en espanol e ingles.
     */
    @Transactional(readOnly = true)
    public Optional<ResultadoVerificacion> consultar(String codigo) {
        return buscarAprobado(codigo)
                .filter(VerificacionService::tieneDatosParaVerificar)
                .map(this::aResultado);
    }

    /**
     * Un titulo sin graduado o sin fecha de aprobacion (filas anteriores a que
     * esos datos fueran obligatorios) no se puede mostrar completo: se responde
     * "No encontrado" en lugar de romper la pagina publica.
     */
    private static boolean tieneDatosParaVerificar(Titulo titulo) {
        return titulo.getGraduado() != null
                && titulo.getGraduado().getTipoDocumento() != null
                && titulo.getFechaRevision() != null;
    }

    private ResultadoVerificacion aResultado(Titulo titulo) {
        Graduado graduado = titulo.getGraduado();
        // La fecha de emision es la de aprobacion, igual que en la credencial.
        LocalDate emision = titulo.getFechaRevision().toLocalDate();
        ZonedDateTime consulta = ZonedDateTime.now(reloj).withZoneSameInstant(PANAMA);

        return new ResultadoVerificacion(
                titulo.getNombreGraduado(),
                graduado.getTipoDocumento().getEtiqueta(),
                enmascarar(graduado.getNumeroDocumento()),
                titulo.getNombreTitulo(),
                institucion,
                emision.format(FECHA_ES),
                emision.format(FECHA_EN),
                titulo.getIdentificador(),
                consulta.format(FECHA_HORA_ES),
                consulta.format(FECHA_HORA_EN));
    }
}
