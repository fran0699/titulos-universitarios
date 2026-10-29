package pa.edu.utp.titulos_universitarios;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

/** Genera la credencial PDF de un título aprobado. */
@Service
public class CredencialPdfService {

    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();
    private static final float CENTER = PAGE_WIDTH / 2;
    private static final float QR_SIDE_POINTS = 80;
    private static final float QR_BOTTOM = 138;
    private static final int QR_IMAGE_PIXELS = 300;
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
        if (urlVerificacion == null || urlVerificacion.isBlank()) {
            throw new IllegalArgumentException("La URL de verificación es obligatoria.");
        }
        this.urlVerificacion = urlVerificacion.trim().replaceAll("/+$", "");
        if (this.urlVerificacion.isEmpty()) {
            throw new IllegalArgumentException("La URL de verificación es obligatoria.");
        }
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

                TextoEnvuelto nombre = ajustarTexto(titulo.getNombreGraduado(), SERIF, 22, 430, 3);
                float nombreFinalY = dibujarNombre(contenido, nombre, 506);
                float separadorY = nombreFinalY - 21;
                dibujarSeparador(contenido, separadorY);
                float documentoY = separadorY - 17;
                centrar(contenido, textoDocumento(titulo), SERIF, 9, documentoY, 80, 87, 111);
                float introduccionY = documentoY - 35;
                centrar(contenido, "obtuvo el título de",
                        SERIF_ITALIC, 11, introduccionY, 80, 87, 111);
                TextoEnvuelto nombreTitulo = ajustarTexto(titulo.getNombreTitulo(), SERIF, 16, 440, 4);
                float tituloY = introduccionY - 30;
                float y = dibujarTitulo(contenido, nombreTitulo, tituloY);
                centrar(contenido,
                        "Fecha de emisión: " + titulo.getFechaRevision().toLocalDate().format(FECHA_EMISION),
                        SANS, 9, y - 7, 77, 96, 125);

                dibujarIdentificador(contenido, titulo.getIdentificador());
                dibujarCodigoQr(documento, contenido, urlCompleta(titulo.getIdentificador()));
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

    private float dibujarNombre(PDPageContentStream contenido, TextoEnvuelto texto, float y)
            throws IOException {
        float lineaY = y;
        for (String linea : texto.lineas()) {
            centrar(contenido, linea, SERIF, texto.tamano(), lineaY, 29, 61, 111);
            lineaY -= texto.saltoLinea();
        }
        return lineaY + texto.saltoLinea();
    }

    private float dibujarTitulo(PDPageContentStream contenido, TextoEnvuelto titulo, float y)
            throws IOException {
        float lineaY = y;
        for (String linea : titulo.lineas()) {
            centrar(contenido, linea, SERIF, titulo.tamano(), lineaY, 29, 40, 57);
            lineaY -= titulo.saltoLinea();
        }
        return lineaY;
    }

    private TextoEnvuelto ajustarTexto(
            String texto, PDFont fuente, float tamanoInicial, float anchoMaximo, int maxLineas)
            throws IOException {
        for (float tamano = tamanoInicial; tamano >= 10; tamano -= 0.5f) {
            List<String> lineas = envolver(texto, fuente, tamano, anchoMaximo);
            if (lineas.size() <= maxLineas) {
                return new TextoEnvuelto(tamano, tamano * 1.25f, lineas);
            }
        }
        throw new IllegalArgumentException("El texto excede el espacio disponible en la credencial.");
    }

    private List<String> envolver(String texto, PDFont fuente, float tamano, float anchoMaximo)
            throws IOException {
        List<String> lineas = new ArrayList<>();
        StringBuilder linea = new StringBuilder();
        for (String palabra : texto.trim().split("\\s+")) {
            boolean primerCaracter = true;
            for (int codigo : palabra.codePoints().toArray()) {
                String caracter = new String(Character.toChars(codigo));
                boolean agregarEspacio = primerCaracter && !linea.isEmpty();
                String candidata = linea + (agregarEspacio ? " " : "") + caracter;
                if (ancho(candidata, fuente, tamano) > anchoMaximo) {
                    if (linea.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Un carácter excede el espacio disponible en la credencial.");
                    }
                    lineas.add(linea.toString());
                    linea.setLength(0);
                    agregarEspacio = false;
                }
                if (agregarEspacio) {
                    linea.append(' ');
                }
                if (ancho(linea + caracter, fuente, tamano) > anchoMaximo) {
                    throw new IllegalArgumentException(
                            "Un carácter excede el espacio disponible en la credencial.");
                }
                linea.append(caracter);
                primerCaracter = false;
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
        float x = 64;
        texto(contenido, "Identificador", SANS_BOLD, 7, x, 206, 29, 40, 57);
        List<String> lineas = envolver(identificador, SANS, 7, 330);
        float y = 194;
        for (String linea : lineas) {
            texto(contenido, linea, SANS, 7, x, y, 29, 40, 57);
            y -= 10;
        }
    }

    private String urlCompleta(String identificador) {
        return urlVerificacion + "/" + identificador;
    }

    private void dibujarCodigoQr(
            PDDocument documento, PDPageContentStream contenido, String direccion) throws IOException {
        try {
            BitMatrix matriz = new QRCodeWriter().encode(
                    direccion, BarcodeFormat.QR_CODE, QR_IMAGE_PIXELS, QR_IMAGE_PIXELS);
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", png);
            PDImageXObject codigoQr =
                    PDImageXObject.createFromByteArray(documento, png.toByteArray(), "codigo-qr");
            float x = PAGE_WIDTH - 64 - QR_SIDE_POINTS;
            contenido.drawImage(codigoQr, x, QR_BOTTOM, QR_SIDE_POINTS, QR_SIDE_POINTS);
            String leyenda = "Escanee para verificar";
            float xLeyenda = x + (QR_SIDE_POINTS - ancho(leyenda, SANS, 7)) / 2;
            texto(contenido, leyenda, SANS, 7, xLeyenda, QR_BOTTOM - 12, 29, 40, 57);
        } catch (WriterException exception) {
            throw new IOException("No se pudo generar el código QR de la credencial.", exception);
        }
    }

    private void dibujarPie(PDPageContentStream contenido) throws IOException {
        String pie = "Verifique este título en " + urlVerificacion
                + " o escaneando el código QR";
        List<String> lineas = envolver(pie, SANS, 8, PAGE_WIDTH - 120);
        contenido.setStrokingColor(new Color(190, 202, 220));
        contenido.setLineWidth(0.6f);
        contenido.moveTo(64, 114);
        contenido.lineTo(PAGE_WIDTH - 64, 114);
        contenido.stroke();
        float y = 101;
        for (String linea : lineas) {
            centrar(contenido, linea, SANS, 8, y, 29, 61, 111);
            y -= 10;
        }
        centrar(contenido, "Documento académico. No constituye una firma electrónica certificada.",
                SANS, 7, y - 8, 77, 96, 125);
    }

    private record TextoEnvuelto(float tamano, float saltoLinea, List<String> lineas) {
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
