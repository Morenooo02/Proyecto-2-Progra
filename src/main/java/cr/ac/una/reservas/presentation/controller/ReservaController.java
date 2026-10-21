package cr.ac.una.reservas.presentation.controller;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import cr.ac.una.reservas.logic.Categoria;
import cr.ac.una.reservas.logic.Recurso;
import cr.ac.una.reservas.logic.Reserva;
import cr.ac.una.reservas.logic.ServiceModel;
import cr.ac.una.reservas.logic.ai.ReservaExtraccion;
import cr.ac.una.reservas.presentation.view.CalendarioPopup;
import cr.ac.una.reservas.presentation.view.VistaUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.ImageIcon;

@SuppressWarnings({"unused", "WeakerAccess", "FieldCanBeLocal"})
public class ReservaController extends JPanel {
    public JPanel rootPanel;
    public JPanel panelNorte;
    public JPanel panelIA;
    public JPanel panelForm;
    public JPanel panelBotones;
    public JLabel lblFrase;
    public JTextField txtFrase;
    public JTextField txtActividad;
    public JTextField txtFecha;
    public JButton btnFecha;
    public JComboBox<String> cbHoraInicio;
    public JComboBox<String> cbHoraFin;
    public JPanel panelCategorias;
    public JTable tabla;
    public JButton btnUsarIA;
    public JButton btnAplicar;
    public JButton btnCancelar;
    public JButton btnLimpiar;
    public JButton btnPdf;

    private final List<JCheckBox> checksCategorias = new ArrayList<>();
    private DefaultTableModel modelo;
    private ServiceModel service;
    private String funcionarioId;
    private final PropertyChangeListener modeloListener = this::onModeloCambio;

    public ReservaController(String funcionarioId) {
        this.funcionarioId = funcionarioId;
        $$$setupUI$$$();

        configurarIconos();

        this.service = ServiceModel.getInstance();
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);

        cargarChecksCategorias();
        configurarCalendario();
        configurarCombosHoras();
        configurarAcciones();
        service.addPropertyChangeListener(modeloListener);
        refrescar();
    }

    private void configurarAcciones() {
        btnUsarIA.addActionListener(e -> procesarFraseIA());
        btnAplicar.addActionListener(e -> aplicar());
        btnCancelar.addActionListener(e -> cancelar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnPdf.addActionListener(e -> generarPdf());
    }

    private void configurarCalendario() {
        btnFecha.addActionListener(e -> {
            LocalDate fechaActual = null;
            try {
                if (!txtFecha.getText().isBlank()) {
                    fechaActual = LocalDate.parse(txtFecha.getText().trim());
                }
            } catch (Exception ignored) {
            }
            CalendarioPopup popup = new CalendarioPopup(fechaActual, fecha -> txtFecha.setText(fecha.toString()));
            popup.show(btnFecha, 0, btnFecha.getHeight());
        });
    }

    private void configurarCombosHoras() {
        cbHoraInicio.removeAllItems();
        cbHoraFin.removeAllItems();
        cbHoraInicio.addItem("Seleccione...");
        cbHoraFin.addItem("Seleccione...");

        for (int hora = 0; hora < 24; hora++) {
            for (int minutos = 0; minutos < 60; minutos += 15) {
                String valor = String.format("%02d:%02d", hora, minutos);
                cbHoraInicio.addItem(valor);
                cbHoraFin.addItem(valor);
            }
        }
        cbHoraInicio.setSelectedIndex(0);
        cbHoraFin.setSelectedIndex(0);
    }

    private void onModeloCambio(PropertyChangeEvent evt) {
        if ("reservas".equals(evt.getPropertyName()) || "categorias".equals(evt.getPropertyName())) {
            refrescar();
        }
    }

    @Override
    public void removeNotify() {
        service.removePropertyChangeListener(modeloListener);
        super.removeNotify();
    }

    private void procesarFraseIA() {
        String frase = txtFrase.getText().trim();

        if (frase.isBlank()) {
            VistaUtil.error(this, "Ingrese una frase para utilizar la IA.");
            return;
        }

        btnUsarIA.setEnabled(false);
        String textoOriginalBoton = btnUsarIA.getText();
        btnUsarIA.setText("Procesando...");
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

        SwingWorker<ReservaExtraccion, Void> worker = new SwingWorker<>() {
            @Override
            protected ReservaExtraccion doInBackground() {
                return service.extraerReserva(frase);
            }

            @Override
            protected void done() {
                btnUsarIA.setEnabled(true);
                btnUsarIA.setText(textoOriginalBoton);
                setCursor(java.awt.Cursor.getDefaultCursor());
                try {
                    aplicarExtraccion(get());
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    VistaUtil.error(ReservaController.this,
                            "No se pudo obtener respuesta del servicio de IA "
                                    + "(puede estar caído o sin conexión en este momento).\n"
                                    + "Puede completar los campos de la reserva manualmente.\n"
                                    + "Detalle: " + causa.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void aplicarExtraccion(ReservaExtraccion extraccion) {
        if (extraccion == null) {
            VistaUtil.error(this, "No se pudieron extraer los datos de la reserva. Complete los campos manualmente.");
            return;
        }

        if (extraccion.getActividad() != null) {
            txtActividad.setText(extraccion.getActividad());
        }

        if (extraccion.getFecha() != null) {
            txtFecha.setText(extraccion.getFecha());
        }

        seleccionarHora(cbHoraInicio, extraccion.getHoraInicio());
        seleccionarHora(cbHoraFin, extraccion.getHoraFinal());

        List<String> categoriasExtraidas = extraccion.getCategoriasRecurso();
        for (JCheckBox cb : checksCategorias) {
            String descripcion = cb.getText();
            boolean seleccionada = categoriasExtraidas != null
                    && categoriasExtraidas.stream().anyMatch(c -> c.equalsIgnoreCase(descripcion));
            cb.setSelected(seleccionada);
        }

        VistaUtil.mensaje(this, "Datos extraídos correctamente.");
    }
    private void seleccionarHora(JComboBox<String> combo, String hora) {
        if (hora == null || hora.isBlank()) {
            combo.setSelectedIndex(0);
            return;
        }
        String normalizada;
        try {
            LocalTime tiempo = LocalTime.parse(hora.trim().length() == 5 ? hora.trim() : normalizarHora(hora.trim()));
            normalizada = tiempo.withSecond(0).withNano(0).format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception ex) {
            combo.setSelectedIndex(0);
            return;
        }
        combo.setSelectedItem(normalizada);
        if (combo.getSelectedIndex() < 0) {
            combo.setSelectedIndex(0);
        }
    }

    private void aplicar() {
        try {
            String actividad = txtActividad.getText().trim();
            String fechaTexto = txtFecha.getText().trim();
            String horaInicioTexto = obtenerHora(cbHoraInicio);
            String horaFinTexto = obtenerHora(cbHoraFin);

            if (actividad.isBlank() || fechaTexto.isBlank() || horaInicioTexto == null || horaFinTexto == null) {
                VistaUtil.error(this, "Complete actividad, fecha y horas.");
                return;
            }

            List<String> cats = categoriasSeleccionadas();
            if (cats.isEmpty()) {
                VistaUtil.error(this, "Seleccione al menos una categoria.");
                return;
            }

            Reserva r = new Reserva();
            r.setActividad(actividad);
            r.setFecha(LocalDate.parse(fechaTexto));
            r.setHoraInicio(LocalTime.parse(horaInicioTexto));
            r.setHoraFin(LocalTime.parse(horaFinTexto));
            r.setFuncionario(service.buscarFuncionario(funcionarioId));

            LocalDate hoy = LocalDate.now();
            if (r.getFecha().isBefore(hoy)
                    || (r.getFecha().equals(hoy) && !r.getHoraInicio().isAfter(LocalTime.now()))) {
                VistaUtil.error(this, "No puede reservar en una fecha u hora que ya pasó.");
                return;
            }

            if (!service.registrarReserva(r, cats)) {
                List<String> noDisp = service.categoriasNoDisponibles(r.getFecha(), r.getHoraInicio(), r.getHoraFin(), cats);
                if (!noDisp.isEmpty()) {
                    VistaUtil.error(this, "No hay disponibilidad en: " + String.join(", ", noDisp));
                } else {
                    VistaUtil.error(this, "No se pudo registrar la reserva. Revise los datos.");
                }
                return;
            }

            VistaUtil.mensaje(this, "Reserva registrada con exito.");
            limpiarFormulario();
            refrescar();
        } catch (Exception ex) {
            VistaUtil.error(this, "Datos invalidos: " + ex.getMessage());
        }
    }

    private void cancelar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            VistaUtil.error(this, "Seleccione una reserva.");
            return;
        }
        String id = String.valueOf(modelo.getValueAt(fila, 0));
        if (service.cancelarReserva(id)) {
            VistaUtil.mensaje(this, "Reserva cancelada.");
            refrescar();
        } else {
            VistaUtil.error(this, "Solo se pueden cancelar reservas futuras activas.");
        }
    }

    private void limpiar() {
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        txtFrase.setText("");
        txtActividad.setText("");
        txtFecha.setText("");
        cbHoraInicio.setSelectedIndex(0);
        cbHoraFin.setSelectedIndex(0);
        for (JCheckBox cb : checksCategorias) {
            cb.setSelected(false);
        }
    }

    private void generarPdf() {
        String elegido = VistaUtil.elegirRutaPdf(this, "reservas.pdf");
        if (elegido == null) {
            return;
        }
        service.exportarPDF("Mis reservas", VistaUtil.columnas(modelo), VistaUtil.tablaADatos(modelo), elegido);
        VistaUtil.mensaje(this, "PDF generado.");
    }

    private void refrescar() {
        cargarChecksCategorias();
        modelo.setRowCount(0);
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        for (Reserva r : service.obtenerReservasPorFuncionario(funcionarioId)) {
            String horario = (r.getHoraInicio() == null ? "" : r.getHoraInicio().format(tf))
                    + (r.getHoraFin() == null ? "" : " - " + r.getHoraFin().format(tf));
            String recursos = r.getRecursosAsignados() == null ? "" : r.getRecursosAsignados().stream()
                    .map(Recurso::getDescripcion)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            modelo.addRow(new Object[]{
                    r.getId(), r.getActividad(), r.getFecha(), horario, recursos, r.getEstado()
            });
        }
    }

    private void cargarChecksCategorias() {
        panelCategorias.removeAll();
        checksCategorias.clear();
        for (Categoria c : service.buscarCategorias("")) {
            JCheckBox cb = new JCheckBox(c.getDescripcion());
            cb.putClientProperty("catId", c.getId());
            cb.setFocusPainted(false);
            cb.setOpaque(false);
            checksCategorias.add(cb);
            panelCategorias.add(cb);
        }
        panelCategorias.revalidate();
        panelCategorias.repaint();
    }

    private List<String> categoriasSeleccionadas() {
        List<String> ids = new ArrayList<>();
        for (JCheckBox cb : checksCategorias) {
            if (cb.isSelected()) {
                ids.add((String) cb.getClientProperty("catId"));
            }
        }
        return ids;
    }

    private String obtenerHora(JComboBox<String> combo) {
        Object valor = combo.getSelectedItem();
        if (valor == null || "Seleccione...".equals(valor.toString())) {
            return null;
        }
        return valor.toString();
    }

    private String normalizarHora(String hora) {
        String[] p = hora.split(":");
        return String.format("%02d:%02d", Integer.parseInt(p[0]), Integer.parseInt(p[1]));
    }

    private ImageIcon icono(String recurso, int ancho, int alto) {
        URL url = getClass().getResource(recurso);
        if (url == null) return null;
        Image imagen = new ImageIcon(url).getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(imagen);
    }

    private void estilizarBoton(JButton boton, ImageIcon icono) {
        if (icono != null) {
            boton.setIcon(icono);
            boton.setIconTextGap(7);
        }
        boton.setFocusPainted(false);
        boton.setMargin(new Insets(7, 12, 7, 12));
    }

    private ImageIcon cargarIcono(String ruta, int ancho, int alto) {
        URL url = getClass().getResource(ruta);

        if (url == null) {
            System.err.println("No se encontró la imagen: " + ruta);
            return null;
        }

        ImageIcon original = new ImageIcon(url);

        Image imagen = original.getImage().getScaledInstance(
                ancho,
                alto,
                Image.SCALE_SMOOTH
        );

        return new ImageIcon(imagen);
    }

    private void createUIComponents() {
        modelo = new DefaultTableModel(new Object[]{"Id", "Actividad", "Fecha", "Horario", "Recursos", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(27);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));
        tabla.setGridColor(new Color(220, 225, 230));
    }

    private void configurarIconos() {

        ImageIcon iconoIA = cargarIcono("/images/IA.png", 22, 22);
        ImageIcon iconoReservar = cargarIcono("/images/guardar.png", 22, 22);
        ImageIcon iconoCancelar = cargarIcono("/images/cancelar.png", 22, 22);
        ImageIcon iconoLimpiar = cargarIcono("/images/limpiar.png", 22, 22);
        ImageIcon iconoPdf = cargarIcono("/images/pdf.png", 22, 22);
        ImageIcon iconoCalendario = cargarIcono("/images/calendario.png", 20, 20);

        estilizarBoton(btnUsarIA, iconoIA);
        estilizarBoton(btnAplicar, iconoReservar);
        estilizarBoton(btnCancelar, iconoCancelar);
        estilizarBoton(btnLimpiar, iconoLimpiar);
        estilizarBoton(btnPdf, iconoPdf);
        estilizarBoton(btnFecha, iconoCalendario);
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
        rootPanel.setLayout(new GridLayoutManager(3, 2, new Insets(12, 14, 12, 14), -1, 10));
        panelNorte = new JPanel();
        panelNorte.setLayout(new GridLayoutManager(3, 1, new Insets(8, 8, 8, 8), 8, 8));
        rootPanel.add(panelNorte, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panelNorte.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        panelIA = new JPanel();
        panelIA.setLayout(new GridLayoutManager(1, 4, new Insets(7, 7, 7, 7), 8, 5));
        panelNorte.add(panelIA, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panelIA.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Nueva Reserva", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        txtFrase = new JTextField();
        txtFrase.setToolTipText("Describe la reserva en lenguaje natural");
        panelIA.add(txtFrase, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(420, 30), null, 0, false));
        btnUsarIA = new JButton();
        btnUsarIA.setText("Extraer");
        panelIA.add(btnUsarIA, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblFrase = new JLabel();
        lblFrase.setText("Frase");
        panelIA.add(lblFrase, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelForm = new JPanel();
        panelForm.setLayout(new GridLayoutManager(3, 6, new Insets(7, 7, 7, 7), 8, 6));
        panelNorte.add(panelForm, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panelForm.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Actividad");
        panelForm.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Fecha");
        panelForm.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtActividad = new JTextField();
        panelForm.add(txtActividad, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCancelar = new JButton();
        btnCancelar.setText("Cancelar reserva");
        panelForm.add(btnCancelar, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnFecha = new JButton();
        btnFecha.setText("");
        panelForm.add(btnFecha, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtFecha = new JTextField();
        panelForm.add(txtFecha, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbHoraFin = new JComboBox();
        panelForm.add(cbHoraFin, new GridConstraints(1, 4, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbHoraInicio = new JComboBox();
        panelForm.add(cbHoraInicio, new GridConstraints(0, 4, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLimpiar = new JButton();
        btnLimpiar.setText("Limpiar");
        panelForm.add(btnLimpiar, new GridConstraints(2, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnAplicar = new JButton();
        btnAplicar.setText("Reservar");
        panelForm.add(btnAplicar, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Hora inicio");
        panelForm.add(label3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Hora fin");
        panelForm.add(label4, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelNorte.add(scrollPane1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(-1, 72), null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Categorias", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        panelCategorias = new JPanel();
        panelCategorias.setOpaque(false);
        scrollPane1.setViewportView(panelCategorias);
        final JScrollPane scrollPane2 = new JScrollPane();
        rootPanel.add(scrollPane2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Mis Reservas", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, -1, -1, scrollPane2.getFont()), null));
        scrollPane2.setViewportView(tabla);
        panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        rootPanel.add(panelBotones, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnPdf = new JButton();
        btnPdf.setText("Imprimir");
        rootPanel.add(btnPdf, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
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
