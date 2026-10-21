package cr.ac.una.reservas.presentation.controller;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import cr.ac.una.reservas.logic.Funcionario;
import cr.ac.una.reservas.logic.ServiceModel;
import cr.ac.una.reservas.presentation.view.VistaUtil;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.util.List;
import java.util.Locale;


@SuppressWarnings({"unused", "WeakerAccess", "FieldCanBeLocal"})
public class FuncionarioController extends JPanel {
    public JPanel rootPanel;
    public JPanel panelNorte;
    public JPanel panelBusqueda;
    public JPanel panelForm;
    public JPanel panelBotones;
    public JTextField txtBusquedaId;
    public JTextField txtBusquedaNombre;
    public JTextField txtId;
    public JTextField txtNombre;
    public JTextField txtTelefono;
    public JTable tabla;
    private DefaultTableModel modelo;
    public JButton btnBuscar;
    public JButton btnGuardar;
    public JButton btnBorrar;
    public JButton btnLimpiar;
    public JButton btnPDF;
    private ServiceModel service;
    private final PropertyChangeListener modeloListener = this::onModeloCambio;


    public FuncionarioController() {
        $$$setupUI$$$();
        service = ServiceModel.getInstance();
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        btnBuscar.addActionListener(e -> buscar());
        btnGuardar.addActionListener(e -> guardarFormulario());
        btnBorrar.addActionListener(e -> eliminarSeleccionado());
        btnLimpiar.addActionListener(e -> limpiar());
        btnPDF.addActionListener(e -> generarPdf());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        service.addPropertyChangeListener(modeloListener);
        actualizarTabla(service.buscarFuncionarios("", ""));
    }

    private void onModeloCambio(PropertyChangeEvent evt) {
        if ("funcionarios".equals(evt.getPropertyName())) {
            actualizarTabla(service.buscarFuncionarios(txtBusquedaId.getText().trim(), txtBusquedaNombre.getText().trim()));
        }
    }
    @Override
    public void removeNotify() {
        super.removeNotify();
        service.removePropertyChangeListener(modeloListener);
    }

    private void buscar() {
        actualizarTabla(service.buscarFuncionarios(txtBusquedaId.getText().trim(), txtBusquedaNombre.getText().trim()));
    }

    private boolean guardar(Funcionario f) {
        Funcionario existente = service.buscarFuncionario(f.getId());
        boolean ok = existente == null ? service.agregarFuncionario(f) : service.modificarFuncionario(f);
        if (ok) {
            actualizarTabla(service.buscarFuncionarios("", ""));
            limpiar();
        }
        return ok;
    }

    private void guardarFormulario() {
        if (txtId.getText().isBlank() || txtNombre.getText().isBlank() || txtTelefono.getText().isBlank()) {
            VistaUtil.error(this, "Complete id, nombre y telefono.");
            return;
        }
        Funcionario f = new Funcionario(txtId.getText().trim(), txtNombre.getText().trim(), txtTelefono.getText().trim());
        if (guardar(f)) {
            VistaUtil.mensaje(this, "Funcionario guardado. La clave inicial es igual al id.");
        } else {
            VistaUtil.error(this, "No se pudo guardar. El id puede estar repetido o los datos son invalidos.");
        }
    }

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            VistaUtil.error(this, "Seleccione un funcionario.");
            return;
        }
        String id = String.valueOf(modelo.getValueAt(fila, 0));
        if (service.eliminarFuncionario(id)) {
            actualizarTabla(service.buscarFuncionarios("", ""));
            limpiar();
            VistaUtil.mensaje(this, "Funcionario eliminado.");
        } else {
            VistaUtil.error(this, "No se puede eliminar. Verifique que no tenga reservas futuras.");
        }
    }

    private void generarPdf() {
        String elegido = VistaUtil.elegirRutaPdf(this, "funcionarios.pdf");
        if (elegido == null) {
            return;
        }
        service.exportarPDF("Funcionarios", VistaUtil.columnas(modelo), VistaUtil.tablaADatos(modelo), elegido);
        VistaUtil.mensaje(this, "PDF generado.");
    }

    private void actualizarTabla(List<Funcionario> lista) {
        modelo.setRowCount(0);
        for (Funcionario f : lista) {
            modelo.addRow(new Object[]{f.getId(), f.getNombre(), f.getTelefono()});
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            txtId.setText(String.valueOf(modelo.getValueAt(fila, 0)));
            txtNombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
            txtTelefono.setText(String.valueOf(modelo.getValueAt(fila, 2)));
            txtId.setEnabled(false);
        }
    }

    private void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtId.setEnabled(true);
        tabla.clearSelection();
    }

    private void createUIComponents() {
        modelo = new DefaultTableModel(new Object[]{"Id", "Nombre", "Telefono"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        btnBuscar = boton("Buscar", "/images/buscar.png");
        btnPDF = boton("Imprimir", "/images/pdf.png");
        btnGuardar = boton("Guardar", "/images/guardar.png");
        btnBorrar = boton("Borrar", "/images/cancelar.png");
        btnLimpiar = boton("Limpiar", "/images/limpiar.png");
    }

    public JButton boton(String texto, String recurso) {
        ImageIcon original = new ImageIcon(getClass().getResource(recurso));
        Image scaled = original.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        return new JButton(texto, new ImageIcon(scaled));
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
        rootPanel.setLayout(new GridLayoutManager(4, 1, new Insets(8, 8, 8, 8), -1, -1));
        panelNorte = new JPanel();
        panelNorte.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), 8, 6));
        rootPanel.add(panelNorte, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panelForm = new JPanel();
        panelForm.setLayout(new GridLayoutManager(4, 4, new Insets(8, 10, 8, 10), 8, 6));
        panelNorte.add(panelForm, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelForm.setBorder(BorderFactory.createTitledBorder(null, "Funcionario", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, -1, -1, panelForm.getFont()), null));
        final JLabel label1 = new JLabel();
        label1.setText("ID");
        panelForm.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtId = new JTextField();
        panelForm.add(txtId, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Telefono");
        panelForm.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtTelefono = new JTextField();
        panelForm.add(txtTelefono, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Nombre");
        panelForm.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtNombre = new JTextField();
        panelForm.add(txtNombre, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelBotones = new JPanel();
        panelBotones.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panelForm.add(panelBotones, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnGuardar.setText("Guardar");
        panelBotones.add(btnGuardar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnBorrar.setText("Borrar");
        panelBotones.add(btnBorrar, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLimpiar.setText("Limpiar");
        panelBotones.add(btnLimpiar, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        rootPanel.add(scrollPane1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(450, 90), null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(null, "Listado", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scrollPane1.setViewportView(tabla);
        panelBusqueda = new JPanel();
        panelBusqueda.setLayout(new GridLayoutManager(1, 6, new Insets(8, 10, 8, 10), 8, 6));
        panelBusqueda.setToolTipText("");
        rootPanel.add(panelBusqueda, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelBusqueda.setBorder(BorderFactory.createTitledBorder(null, "Busqueda", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label4 = new JLabel();
        label4.setText("ID");
        panelBusqueda.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtBusquedaId = new JTextField();
        panelBusqueda.add(txtBusquedaId, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Nombre");
        panelBusqueda.add(label5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtBusquedaNombre = new JTextField();
        panelBusqueda.add(txtBusquedaNombre, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnBuscar.setText("Buscar");
        panelBusqueda.add(btnBuscar, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnPDF.setText("Imprimir");
        panelBusqueda.add(btnPDF, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        rootPanel.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(0, 0), null, null, 1, false));
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
