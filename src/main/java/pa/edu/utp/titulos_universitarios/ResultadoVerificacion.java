package pa.edu.utp.titulos_universitarios;

/**
 * T-3.1.2 (HU-3.1): los unicos datos que la verificacion publica muestra de un
 * titulo. La plantilla recibe este objeto y no la entidad Titulo, asi el
 * documento completo, quien aprobo y el id interno no pueden llegar a la pagina.
 *
 * Las fechas ya vienen como texto en espanol e ingles.
 */
public record ResultadoVerificacion(
        String graduado,
        String tipoDocumento,
        String documentoEnmascarado,
        String titulo,
        String institucion,
        String fechaEmisionEs,
        String fechaEmisionEn,
        String codigo,
        String fechaConsultaEs,
        String fechaConsultaEn) {
}
