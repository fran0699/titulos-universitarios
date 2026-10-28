package pa.edu.utp.titulos_universitarios;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;

import pa.edu.utp.titulos_universitarios.entity.Graduado;
import pa.edu.utp.titulos_universitarios.entity.TipoDocumento;

class CredencialPdfServiceTest {

    private final CredencialPdfService servicio =
            new CredencialPdfService("http://localhost:8080/verificar");

    @Test
    void generaPdfConDatosDelGraduadoYConservaTildes() throws Exception {
        byte[] pdf = servicio.generar(tituloAprobado());

        try (var documento = Loader.loadPDF(pdf)) {
            String texto = new PDFTextStripper().getText(documento);

            assertTrue(texto.contains("UNIVERSIDAD TECNOLÓGICA DE PANAMÁ"));
            assertTrue(texto.contains("Secretaría General"));
            assertTrue(texto.contains("José Núñez Agüero"));
            assertTrue(texto.contains("CREDENCIAL DE TÍTULO UNIVERSITARIO"));
            assertTrue(texto.contains("cédula de identidad personal N.º 8-123-456"));
            assertTrue(texto.contains("Licenciatura en Ingeniería de Software"));
            assertTrue(texto.contains("Fecha de emisión: 21 de octubre de 2026"));
            assertTrue(texto.contains("ID-CREDENCIAL-123"));
            assertTrue(texto.contains("No constituye una firma electrónica certificada."));
            assertFalse(texto.contains("SHA-256"));
            assertTrue(texto.contains("Verifique este título en http://localhost:8080/verificar"));
            assertTrue(texto.contains("o escaneando el código QR"));
            assertTrue(texto.contains("Escanee para verificar"));
        }
    }

    @Test
    void elQrContieneSoloLaUrlDeVerificacionYEsLegible() throws Exception {
        Titulo titulo = tituloAprobado();
        titulo.setNombreGraduado("Nombre privado");
        titulo.getGraduado().setNombreCompleto("Nombre privado");

        byte[] pdf = servicio.generar(titulo);
        String urlLeida = decodificarQr(pdf);

        assertEquals("http://localhost:8080/verificar/ID-CREDENCIAL-123", urlLeida);
        assertFalse(urlLeida.contains("Nombre privado"));
        assertFalse(urlLeida.contains(titulo.getGraduado().getNumeroDocumento()));
        assertTrue(ladoQrEnPuntos(pdf) >= 72);
    }

    @Test
    void eliminaLaBarraFinalDeLaUrlBaseAntesDeAgregarElIdentificador() throws Exception {
        CredencialPdfService servicioConBarraFinal =
                new CredencialPdfService("https://ejemplo.test/verificar/");

        assertEquals(
                "https://ejemplo.test/verificar/ID-CREDENCIAL-123",
                decodificarQr(servicioConBarraFinal.generar(tituloAprobado())));
    }

    @Test
    void centraLaLeyendaDebajoDelQr() throws Exception {
        byte[] pdf = servicio.generar(tituloAprobado());

        try (PDDocument documento = Loader.loadPDF(pdf)) {
            CajasStripper stripper = new CajasStripper();
            stripper.getText(documento);
            CajaTexto leyenda = stripper.cajas.stream()
                    .filter(caja -> caja.texto().contains("Escanee para verificar"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No se encontró la leyenda del QR."));
            float centroQr = PDRectangle.LETTER.getWidth() - 64 - 80 + 40;
            float centroLeyenda = (leyenda.left() + leyenda.right()) / 2;

            assertEquals(centroQr, centroLeyenda, 0.5f,
                    "La leyenda debe quedar centrada respecto al QR.");
            assertTrue(leyenda.top() > PDRectangle.LETTER.getHeight() - 138,
                    "La leyenda debe quedar debajo del QR.");
        }
    }

    @Test
    void admiteNombreYTituloMaximosEnUnaPaginaSinSolaparTextoNiQr() throws Exception {
        Titulo titulo = tituloAprobado();
        String nombreMaximo = "María ".repeat(19) + "Maríaa";
        String tituloMaximo = "Ingeniería ".repeat(13) + "ciencia";
        assertEquals(120, nombreMaximo.length());
        assertEquals(150, tituloMaximo.length());
        titulo.setNombreGraduado(nombreMaximo);
        titulo.setNombreTitulo(tituloMaximo);

        byte[] pdf = servicio.generar(titulo);

        try (var documento = Loader.loadPDF(pdf)) {
            assertEquals(1, documento.getNumberOfPages());
            String texto = new PDFTextStripper().getText(documento);
            assertTrue(texto.contains("María"));
            assertTrue(texto.contains("ciencia"));
            assertTrue(texto.contains("Fecha de emisión"));
            assertNoSeSolapanTextosNiQr(documento);
            assertNombreNoSeMontaSobreSeparador(documento);
        }
        assertEquals("http://localhost:8080/verificar/ID-CREDENCIAL-123", decodificarQr(pdf));
    }

    @Test
    void rechazaTituloQueNoEstaAprobado() {
        Titulo titulo = tituloAprobado();
        titulo.setEstado(EstadoTitulo.PENDIENTE);

        assertThrows(IllegalStateException.class, () -> servicio.generar(titulo));
    }

    @Test
    void rechazaTituloAprobadoSinIdentificador() {
        Titulo titulo = tituloAprobado();
        titulo.setIdentificador(" ");

        assertThrows(IllegalArgumentException.class, () -> servicio.generar(titulo));
    }

    @Test
    void generaPdfValidoParaTituloAprobado() {
        assertDoesNotThrow(() -> servicio.generar(tituloAprobado()));
    }

    private Titulo tituloAprobado() {
        Titulo titulo = new Titulo();
        titulo.setNombreGraduado("José Núñez Agüero");
        titulo.setNombreTitulo("Licenciatura en Ingeniería de Software");
        titulo.setEstado(EstadoTitulo.APROBADO);
        titulo.setFechaRevision(LocalDateTime.of(2026, 10, 21, 10, 30));
        titulo.setIdentificador("ID-CREDENCIAL-123");
        titulo.setGraduado(new Graduado(TipoDocumento.CEDULA, "8-123-456", "José Núñez Agüero"));
        return titulo;
    }

    private String decodificarQr(byte[] pdf) throws Exception {
        try (var documento = Loader.loadPDF(pdf)) {
            BufferedImage pagina = new PDFRenderer(documento).renderImageWithDPI(0, 200);
            float escala = 200 / 72f;
            int x = Math.round((612 - 64 - 80) * escala) - 10;
            int y = Math.round((792 - 138 - 80) * escala) - 10;
            int lado = Math.round(80 * escala) + 20;
            BufferedImage recorte = pagina.getSubimage(x, y, lado, lado);
            BinaryBitmap bitmap = new BinaryBitmap(
                    new HybridBinarizer(new BufferedImageLuminanceSource(recorte)));
            return new MultiFormatReader().decode(bitmap).getText();
        }
    }

    private float ladoQrEnPuntos(byte[] pdf) throws Exception {
        try (PDDocument documento = Loader.loadPDF(pdf)) {
            PDFStreamParser parser = new PDFStreamParser(documento.getPage(0));
            List<Object> tokens = parser.parse();
            for (int i = 6; i < tokens.size(); i++) {
                if (tokens.get(i) instanceof Operator operator && "cm".equals(operator.getName())) {
                    float ancho = ((COSNumber) tokens.get(i - 6)).floatValue();
                    float alto = ((COSNumber) tokens.get(i - 3)).floatValue();
                    float inclinacionX = ((COSNumber) tokens.get(i - 5)).floatValue();
                    float inclinacionY = ((COSNumber) tokens.get(i - 4)).floatValue();
                    if (Math.abs(inclinacionX) < 0.01f
                            && Math.abs(inclinacionY) < 0.01f
                            && Math.abs(ancho - alto) < 0.01f) {
                        return ancho;
                    }
                }
            }
        }
        throw new AssertionError("El PDF no contiene una imagen QR cuadrada.");
    }

    private void assertNoSeSolapanTextosNiQr(PDDocument documento) throws IOException {
        CajasStripper stripper = new CajasStripper();
        stripper.getText(documento);
        List<CajaTexto> cajas = stripper.cajas;
        for (int i = 0; i < cajas.size(); i++) {
            CajaTexto actual = cajas.get(i);
            assertFalse(actual.left() < 548 && actual.right() > 468
                            && actual.top() < 654 && actual.bottom() > 574,
                    "Un texto se superpone con el QR: " + actual.texto());
            for (int j = i + 1; j < cajas.size(); j++) {
                CajaTexto otra = cajas.get(j);
                boolean solapaHorizontal = actual.left() < otra.right()
                        && otra.left() < actual.right();
                boolean solapaVertical = actual.top() < otra.bottom()
                        && otra.top() < actual.bottom();
                assertFalse(solapaHorizontal && solapaVertical,
                        "Se solapan los textos: " + actual.texto() + " / " + otra.texto());
            }
        }
    }

    private void assertNombreNoSeMontaSobreSeparador(PDDocument documento) throws IOException {
        CajasStripper stripper = new CajasStripper();
        stripper.getText(documento);
        float finalNombre = stripper.cajas.stream()
                .filter(caja -> caja.texto().contains("María"))
                .map(CajaTexto::bottom)
                .max(Float::compareTo)
                .orElseThrow();
        float separadorY = separadorY(documento);
        float separadorDesdeArriba = PDRectangle.LETTER.getHeight() - separadorY;
        assertTrue(finalNombre < separadorDesdeArriba,
                "El nombre se monta sobre la línea separadora.");
    }

    private float separadorY(PDDocument documento) throws IOException {
        List<Object> tokens = new PDFStreamParser(documento.getPage(0)).parse();
        for (int i = 2; i + 3 < tokens.size(); i++) {
            if (tokens.get(i) instanceof Operator operador
                    && "m".equals(operador.getName())
                    && tokens.get(i - 2) instanceof COSNumber x1
                    && tokens.get(i - 1) instanceof COSNumber y1
                    && tokens.get(i + 3) instanceof Operator siguiente
                    && "l".equals(siguiente.getName())
                    && tokens.get(i + 1) instanceof COSNumber x2
                    && tokens.get(i + 2) instanceof COSNumber y2
                    && Math.abs(x1.floatValue() - 142) < 0.01f
                    && Math.abs(x2.floatValue() - (612 - 142)) < 0.01f
                    && Math.abs(y1.floatValue() - y2.floatValue()) < 0.01f) {
                return y1.floatValue();
            }
        }
        throw new AssertionError("El PDF no contiene la línea separadora esperada.");
    }

    private record CajaTexto(float left, float right, float top, float bottom, String texto) {
    }

    private static final class CajasStripper extends PDFTextStripper {
        private final List<CajaTexto> cajas = new ArrayList<>();

        private CajasStripper() throws IOException {
        }

        @Override
        protected void writeString(String texto, List<TextPosition> posiciones) throws IOException {
            if (!posiciones.isEmpty()) {
                float left = posiciones.stream().map(TextPosition::getXDirAdj)
                        .min(Float::compareTo).orElseThrow();
                float right = posiciones.stream()
                        .map(posicion -> posicion.getXDirAdj() + posicion.getWidthDirAdj())
                        .max(Float::compareTo).orElseThrow();
                float top = posiciones.stream().map(TextPosition::getYDirAdj)
                        .min(Float::compareTo).orElseThrow();
                float bottom = posiciones.stream()
                        .map(posicion -> posicion.getYDirAdj() + posicion.getHeightDir())
                        .max(Float::compareTo).orElseThrow();
                cajas.add(new CajaTexto(left, right, top, bottom, texto));
            }
            super.writeString(texto, posiciones);
        }
    }
}
