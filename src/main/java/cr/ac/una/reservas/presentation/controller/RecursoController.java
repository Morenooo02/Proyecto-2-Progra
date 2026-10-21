package cr.ac.una.reservas.presentation.controller;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import cr.ac.una.reservas.logic.Categoria;
import cr.ac.una.reservas.logic.Recurso;
import cr.ac.una.reservas.logic.ServiceModel;
import cr.ac.una.reservas.presentation.view.VistaUtil;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"unused", "WeakerAccess", "FieldCanBeLocal"})
public class RecursoController extends JPanel {
    public JPanel rootPanel;
    public JPanel panelNorte;
    public JPanel panelFiltro;
    public JPanel panelForm;
    public JPanel panelBotones;
    public JComboBox<Categoria> cmbFiltro;
    public JComboBox<Categoria> cmbCategoria;
    public JTextField txtId;
    public JTextField txtDescripcion;
    public JTable tabla;
    private DefaultTableModel modelo;
    public JButton btnFiltrar;
    public JButton btnGuardar;
    public JButton btnEliminar;
    public JButton btnLimpiar;
    public JButton btnPdf;
    private JTextField txtDescripcion2;
    private ServiceModel service;
    private final PropertyChangeListener modeloListener = this::onModeloCambio;


    public RecursoController() {
        $$$setupUI$$$();
        configurarIconos();
        service = ServiceModel.getInstance();
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        cargarCategorias();
        btnFiltrar.addActionListener(e -> buscarRecursos());
        btnGuardar.addActionListener(e -> guardarFormulario());
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        btnLimpiar.addActionListener(e -> limpiar());
        btnPdf.addActionListener(e -> generarPdf());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        service.addPropertyChangeListener(modeloListener);
        actualizarTabla(service.buscarRecursosPorCategoria(""));
    }

    private void onModeloCambio(PropertyChangeEvent evt) {
        if ("recursos".equals(evt.getPropertyName())) {
            buscarRecursos();
        } else if ("categorias".equals(evt.getPropertyName())) {
            cargarCategorias();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        service.removePropertyChangeListener(modeloListener);
    }

    private String categoriaFiltro() {
        Categoria c = (Categoria) cmbFiltro.getSelectedItem();
        return c == null ? "" : c.getId();
    }
    private void buscarRecursos() {
        String categoria = categoriaFiltro();
        String descripcion = txtDescripcion2.getText().trim().toLowerCase();

        List<Recurso> recursos = service.buscarRecursosPorCategoria(categoria);
        List<Recurso> filtrados = new ArrayList<>();

        for (Recurso r : recursos) {
            if (descripcion.isBlank() || (r.getDescripcion() != null && r.getDescripcion().toLowerCase().contains(descripcion))) {
                filtrados.add(r);
            }
        }

        actualizarTabla(filtrados);
    }

    private void cargarCategorias() {
        cmbFiltro.removeAllItems();
        cmbCategoria.removeAllItems();
        cmbFiltro.addItem(new Categoria("", "Todas"));
        for (Categoria c : service.buscarCategorias("")) {
            cmbFiltro.addItem(c);
            cmbCategoria.addItem(c);
        }
    }

    private void guardarFormulario() {
        if (txtId.getText().isBlank() || txtDescripcion.getText().isBlank() || cmbCategoria.getSelectedItem() == null) {
            VistaUtil.error(this, "Complete id, descripcion y categoria.");
            return;
        }
        Recurso r = new Recurso(txtId.getText().trim(), txtDescripcion.getText().trim(),
                (Categoria) cmbCategoria.getSelectedItem());
        Recurso existente = null;
        for (Recurso rec : service.buscarRecursosPorCategoria("")) {
            if (rec.getId().equals(r.getId())) {
                existente = rec;
                break;
            }
        }
        boolean ok = existente == null ? service.agregarRecurso(r) : service.modificarRecurso(r);
        if (ok) {
            cargarCategorias();
            actualizarTabla(service.buscarRecursosPorCategoria(""));
            limpiar();
            VistaUtil.mensaje(this, "Recurso guardado.");
        } else {
            VistaUtil.error(this, "No se pudo guardar. El id puede estar repetido.");
        }
    }

    private void configurarIconos() {
        btnFiltrar.setIcon(cargarIcono("/images/buscar.png", 24, 24));
        btnPdf.setIcon(cargarIcono("/images/pdf.png", 24, 24));
        btnGuardar.setIcon(cargarIcono("/images/guardar.png", 24, 24));
        btnEliminar.setIcon(cargarIcono("/images/cancelar.png", 24, 24));
        btnLimpiar.setIcon(cargarIcono("/images/limpiar.png", 24, 24));
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

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            VistaUtil.error(this, "Seleccione un recurso.");
            return;
        }
        String id = String.valueOf(modelo.getValueAt(fila, 0));
        if (service.eliminarRecurso(id)) {
            actualizarTabla(service.buscarRecursosPorCategoria(""));
            limpiar();
            VistaUtil.mensaje(this, "Recurso eliminado.");
        } else {
            VistaUtil.error(this, "No se puede eliminar porque esta en una reserva activa.");
        }
    }

    private void generarPdf() {
        String elegido = VistaUtil.elegirRutaPdf(this, "recursos.pdf");
        if (elegido == null) {
            return;
        }
        service.exportarPDF("Recursos", VistaUtil.columnas(modelo), VistaUtil.tablaADatos(modelo), elegido);
        VistaUtil.mensaje(this, "PDF generado.");
    }

    private void actualizarTabla(List<Recurso> lista) {
        modelo.setRowCount(0);
        for (Recurso r : lista) {
            String cat = r.getCategoria() == null ? "" : r.getCategoria().getDescripcion();
            modelo.addRow(new Object[]{r.getId(), r.getDescripcion(), cat});
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        String id = String.valueOf(modelo.getValueAt(fila, 0));
        Recurso rec = null;
        for (Recurso r : service.buscarRecursosPorCategoria("")) {
            if (r.getId().equals(id)) {
                rec = r;
                break;
            }
        }
        if (rec == null) {
            return;
        }
        txtId.setText(rec.getId());
        txtDescripcion.setText(rec.getDescripcion());
        txtId.setEnabled(false);
        String catId = rec.getCategoria() == null ? "" : rec.getCategoria().getId();
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            Categoria item = (Categoria) cmbCategoria.getItemAt(i);
            if (item != null && item.getId().equals(catId)) {
                cmbCategoria.setSelectedIndex(i);
                break;
            }
        }
    }

    private void limpiar() {
        txtId.setText("");
        txtDescripcion.setText("");
        txtDescripcion2.setText("");
        txtId.setEnabled(true);
        tabla.clearSelection();
    }

    private void createUIComponents() {
        modelo = new DefaultTableModel(new Object[]{"Id", "Descripcion", "Categoria"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modelo);
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
        rootPanel.setLayout(new GridLayoutManager(3, 1, new Insets(8, 8, 8, 8), -1, -1));
        panelNorte = new JPanel();
        panelNorte.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panelNorte, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelFiltro = new JPanel();
        panelFiltro.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        panelNorte.add(panelFiltro, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelFiltro.setBorder(BorderFactory.createTitledBorder(null, "Filtro", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Categoria:");
        panelFiltro.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cmbFiltro = new JComboBox();
        panelFiltro.add(cmbFiltro, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnFiltrar = new JButton();
        btnFiltrar.setText("Buscar");
        panelFiltro.add(btnFiltrar, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnPdf = new JButton();
        btnPdf.setText("Imprimir");
        panelFiltro.add(btnPdf, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Descrpicion");
        panelFiltro.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtDescripcion2 = new JTextField();
        panelFiltro.add(txtDescripcion2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        panelForm = new JPanel();
        panelForm.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        panelNorte.add(panelForm, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelForm.setBorder(BorderFactory.createTitledBorder(null, "Recurso", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label3 = new JLabel();
        label3.setText("Id/Activo:");
        panelForm.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtId = new JTextField();
        panelForm.add(txtId, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Categoria:");
        panelForm.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cmbCategoria = new JComboBox();
        panelForm.add(cmbCategoria, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Descripcion:");
        panelForm.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtDescripcion = new JTextField();
        panelForm.add(txtDescripcion, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLimpiar = new JButton();
        btnLimpiar.setText("Limpiar");
        panelForm.add(btnLimpiar, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnGuardar = new JButton();
        btnGuardar.setText("Guardar");
        panelForm.add(btnGuardar, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEliminar = new JButton();
        btnEliminar.setText("Borrar");
        panelForm.add(btnEliminar, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        rootPanel.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(null, "Listado", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scrollPane1.setViewportView(tabla);
        panelBotones = new JPanel();
        panelBotones.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panelBotones, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
