package cr.ac.una.reservas.presentation.controller;

import com.github.lgooddatepicker.components.DatePicker;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import cr.ac.una.reservas.logic.ServiceModel;
import cr.ac.una.reservas.presentation.view.VistaUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess", "FieldCanBeLocal"})
public class EstadisticasController extends JPanel {
    public JPanel rootPanel;
    public JPanel panelRecursos;
    public JPanel panelActividades;
    public JPanel panelFiltroRecursos;
    public JPanel panelFiltroActividades;
    public JPanel panelCentroRecursos;
    public JPanel panelCentroActividades;
    public JPanel panelPdf;
    public DatePicker dateDesdeRecursos;
    public DatePicker dateHastaRecursos;
    public DatePicker dateDesdeActividades;
    public DatePicker dateHastaActividades;
    private DefaultTableModel modeloRecursos;
    private DefaultTableModel modeloActividades;
    public JTable tablaRecursos;
    public JTable tablaActividades;
    public JPanel panelGraficoRecursos;
    public JPanel panelGraficoActividades;
    public JButton btnRecursos;
    public JButton btnActividades;
    public JButton btnPdf;
    private ServiceModel service;
    private byte[] pngRecursos;
    private byte[] pngActividades;

    public EstadisticasController() {
        $$$setupUI$$$();
        service = ServiceModel.getInstance();
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        btnRecursos.addActionListener(e -> cargarRecursos());
        btnActividades.addActionListener(e -> cargarActividades());
        btnPdf.addActionListener(e -> generarPdf());
    }

    private void cargarRecursos() {
        LocalDate desde = dateDesdeRecursos.getDate();
        LocalDate hasta = dateHastaRecursos.getDate();
        if (desde == null || hasta == null) {
            VistaUtil.error(this, "Debe seleccionar ambas fechas.");
            return;
        }
        if (desde.isAfter(hasta)) {
            VistaUtil.error(this, "El rango de recursos es invalido.");
            return;
        }
        llenarTablaYGrafico(modeloRecursos, panelGraficoRecursos,
                service.obtenerEstadisticasRecursos(desde, hasta), "Recursos");
    }

    private void cargarActividades() {
        LocalDate desde = dateDesdeActividades.getDate();
        LocalDate hasta = dateHastaActividades.getDate();
        if (desde == null || hasta == null) {
            VistaUtil.error(this, "Debe seleccionar ambas fechas.");
            return;
        }
        if (desde.isAfter(hasta)) {
            VistaUtil.error(this, "El rango de actividades es invalido.");
            return;
        }
        llenarTablaYGrafico(modeloActividades, panelGraficoActividades,
                service.obtenerEstadisticasActividades(desde, hasta), "Actividades");
    }

    private void generarPdf() {
        String elegido = VistaUtil.elegirRutaPdf(this, "estadisticas.pdf");
        if (elegido == null) {
            return;
        }
        List<Object[]> filas = new ArrayList<>();
        for (int i = 0; i < modeloRecursos.getRowCount(); i++) {
            filas.add(new Object[]{modeloRecursos.getValueAt(i, 0), modeloRecursos.getValueAt(i, 1)});
        }
        for (int i = 0; i < modeloActividades.getRowCount(); i++) {
            filas.add(new Object[]{modeloActividades.getValueAt(i, 0), modeloActividades.getValueAt(i, 1)});
        }
        service.exportarPDF("Estadisticas", new String[]{"Descripcion", "Cantidad"},
                filas.toArray(new Object[0][0]), elegido);
        VistaUtil.mensaje(this, "PDF generado.");
    }

    private void llenarTablaYGrafico(DefaultTableModel modelo, JPanel panel, Map<String, Integer> datos, String titulo) {
        modelo.setRowCount(0);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (datos != null) {
            for (Map.Entry<String, Integer> e : datos.entrySet()) {
                modelo.addRow(new Object[]{e.getKey(), e.getValue()});
                dataset.addValue(e.getValue(), titulo, e.getKey());
            }
        }
        JFreeChart chart = ChartFactory.createBarChart(titulo, "", "Cantidad", dataset);
        panel.removeAll();
        panel.add(new ChartPanel(chart), BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();

        byte[] png = chartAPng(chart);
        if ("Recursos".equals(titulo)) {
            pngRecursos = png;
        } else {
            pngActividades = png;
        }
    }

    private byte[] chartAPng(JFreeChart chart) {
        try {
            BufferedImage img = chart.createBufferedImage(500, 320);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return null; // si falla la captura, el PDF sigue generándose sin ese gráfico
        }
    }

    private void createUIComponents() {
        modeloRecursos = new DefaultTableModel(new Object[]{"Categoria", "Cantidad"}, 0);
        modeloActividades = new DefaultTableModel(new Object[]{"Semana", "Cantidad"}, 0);
        tablaRecursos = new JTable(modeloRecursos);
        tablaActividades = new JTable(modeloActividades);
    }


    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(1, 2, new Insets(8, 8, 8, 8), -1, -1, true, false));
        panelRecursos = new JPanel();
        panelRecursos.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panelRecursos, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panelRecursos.setBorder(BorderFactory.createTitledBorder(null, "Recursos", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        panelFiltroRecursos = new JPanel();
        panelFiltroRecursos.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panelFiltroRecursos.setToolTipText("");
        panelRecursos.add(panelFiltroRecursos, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelFiltroRecursos.setBorder(BorderFactory.createTitledBorder(null, "Fechas Desde y Hasta", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, -1, -1, panelFiltroRecursos.getFont()), null));
        btnRecursos = new JButton();
        btnRecursos.setText("Cargar");
        panelFiltroRecursos.add(btnRecursos, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dateHastaRecursos = new DatePicker();
        panelFiltroRecursos.add(dateHastaRecursos, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        dateDesdeRecursos = new DatePicker();
        panelFiltroRecursos.add(dateDesdeRecursos, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panelCentroRecursos = new JPanel();
        panelCentroRecursos.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelRecursos.add(panelCentroRecursos, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelCentroRecursos.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(null, "Estadisticas", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scrollPane1.setViewportView(tablaRecursos);
        panelGraficoRecursos = new JPanel();
        panelGraficoRecursos.setLayout(new BorderLayout(0, 0));
        panelCentroRecursos.add(panelGraficoRecursos, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panelGraficoRecursos.setBorder(BorderFactory.createTitledBorder(null, "Grafico", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        panelActividades = new JPanel();
        panelActividades.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panelActividades, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panelActividades.setBorder(BorderFactory.createTitledBorder(null, "Actividades", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        panelFiltroActividades = new JPanel();
        panelFiltroActividades.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panelActividades.add(panelFiltroActividades, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelFiltroActividades.setBorder(BorderFactory.createTitledBorder(null, "Fechas Desde y Hasta", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        btnActividades = new JButton();
        btnActividades.setText("Cargar");
        panelFiltroActividades.add(btnActividades, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dateDesdeActividades = new DatePicker();
        panelFiltroActividades.add(dateDesdeActividades, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        dateHastaActividades = new DatePicker();
        panelFiltroActividades.add(dateHastaActividades, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnPdf = new JButton();
        btnPdf.setText("PDF");
        panelFiltroActividades.add(btnPdf, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelCentroActividades = new JPanel();
        panelCentroActividades.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelActividades.add(panelCentroActividades, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panelCentroActividades.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane2.setBorder(BorderFactory.createTitledBorder(null, "Estadisticas", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scrollPane2.setViewportView(tablaActividades);
        panelGraficoActividades = new JPanel();
        panelGraficoActividades.setLayout(new BorderLayout(0, 0));
        panelCentroActividades.add(panelGraficoActividades, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panelGraficoActividades.setBorder(BorderFactory.createTitledBorder(null, "Grafico", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}