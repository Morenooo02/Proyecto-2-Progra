package cr.ac.una.reservas.backend.data;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionIT {

    @Test
    void debeConectarseAMySQL() throws Exception {
        try (Connection connection = Database.getConnection()) {

            assertNotNull(connection, "La conexión no debe ser nula");
            assertTrue(connection.isValid(3),
                    "La conexión debe estar activa");

            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(
                         "SELECT DATABASE(), CURRENT_USER()")) {

                assertTrue(result.next());

                assertEquals(
                        "reservas_recursos",
                        result.getString(1),
                        "Debe conectarse a la base de datos del proyecto"
                );

                assertTrue(
                        result.getString(2).startsWith("reservas_backend@"),
                        "Debe conectarse con el usuario del Backend"
                );
            }
        }
    }
}