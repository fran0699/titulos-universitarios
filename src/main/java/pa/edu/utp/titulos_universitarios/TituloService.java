
package pa.edu.utp.titulos_universitarios;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pa.edu.utp.titulos_universitarios.entity.AccionAuditoria;
import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;
import pa.edu.utp.titulos_universitarios.repository.GraduadoRepository;

@Service
public class TituloService {

    private final TituloRepository tituloRepository;
    private final GraduadoRepository graduadoRepository;
    private final IdentificadorGenerator identificadorGenerator;
    private final AuditoriaService auditoriaService;
    private final CredencialPdfService credencialPdfService;

    public TituloService(
            TituloRepository tituloRepository,
            GraduadoRepository graduadoRepository,
            IdentificadorGenerator identificadorGenerator,
            AuditoriaService auditoriaService,
            CredencialPdfService credencialPdfService) {
        this.tituloRepository = tituloRepository;
        this.graduadoRepository = graduadoRepository;
        this.identificadorGenerator = identificadorGenerator;
        this.auditoriaService = auditoriaService;
        this.credencialPdfService = credencialPdfService;
    }

    @Transactional
    public Titulo registrar(Titulo titulo, String usuario) {
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

        Titulo guardado = tituloRepository.save(titulo);

        // T-3.3.2 (HU-3.3): auditar el registro en la misma transaccion.
        auditoriaService.registrarEvento(
                AccionAuditoria.REGISTRAR,
                usuario,
                guardado.getId(),
                "Registró el título \"" + guardado.getNombreTitulo()
                        + "\" del graduado "
                        + guardado.getGraduado().getNombreCompleto());

        return guardado;
    }

    public List<Titulo> listar() {
        return tituloRepository.findAll();
    }

    @Transactional(readOnly = true)
    public byte[] generarCredencialPdf(Long id) {
        Titulo titulo = tituloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (titulo.getEstado() != EstadoTitulo.APROBADO) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        try {
            return credencialPdfService.generar(titulo);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar la credencial PDF.",
                    exception);
        }
    }

    public List<Titulo> listarPendientes() {
        return tituloRepository.findByEstado(EstadoTitulo.PENDIENTE);
    }

    // T-1.7.2 (HU-1.7): cantidad de titulos pendientes para el contador del menu.
    public long contarPendientes() {
        return tituloRepository.countByEstado(EstadoTitulo.PENDIENTE);
    }

    @Transactional
    public Titulo aprobar(Long id, String usuario) {
        Titulo titulo = obtenerPendienteParaRevision(id, usuario);

        registrarRevision(titulo, EstadoTitulo.APROBADO, usuario);

        titulo.setIdentificador(identificadorGenerator.generar());

        Titulo guardado = tituloRepository.save(titulo);

        // T-3.3.2 (HU-3.3): auditar la aprobacion en la misma transaccion.
        auditoriaService.registrarEvento(
                AccionAuditoria.APROBAR,
                usuario,
                guardado.getId(),
                "Aprobó el título \"" + guardado.getNombreTitulo() + "\"");

        return guardado;
    }

    @Transactional
    public Titulo rechazar(Long id, String usuario) {
        Titulo titulo = obtenerPendienteParaRevision(id, usuario);

        registrarRevision(titulo, EstadoTitulo.RECHAZADO, usuario);

        Titulo guardado = tituloRepository.save(titulo);

        // T-3.3.2 (HU-3.3): auditar el rechazo en la misma transaccion.
        auditoriaService.registrarEvento(
                AccionAuditoria.RECHAZAR,
                usuario,
                guardado.getId(),
                "Rechazó el título \"" + guardado.getNombreTitulo() + "\"");

        return guardado;
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
