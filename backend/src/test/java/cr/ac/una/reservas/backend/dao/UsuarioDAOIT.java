package cr.ac.una.reservas.backend.dao;

import cr.ac.una.reservas.backend.data.Database;
import cr.ac.una.reservas.backend.model.Usuario;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioDAOIT {

    @Test
    void debeRegistrarAutenticarYCambiarClave() throws Exception {
        UsuarioDAO dao = new UsuarioDAO();

        String id = "TEST-USR-" + UUID.randomUUID();
        String claveOriginal = "ClaveTemporal#A1";
        String claveNueva = "ClaveNueva#B2";

        boolean creado = false;

        try {

            dao.create(id, claveOriginal, "FUNCIONARIO");
            creado = true;


            assertTrue(dao.existsById(id));

            Optional<Usuario> encontrado = dao.findById(id);

            assertTrue(encontrado.isPresent());
            assertEquals(id, encontrado.get().getId());
            assertEquals("FUNCIONARIO", encontrado.get().getRol());


            assertTrue(
                    dao.findAll().stream()
                            .anyMatch(u -> id.equals(u.getId()))
            );


            assertTrue(dao.authenticate(id, claveOriginal).isPresent());


            assertTrue(dao.authenticate(id, "ClaveIncorrecta#9").isEmpty());


            assertFalse(
                    dao.changePassword(
                            id,
                            "ClaveIncorrecta#9",
                            claveNueva
                    )
            );


            assertTrue(dao.authenticate(id, claveOriginal).isPresent());


            assertTrue(
                    dao.changePassword(id, claveOriginal, claveNueva)
            );


            assertTrue(dao.authenticate(id, claveOriginal).isEmpty());


            assertTrue(dao.authenticate(id, claveNueva).isPresent());

        } finally {

            if (creado) {
                try (Connection connection = Database.getConnection();
                     PreparedStatement statement =
                             connection.prepareStatement(
                                     "DELETE FROM usuarios WHERE id = ?")) {

                    statement.setString(1, id);
                    statement.executeUpdate();
                }
            }
        }

        assertFalse(dao.existsById(id));
    }
}