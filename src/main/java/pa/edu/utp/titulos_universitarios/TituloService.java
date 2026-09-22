package pa.edu.utp.titulos_universitarios;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TituloService {

    private final TituloRepository tituloRepository;

    public TituloService(TituloRepository tituloRepository) {
        this.tituloRepository = tituloRepository;
    }

    public Titulo registrar(Titulo titulo) {
        if (titulo == null || !TituloValidator.esValido(titulo.getNombreGraduado(), titulo.getNombreTitulo())) {
            throw new IllegalArgumentException("El nombre del graduado y el nombre del título son obligatorios.");
        }

        if (titulo.getFechaRegistro() == null) {
            titulo.setFechaRegistro(LocalDate.now());
        }

        return tituloRepository.save(titulo);
    }

    public List<Titulo> listar() {
        return tituloRepository.findAll();
    }
}
