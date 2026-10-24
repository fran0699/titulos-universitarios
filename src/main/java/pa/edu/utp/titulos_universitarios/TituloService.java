
package pa.edu.utp.titulos_universitarios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;
import pa.edu.utp.titulos_universitarios.repository.GraduadoRepository;

@Service
public class TituloService {

    private final TituloRepository tituloRepository;
    private final GraduadoRepository graduadoRepository;
    private final IdentificadorGenerator identificadorGenerator;

    public TituloService(
            TituloRepository tituloRepository,
            GraduadoRepository graduadoRepository,
            IdentificadorGenerator identificadorGenerator) {
        this.tituloRepository = tituloRepository;
        this.graduadoRepository = graduadoRepository;
        this.identificadorGenerator = identificadorGenerator;
    }

    @Transactional
    public Titulo registrar(Titulo titulo) {
        if (titulo == null
                || !TituloValidator.esValido(
                titulo.getNombreGraduado(),
                titulo.getNombreTitulo())) {
            throw new TituloValidationException(
                    null,
                    "El nombre del graduado y el nombre del título son obligatorios.");
        }

        Graduado datosGraduado = titulo.getGraduado();

        if (datosGraduado == null) {
            throw new TituloValidationException(
                    "numeroDocumento",
                    "Debe proporcionar el documento del graduado.");
        }

        TipoDocumento tipoDocumento = datosGraduado.getTipoDocumento();
        String numeroDocumento = datosGraduado.getNumeroDocumento();

        String nombreFormulario =
                GraduadoValidator.normalizarNombre(
                        titulo.getNombreGraduado());

        if (!GraduadoValidator.esDocumentoValido(
                tipoDocumento, numeroDocumento)) {
            throw new TituloValidationException(
                    "numeroDocumento",
                    "El tipo o número de documento del graduado no es válido.");
        }

        if (!GraduadoValidator.esNombreValido(nombreFormulario)) {
            throw new TituloValidationException(
                    "nombreGraduado",
                    "El nombre del graduado no tiene un formato válido.");
        }

        String documentoNormalizado =
                GraduadoValidator.normalizarDocumento(
                        tipoDocumento, numeroDocumento);

        if (documentoNormalizado == null) {
            throw new TituloValidationException(
                    "numeroDocumento",
                    "El tipo o número de documento del graduado no es válido.");
        }

        String nombreTituloNormalizado =
                titulo.getNombreTitulo().trim().replaceAll("\\s+", " ");

        Graduado graduado = graduadoRepository
                .findByTipoDocumentoAndNumeroDocumento(
                        tipoDocumento, documentoNormalizado)
                .orElse(null);

        boolean graduadoNuevo = graduado == null;

        if (graduadoNuevo) {
            graduado = new Graduado(
                    tipoDocumento,
                    documentoNormalizado,
                    nombreFormulario);
        } else {
            String nombreExistente =
                    GraduadoValidator.normalizarNombre(
                            graduado.getNombreCompleto());

            if (!nombreExistente.equalsIgnoreCase(nombreFormulario)) {
                throw new TituloValidationException(
                        "nombreGraduado",
                        "El documento ya está registrado con otro nombre.");
            }

            if (tituloRepository
                    .existsByGraduado_IdAndNombreTituloIgnoreCase(
                            graduado.getId(),
                            nombreTituloNormalizado)) {
                throw new TituloValidationException(
                        "nombreTitulo",
                        "Este graduado ya tiene registrado ese título.");
            }
        }

        if (graduadoNuevo) {
            graduado = graduadoRepository.save(graduado);
        }

        titulo.setGraduado(graduado);
        titulo.setNombreGraduado(
                GraduadoValidator.normalizarNombre(
                        graduado.getNombreCompleto()));
        titulo.setNombreTitulo(nombreTituloNormalizado);

        titulo.setFechaRegistro(LocalDate.now());
        titulo.setEstado(EstadoTitulo.PENDIENTE);
        titulo.setRevisadoPor(null);
        titulo.setFechaRevision(null);
        titulo.setIdentificador(null);

        return tituloRepository.save(titulo);
    }

    public List<Titulo> listar() {
        return tituloRepository.findAll();
    }

    public List<Titulo> listarPendientes() {
        return tituloRepository.findByEstado(EstadoTitulo.PENDIENTE);
    }

    // T-1.7.2 (HU-1.7): cantidad de titulos pendientes para el contador del menu.
    public long contarPendientes() {
        return tituloRepository.countByEstado(EstadoTitulo.PENDIENTE);
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
            throw new IllegalArgumentException(
                    "El usuario que revisa el título es obligatorio.");
        }

        Titulo titulo = tituloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el título con id " + id + "."));

        if (titulo.getEstado() != EstadoTitulo.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se puede revisar un título en estado PENDIENTE.");
        }

        return titulo;
    }

    private void registrarRevision(
            Titulo titulo,
            EstadoTitulo nuevoEstado,
            String usuario) {
        titulo.setEstado(nuevoEstado);
        titulo.setRevisadoPor(usuario);
        titulo.setFechaRevision(LocalDateTime.now());
    }
}
