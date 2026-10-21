package cr.ac.una.reservas.presentation.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;

/** Simple, dependency-free calendar popup for selecting a LocalDate. */
public class CalendarioPopup extends JPopupMenu {
    private final Consumer<LocalDate> alSeleccionar;
    private final JLabel lblMes = new JLabel();
    private final JPanel panelDias = new JPanel(new GridLayout(7, 7, 2, 2));
    private YearMonth mesMostrado;

    public CalendarioPopup(LocalDate fechaInicial, Consumer<LocalDate> alSeleccionar) {
        this.alSeleccionar = alSeleccionar;
        this.mesMostrado = fechaInicial != null ? YearMonth.from(fechaInicial) : YearMonth.now();
        construir();
        actualizar();
    }

    private void construir() {
        setBorder(BorderFactory.createLineBorder(new java.awt.Color(210, 210, 210)));
        setLayout(new BorderLayout(8, 8));

        JPanel encabezado = new JPanel(new BorderLayout());
        JButton btnAnterior = new JButton("‹");
        JButton btnSiguiente = new JButton("›");
        btnAnterior.setMargin(new Insets(2, 8, 2, 8));
        btnSiguiente.setMargin(new Insets(2, 8, 2, 8));
        btnAnterior.addActionListener(e -> {
            mesMostrado = mesMostrado.minusMonths(1);
            actualizar();
        });
        btnSiguiente.addActionListener(e -> {
            mesMostrado = mesMostrado.plusMonths(1);
            actualizar();
        });
        lblMes.setHorizontalAlignment(JLabel.CENTER);
        encabezado.add(btnAnterior, BorderLayout.WEST);
        encabezado.add(lblMes, BorderLayout.CENTER);
        encabezado.add(btnSiguiente, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        add(panelDias, BorderLayout.CENTER);
    }

    private void actualizar() {
        lblMes.setText(nombreMes(mesMostrado));
        panelDias.removeAll();

        for (DayOfWeek dia : DayOfWeek.values()) {
            JLabel label = new JLabel(nombreCorto(dia), JLabel.CENTER);
            panelDias.add(label);
        }

        LocalDate primero = mesMostrado.atDay(1);
        int desplazamiento = primero.getDayOfWeek().getValue() - 1;
        for (int i = 0; i < desplazamiento; i++) {
            panelDias.add(new JLabel());
        }

        LocalDate hoy = LocalDate.now();
        for (int dia = 1; dia <= mesMostrado.lengthOfMonth(); dia++) {
            LocalDate fecha = mesMostrado.atDay(dia);
            JButton boton = new JButton(String.valueOf(dia));
            boton.setMargin(new Insets(3, 3, 3, 3));
            boton.setFocusPainted(false);
            if (fecha.equals(hoy)) {
                boton.setBorder(BorderFactory.createLineBorder(new java.awt.Color(70, 130, 180), 1));
            }
            boton.addActionListener((ActionEvent e) -> {
                alSeleccionar.accept(fecha);
                setVisible(false);
            });
            panelDias.add(boton);
        }

        panelDias.revalidate();
        panelDias.repaint();
        pack();
    }

    private String nombreMes(YearMonth mes) {
        String nombre = mes.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"));
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1) + " " + mes.getYear();
    }

    private String nombreCorto(DayOfWeek dia) {
        String nombre = dia.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es"));
        return nombre.substring(0, 1).toUpperCase(Locale.ROOT);
    }
}
