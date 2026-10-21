package pa.edu.utp.titulos_universitarios.entity;

public enum Rol {
    APROBADOR,
    REGISTRADOR,
    // T-4.10.1 (HU-4.10): usuario técnico de Prometheus; solo lee métricas.
    MONITOREO
}