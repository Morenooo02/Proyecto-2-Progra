package cr.ac.una.reservas.presentation.view;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.io.File;

public class VistaUtil {
    public static void mensaje(Component parent, String texto) {
        JOptionPane.showMessageDialog(parent, texto);
    }

    public static void error(Component parent, String texto) {
        JOptionPane.showMessageDialog(parent, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static String elegirRutaPdf(Component parent, String nombreSugerido) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(nombreSugerido));
        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            String ruta = chooser.getSelectedFile().getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".pdf")) {
                ruta = ruta + ".pdf";
            }
            return ruta;
        }
        return null;
    }

    public static Object[][] tablaADatos(DefaultTableModel modelo) {
        Object[][] datos = new Object[modelo.getRowCount()][modelo.getColumnCount()];
        for (int i = 0; i < modelo.getRowCount(); i++) {
            for (int j = 0; j < modelo.getColumnCount(); j++) {
                datos[i][j] = modelo.getValueAt(i, j);
            }
        }
        return datos;
    }

    public static String[] columnas(DefaultTableModel modelo) {
        String[] cols = new String[modelo.getColumnCount()];
        for (int i = 0; i < modelo.getColumnCount(); i++) {
            cols[i] = modelo.getColumnName(i);
        }
        return cols;
    }
}
