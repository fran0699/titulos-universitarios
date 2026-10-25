package pa.edu.utp.titulos_universitarios;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

/** Genera la credencial PDF de un título aprobado. */
@Service
public class CredencialPdfService {

    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();
    private static final float CENTER = PAGE_WIDTH / 2;
    private static final DateTimeFormatter FECHA_EMISION =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "PA"));
    private static final PDFont SERIF = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
    private static final PDFont SERIF_BOLD = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
    private static final PDFont SERIF_ITALIC = new PDType1Font(Standard14Fonts.FontName.TIMES_ITALIC);
    private static final PDFont SANS = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont SANS_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private final String urlVerificacion;

    public CredencialPdfService(
            @Value("${titulos.verificacion.url:http://localhost:8080/verificar}") String urlVerificacion) {
        this.urlVerificacion = urlVerificacion;
    }

    public byte[] generar(Titulo titulo) throws IOException {
        validarTitulo(titulo);

        try (PDDocument documento = new PDDocument();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PDPage pagina = new PDPage(PDRectangle.LETTER);
            documento.addPage(pagina);
            documento.getDocumentInformation().setTitle("Credencial de título universitario");

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                dibujarBordes(contenido);
                dibujarBirrete(contenido);
                centrar(contenido, "UNIVERSIDAD TECNOLÓGICA DE PANAMÁ", SANS_BOLD, 11, 645, 29, 61, 111);
                centrar(contenido, "Secretaría General · Registro de títulos", SANS, 8, 630, 77, 96, 125);
                centrar(contenido, "CREDENCIAL DE TÍTULO UNIVERSITARIO",
                        SANS_BOLD, 8, 582, 20, 68, 255);
                centrar(contenido, "Se certifica que", SERIF_ITALIC, 11, 542, 80, 87, 111);

                dibujarNombre(contenido, titulo.getNombreGraduado(), 506);
                dibujarSeparador(contenido, 461);
                centrar(contenido, textoDocumento(titulo), SERIF, 9, 444, 80, 87, 111);
                centrar(contenido, "obtuvo el título de", SERIF_ITALIC, 11, 408, 80, 87, 111);
                float y = dibujarTitulo(contenido, titulo.getNombreTitulo(), 378);
                centrar(contenido,
                        "Fecha de emisión: " + titulo.getFechaRevision().toLocalDate().format(FECHA_EMISION),
                        SANS, 9, y - 10, 77, 96, 125);

                dibujarIdentificador(contenido, titulo.getIdentificador());
                dibujarPie(contenido);
            }

            documento.save(salida);
            return salida.toByteArray();
        }
    }

    private void validarTitulo(Titulo titulo) {
        if (titulo == null) {
            throw new IllegalArgumentException("Debe indicar el título para generar la credencial.");
        }
        if (titulo.getEstado() != EstadoTitulo.APROBADO) {
            throw new IllegalStateException("Solo se genera credencial para títulos aprobados.");
        }
        if (titulo.getGraduado() == null
                || titulo.getGraduado().getTipoDocumento() == null
                || esVacio(titulo.getGraduado().getNumeroDocumento())
                || esVacio(titulo.getNombreGraduado())
                || esVacio(titulo.getNombreTitulo())
                || titulo.getFechaRevision() == null
                || esVacio(titulo.getIdentificador())) {
            throw new IllegalArgumentException(
                    "El título aprobado no tiene todos los datos requeridos para la credencial.");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String textoDocumento(Titulo titulo) {
        TipoDocumento tipo = titulo.getGraduado().getTipoDocumento();
        String etiqueta = tipo == TipoDocumento.CEDULA
                ? "con cédula de identidad personal N.º "
                : "con pasaporte N.º ";
        return etiqueta + titulo.getGraduado().getNumeroDocumento();
    }

    private void dibujarBordes(PDPageContentStream contenido) throws IOException {
        contenido.setStrokingColor(new Color(29, 61, 111));
        contenido.setLineWidth(2);
        contenido.addRect(38, 38, PAGE_WIDTH - 76, PAGE_HEIGHT - 76);
        contenido.stroke();

        contenido.setLineWidth(0.7f);
        contenido.addRect(44, 44, PAGE_WIDTH - 88, PAGE_HEIGHT - 88);
        contenido.stroke();
    }

    private void dibujarBirrete(PDPageContentStream contenido) throws IOException {
        float x = CENTER;
        float y = 684;
        contenido.setStrokingColor(new Color(29, 61, 111));
        contenido.setLineWidth(2);
        contenido.moveTo(x - 17, y);
        contenido.lineTo(x, y + 9);
        contenido.lineTo(x + 17, y);
        contenido.lineTo(x, y - 9);
        contenido.closePath();
        contenido.stroke();

        contenido.moveTo(x - 9, y - 5);
        contenido.lineTo(x - 9, y - 13);
        contenido.curveTo(x - 4, y - 19, x + 4, y - 19, x + 9, y - 13);
        contenido.lineTo(x + 9, y - 5);
        contenido.stroke();

        contenido.moveTo(x + 17, y);
        contenido.lineTo(x + 17, y - 13);
        contenido.lineTo(x + 20, y - 17);
        contenido.stroke();
    }

    private void dibujarNombre(PDPageContentStream contenido, String nombre, float y) throws IOException {
        List<String> lineas = envolver(nombre, SERIF, 22, 430);
        float lineaY = y;
        for (String linea : lineas) {
            centrar(contenido, linea, SERIF, 22, lineaY, 29, 61, 111);
            lineaY -= 27;
        }
    }

    private float dibujarTitulo(PDPageContentStream contenido, String titulo, float y) throws IOException {
        List<String> lineas = envolver(titulo, SERIF, 16, 440);
        float lineaY = y;
        for (String linea : lineas) {
            centrar(contenido, linea, SERIF, 16, lineaY, 29, 40, 57);
            lineaY -= 21;
        }
        return lineaY;
    }

    private List<String> envolver(String texto, PDFont fuente, float tamano, float anchoMaximo)
            throws IOException {
        List<String> lineas = new ArrayList<>();
        StringBuilder linea = new StringBuilder();
        for (String palabra : texto.trim().split("\\s+")) {
            String candidata = linea.isEmpty() ? palabra : linea + " " + palabra;
            if (!linea.isEmpty() && ancho(candidata, fuente, tamano) > anchoMaximo) {
                lineas.add(linea.toString());
                linea.setLength(0);
                linea.append(palabra);
            } else {
                if (!linea.isEmpty()) {
                    linea.append(' ');
                }
                linea.append(palabra);
            }
        }
        if (!linea.isEmpty()) {
            lineas.add(linea.toString());
        }
        return lineas;
    }

    private void dibujarSeparador(PDPageContentStream contenido, float y) throws IOException {
        contenido.setStrokingColor(new Color(190, 202, 220));
        contenido.setLineWidth(0.8f);
        contenido.moveTo(142, y);
        contenido.lineTo(PAGE_WIDTH - 142, y);
        contenido.stroke();
    }

    private void dibujarIdentificador(PDPageContentStream contenido, String identificador)
            throws IOException {
        float x = 88;
        texto(contenido, "Identificador", SANS_BOLD, 7, x, 178, 29, 40, 57);
        List<String> lineas = envolver(identificador, SANS, 7, 420);
        float y = 166;
        for (String linea : lineas) {
            texto(contenido, linea, SANS, 7, x, y, 29, 40, 57);
            y -= 10;
        }
    }

    private void dibujarPie(PDPageContentStream contenido) throws IOException {
        centrar(contenido, "Verifique este título en " + urlVerificacion,
                SANS, 8, 140, 29, 61, 111);
        contenido.setStrokingColor(new Color(190, 202, 220));
        contenido.setLineWidth(0.6f);
        contenido.moveTo(88, 126);
        contenido.lineTo(PAGE_WIDTH - 88, 126);
        contenido.stroke();
        centrar(contenido, "Documento académico. No constituye una firma electrónica certificada.",
                SANS, 7, 106, 77, 96, 125);
    }

    private void centrar(PDPageContentStream contenido, String valor, PDFont fuente, float tamano,
                         float y, int rojo, int verde, int azul) throws IOException {
        float x = CENTER - ancho(valor, fuente, tamano) / 2;
        texto(contenido, valor, fuente, tamano, x, y, rojo, verde, azul);
    }

    private float ancho(String valor, PDFont fuente, float tamano) throws IOException {
        return fuente.getStringWidth(valor) / 1000 * tamano;
    }

    private void texto(PDPageContentStream contenido, String valor, PDFont fuente, float tamano,
                       float x, float y, int rojo, int verde, int azul) throws IOException {
        contenido.beginText();
        contenido.setFont(fuente, tamano);
        contenido.setNonStrokingColor(new Color(rojo, verde, azul));
        contenido.newLineAtOffset(x, y);
        contenido.showText(valor);
        contenido.endText();
    }
}
