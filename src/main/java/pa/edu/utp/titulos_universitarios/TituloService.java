package pa.edu.utp.titulos_universitarios;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class TituloService {

    private final TituloRepository tituloRepository;
    private final IdentificadorGenerator identificadorGenerator;

    public TituloService(TituloRepository tituloRepository, IdentificadorGenerator identificadorGenerator) {
        this.tituloRepository = tituloRepository;
        this.identificadorGenerator = identificadorGenerator;
    }

    public Titulo registrar(Titulo titulo) {
        if (titulo == null || !TituloValidator.esValido(titulo.getNombreGraduado(), titulo.getNombreTitulo())) {
            throw new IllegalArgumentException("El nombre del graduado y el nombre del título son obligatorios.");
        }

        if (titulo.getFechaRegistro() == null) {
            titulo.setFechaRegistro(LocalDate.now());
        }

        titulo.setEstado(EstadoTitulo.PENDIENTE);
        titulo.setRevisadoPor(null);
        titulo.setFechaRevision(null);

        return tituloRepository.save(titulo);
    }

    public List<Titulo> listar() {
        return tituloRepository.findAll();
    }

    public Titulo aprobar(Long id, String usuario) {
        Titulo titulo = obtenerPendienteParaRevision(id, usuario);
        registrarRevision(titulo, EstadoTitulo.APROBADO, usuario);
        titulo.setIdentificador(identificadorGenerator.generar());
        return tituloRepository.save(titulo);
    }

    public Titulo rechazar(Long id, String usuario) {
        Titulo titulo = obtenerPendienteParaRevision(id, usuario);
        registrarRevision(titulo, EstadoTitulo.RECHAZADO, usuario);
        return tituloRepository.save(titulo);
    }

    private Titulo obtenerPendienteParaRevision(Long id, String usuario) {
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("El usuario que revisa el título es obligatorio.");
        }

        Titulo titulo = tituloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el título con id " + id + "."));

        if (titulo.getEstado() != EstadoTitulo.PENDIENTE) {
            throw new IllegalStateException("Solo se puede revisar un título en estado PENDIENTE.");
        }
        return titulo;
    }

    private void registrarRevision(Titulo titulo, EstadoTitulo nuevoEstado, String usuario) {
        titulo.setEstado(nuevoEstado);
        titulo.setRevisadoPor(usuario);
        titulo.setFechaRevision(LocalDateTime.now());
    }
}
