package pa.edu.utp.titulos_universitarios;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IdentificadorGenerator {

    public String generar() {
        return UUID.randomUUID().toString();
    }
}
