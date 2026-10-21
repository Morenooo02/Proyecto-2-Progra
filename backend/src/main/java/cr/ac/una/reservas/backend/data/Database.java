package cr.ac.una.reservas.backend.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private static final String DEFAULT_URL =
            "jdbc:mysql://127.0.0.1:3306/reservas_recursos";

    private Database() {

    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv("RESERVAS_DB_URL");
        String user = System.getenv("RESERVAS_DB_USER");
        String password = System.getenv("RESERVAS_DB_PASSWORD");

        if (url == null || url.isBlank()) {
            url = DEFAULT_URL;
        }

        if (user == null || user.isBlank()) {
            throw new IllegalStateException(
                    "Falta configurar la variable RESERVAS_DB_USER."
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Falta configurar la variable RESERVAS_DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(url, user, password);
    }
}