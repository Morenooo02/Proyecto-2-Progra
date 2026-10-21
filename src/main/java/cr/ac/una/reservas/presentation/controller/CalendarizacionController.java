package cr.ac.una.reservas.presentation.controller;

import com.github.lgooddatepicker.components.DatePicker;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import cr.ac.una.reservas.logic.Categoria;
import cr.ac.una.reservas.logic.Recurso;
import cr.ac.una.reservas.logic.ServiceModel;
import cr.ac.una.reservas.presentation.view.VistaUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings({"unused", "WeakerAccess", "FieldCanBeLocal"})
public class CalendarizacionController extends JPanel {
    public JPanel rootPanel;
    public JPanel panelNorte;
    public JComboBox<Categoria> cmbCategoria;
    public JTable tabla;
    private DefaultTableModel modelo;
    public JButton btnCargar;
    public JButton btnPdf;
    private DatePicker dateFecha;
    private ServiceModel service;

    public CalendarizacionController() {
        $$$setupUI$$$();
        configurarIconos();
        service = ServiceModel.getInstance();
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        cargarCategorias();
        btnCargar.addActionListener(e -> cargar());
        btnPdf.addActionListener(e -> generarPdf());
    }

    private void cargarCategorias() {
        cmbCategoria.removeAllItems();
        for (Categoria c : service.buscarCategorias("")) {
            cmbCategoria.addItem(c);
        }
    }

    private String categoriaSeleccionada() {
        Categoria c = (Categoria) cmbCategoria.getSelectedItem();
        return c == null ? "" : c.getId();
    }

    private void configurarIconos() {
        btnCargar.setIcon(cargarIcono("/images/check.png", 24, 24));
        btnPdf.setIcon(cargarIcono("/images/pdf.png", 24, 24));
    }

    private ImageIcon cargarIcono(String ruta, int ancho, int alto) {
        URL url = getClass().getResource(ruta);

        if (url == null) {
            System.err.println("NO SE ENCONTRO: " + ruta);
            return null;
        }

        ImageIcon original = new ImageIcon(url);
        Image imagen = original.getImage().getScaledInstance(
                ancho, alto, Image.SCALE_SMOOTH
        );

        return new ImageIcon(imagen);
    }

    private void cargar() {
        try {
            LocalDate fecha = dateFecha.getDate();

            if (fecha == null) {
                VistaUtil.error(this, "Debe seleccionar una fecha.");
                return;
            }

            Object[][] matriz = service.obtenerMatrizCalendarizacion(
                    fecha, categoriaSeleccionada());

            List<Recurso> recursos = service.recursosDeCategoria(categoriaSeleccionada());
            String[] cols = new String[recursos.size() + 1];
            cols[0] = "Hora";

            for (int i = 0; i < recursos.size(); i++) {
                cols[i + 1] = recursos.get(i).getDescripcion();
            }

            modelo.setColumnIdentifiers(cols);
            modelo.setRowCount(0);

            if (matriz != null) {
                for (Object[] fila : matriz) {
                    modelo.addRow(fila);
                }
            }

        } catch (Exception ex) {
            VistaUtil.error(this, "No se pudo cargar la calendarización.");
        }
    }

    private void generarPdf() {
        String elegido = VistaUtil.elegirRutaPdf(this, "calendarizacion.pdf");
        if (elegido == null) {
            return;
        }
        service.exportarPDF("Calendarizacion de recursos", VistaUtil.columnas(modelo),
                VistaUtil.tablaADatos(modelo), elegido);
        VistaUtil.mensaje(this, "PDF generado.");
    }

    private void createUIComponents() {
        modelo = new DefaultTableModel();
        tabla = new JTable(modelo);

        tabla.setRowHeight(30);
        tabla.setFont(new Font("Arial", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 28));
        tabla.setGridColor(Color.GRAY);
        tabla.setShowGrid(true);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (!isSelected && column > 0 && value != null && !value.toString().isBlank()) {
                    c.setBackground(new Color(255, 255, 200));
                } else {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        });
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
        rootPanel.setLayout(new GridLayoutManager(2, 1, new Insets(8, 8, 8, 8), -1, -1));
        panelNorte = new JPanel();
        panelNorte.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panelNorte, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelNorte.setBorder(BorderFactory.createTitledBorder(null, "Filtros", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Fecha");
        panelNorte.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Categoria");
        panelNorte.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cmbCategoria = new JComboBox();
        panelNorte.add(cmbCategoria, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCargar = new JButton();
        btnCargar.setText("Cargar");
        panelNorte.add(btnCargar, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnPdf = new JButton();
        btnPdf.setText("Imprimir");
        panelNorte.add(btnPdf, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dateFecha = new DatePicker();
        panelNorte.add(dateFecha, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        rootPanel.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(null, "Calendarizacion de recursos", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scrollPane1.setViewportView(tabla);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
