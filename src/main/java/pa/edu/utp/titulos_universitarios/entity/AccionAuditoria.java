package pa.edu.utp.titulos_universitarios.entity;

/**
 * Acciones críticas que se registran en la auditoría.
 * Los nombres de los valores se conservan para no modificar los
 * valores almacenados en la base de datos.
 */
public enum AccionAuditoria {
    REGISTRAR,
    APROBAR,
    RECHAZAR;

    public String getEtiqueta() {
        return switch (this) {
            case REGISTRAR -> "Registro";
            case APROBAR -> "Aprobación";
            case RECHAZAR -> "Rechazo";
        };
    }
}