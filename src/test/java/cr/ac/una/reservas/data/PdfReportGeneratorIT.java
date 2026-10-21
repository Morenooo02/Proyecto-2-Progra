package cr.ac.una.reservas.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfReportGeneratorIT {

    @TempDir
    Path tempDir;

    @Test
    void generaArchivoPdf() throws Exception {
        Path pdf = tempDir.resolve("reporte.pdf");
        PdfReportGenerator gen = new PdfReportGenerator();
        gen.generarReportePDF("Prueba", new String[]{"Col1", "Col2"},
                new Object[][]{{"A", "1"}, {"B", "2"}}, pdf.toString());
        assertTrue(Files.exists(pdf));
        assertTrue(Files.size(pdf) > 0);
    }
}
