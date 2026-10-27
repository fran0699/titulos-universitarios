package pa.edu.utp.titulos_universitarios;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 * T-3.1.1 (HU-3.1): consulta publica de un titulo por su codigo de verificacion.
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

    private final TituloRepository tituloRepository;

    public VerificacionService(TituloRepository tituloRepository) {
        this.tituloRepository = tituloRepository;
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
}
