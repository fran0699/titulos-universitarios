# Investigación: generación de PDF y QR

**Historia:** HU-2.3 — Investigación de Apache PDFBox y ZXing  
**Fecha de consulta:** 8 de octubre de 2026  
**Contexto del proyecto:** Java 21, Maven y Spring Boot.

## Resumen

La combinación es viable para una prueba de concepto: PDFBox crea el documento PDF y ZXing genera el QR como una matriz de módulos que se convierte a PNG y se inserta en el PDF. Las bibliotecas se pueden probar en un proyecto Maven aislado sin agregar dependencias al `pom.xml` de la aplicación.

Versiones estables consultadas:

| Biblioteca | Versión | Uso | Java mínimo publicado |
|---|---:|---|---:|
| Apache PDFBox | 3.0.8 | Crear y guardar el PDF | Java 8 |
| ZXing | 3.5.4 | Codificar el QR (`core`) y convertirlo a imagen (`javase`) | Java 8 desde ZXing 3.4 |

El JDK 21 del proyecto supera los mínimos publicados. PDFBox y ZXing usan licencia Apache 2.0. ZXing indica que está en modo de mantenimiento, con cambios centrados en correcciones y mejoras menores.

## Dependencias Maven para una prueba aislada

Estas dependencias son para el proyecto de prueba de concepto; no se han añadido al `pom.xml` principal.

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>3.0.8</version>
    </dependency>
    <dependency>
        <groupId>com.google.zxing</groupId>
        <artifactId>core</artifactId>
        <version>3.5.4</version>
    </dependency>
    <dependency>
        <groupId>com.google.zxing</groupId>
        <artifactId>javase</artifactId>
        <version>3.5.4</version>
    </dependency>
</dependencies>
```

`core` contiene el codificador QR. `javase` aporta utilidades para escribir el `BitMatrix` como PNG mediante `MatrixToImageWriter`.

## Flujo propuesto para la prueba de concepto

1. Crear un texto de prueba para el QR, preferiblemente una URL pública de verificación con un identificador opaco.
2. Generar una matriz QR con `QRCodeWriter`.
3. Convertir la matriz a PNG en memoria con `MatrixToImageWriter`.
4. Crear un `PDDocument`, agregar una página y escribir los datos de muestra con `PDPageContentStream`.
5. Cargar el PNG con `PDImageXObject.createFromByteArray`, dibujarlo en la página y guardar el PDF.
6. Abrir el PDF y probar el QR con un lector de teléfono.

Ejemplo Java reducido, válido como base para la POC:

```java
BitMatrix matrix = new QRCodeWriter().encode(
        "https://ejemplo.test/verificar/identificador-de-prueba",
        BarcodeFormat.QR_CODE,
        300,
        300);

ByteArrayOutputStream png = new ByteArrayOutputStream();
MatrixToImageWriter.writeToStream(matrix, "PNG", png);

try (PDDocument pdf = new PDDocument()) {
    PDPage page = new PDPage(PDRectangle.LETTER);
    pdf.addPage(page);

    PDImageXObject qr = PDImageXObject.createFromByteArray(
            pdf, png.toByteArray(), "codigo-qr");

    try (PDPageContentStream content = new PDPageContentStream(pdf, page)) {
        content.drawImage(qr, 420, 500, 120, 120);
    }

    pdf.save(Path.of("titulo-prueba.pdf").toFile());
}
```

El ejemplo se centra en insertar la imagen QR. Para escribir campos de texto se agrega texto con `beginText`, `setFont`, `newLineAtOffset`, `showText` y `endText` en el mismo `PDPageContentStream`.

## Consideraciones y posibles problemas

- **Fuentes y tildes:** las fuentes estándar Type 1 no cubren de forma general Unicode. Para nombres como `José` o `María`, se debe probar con una fuente TTF/OTF embebida mediante `PDType0Font`; revisar también su licencia de redistribución. Sin ello, algunos caracteres pueden fallar o renderizarse incorrectamente.
- **Contraste y tamaño del QR:** conservar el margen blanco (quiet zone) que genera ZXing, usar alto contraste y probar impresión/escaneo con el tamaño real. Evitar estirar el QR de forma no proporcional.
- **Contenido del QR:** no incluir nombre, número de identificación ni datos personales directamente en el QR. Preferir un identificador no predecible o URL de verificación que el servidor pueda validar y revocar.
- **Verificación real pendiente:** el código del proyecto ya tiene `Titulo.identificador` y `IdentificadorGenerator`, pero no se encontró un controlador de verificación `/verificar/{id}`. El QR de la POC debe usar un valor de ejemplo; integrarlo con verificación real requiere esa ruta y reglas de autorización.
- **Cambios de PDFBox 2 a 3:** usar documentación 3.x. Parte de la API de carga de documentos cambió entre versiones principales; no copiar ejemplos antiguos sin comprobarlos.
- **Errores a manejar:** `WriterException` al codificar el QR e `IOException` al escribir imagen/PDF. La POC debe fallar claramente si el texto está vacío o supera la capacidad QR esperada.
- **Tamaño y rendimiento:** generar el QR en memoria evita archivos PNG temporales; para muchos documentos, medir memoria y tiempo antes de procesar lotes grandes.
- **PDF no es PDF417:** PDFBox crea documentos PDF. QR es la simbología elegida por la historia; ZXing también soporta el código de barras PDF417, que es distinto del formato de documento PDF.

## Conclusión

PDFBox 3.0.8 y ZXing 3.5.4 son opciones compatibles con Java 21 para la POC solicitada. La integración mínima consiste en generar PNG con ZXing e incrustarlo con PDFBox. Mantener la POC fuera de la aplicación permite validar generación, lectura del QR y fuentes antes de decidir cómo integrar dependencias y endpoints en el proyecto principal.

## Fuentes oficiales

- [Apache PDFBox: descarga y versiones](https://pdfbox.apache.org/download.html)
- [Apache PDFBox: guía de inicio y dependencia Maven](https://pdfbox.apache.org/3.0/getting-started.html)
- [Apache PDFBox: características y licencia](https://pdfbox.apache.org/)
- [ZXing: versiones publicadas](https://github.com/zxing/zxing/releases)
- [ZXing: guía de desarrollo y módulos Maven](https://github.com/zxing/zxing/wiki/Getting-Started-Developing)
- [ZXing: licencia, formatos y estado de mantenimiento](https://github.com/zxing/zxing)
- [ZXing `QRCodeWriter`: API de codificación](https://github.com/zxing/zxing/blob/master/core/src/main/java/com/google/zxing/qrcode/QRCodeWriter.java)
