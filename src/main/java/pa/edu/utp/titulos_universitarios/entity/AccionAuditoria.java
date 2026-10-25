package pa.edu.utp.titulos_universitarios.entity;

/**
 * T-3.3.1 (HU-3.3): acciones criticas que se auditan. Lista cerrada.
 * REVOCAR y SUSTITUIR se agregaran con HU-3.2.
 *
 * Nota: al sumar REVOCAR y SUSTITUIR, ddl-auto=update NO actualiza el
 * CHECK eventos_auditoria_accion_check de la base de datos; hay que
 * cambiarlo a mano, igual que paso con usuarios_rol_check en HU-4.10.
 */
public enum AccionAuditoria {
    REGISTRAR,
    APROBAR,
    RECHAZAR
}
