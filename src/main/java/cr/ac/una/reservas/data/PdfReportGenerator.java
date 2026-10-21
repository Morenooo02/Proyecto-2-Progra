package cr.ac.una.reservas.data;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Image;
import java.util.List;

import java.io.FileOutputStream;

public class PdfReportGenerator {

    public void generarReportePDF(String titulo, String[] columnas, Object[][] datos, String filePath) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            document.add(new Paragraph(titulo));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(columnas.length);
            for (String col : columnas) {
                table.addCell(col);
            }
            if (datos != null) {
                for (Object[] fila : datos) {
                    for (int i = 0; i < columnas.length; i++) {
                        Object valor = i < fila.length ? fila[i] : "";
                        table.addCell(valor == null ? "" : valor.toString());
                    }
                }
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el PDF: " + e.getMessage(), e);
        }
    }

    public void generarReporteConSecciones(String tituloGeneral, List<Seccion> secciones, String filePath) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            document.add(new Paragraph(tituloGeneral));
            document.add(new Paragraph(" "));

            for (Seccion s : secciones) {
                document.add(new Paragraph(s.titulo()));
                PdfPTable table = new PdfPTable(s.columnas().length);
                for (String col : s.columnas()) {
                    table.addCell(col);
                }
                for (Object[] fila : s.datos()) {
                    for (int i = 0; i < s.columnas().length; i++) {
                        Object valor = i < fila.length ? fila[i] : "";
                        table.addCell(valor == null ? "" : valor.toString());
                    }
                }
                document.add(table);

                if (s.grafico() != null) {
                    Image img = Image.getInstance(s.grafico());
                    img.scaleToFit(450, 300);
                    document.add(img);
                }
                document.add(new Paragraph(" "));
            }
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el PDF: " + e.getMessage(), e);
        }
    }

    public record Seccion(String titulo, String[] columnas, Object[][] datos, byte[] grafico) {
    }
}