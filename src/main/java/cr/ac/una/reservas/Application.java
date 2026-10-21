package cr.ac.una.reservas;

import cr.ac.una.reservas.presentation.controller.LoginController;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatLightLaf;


public class Application {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> error.printStackTrace(System.err));
        System.setProperty("flatlaf.useWindowDecorations", "false");

        SwingUtilities.invokeLater(() -> {
            try {
                FlatLightLaf.setup();
                UIManager.put("Button.arc", 12);
                UIManager.put("Component.arc", 12);
                UIManager.put("TextComponent.arc", 12);
            } catch (RuntimeException exception) {
                exception.printStackTrace(System.err);
            }
            new LoginController().setVisible(true);
        });
    }
}
