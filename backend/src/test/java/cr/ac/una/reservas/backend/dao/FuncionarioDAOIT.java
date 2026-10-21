package cr.ac.una.reservas.backend.dao;

import cr.ac.una.reservas.backend.data.Database;
import cr.ac.una.reservas.backend.model.Funcionario;
import cr.ac.una.reservas.backend.model.Usuario;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioDAOIT {

    @Test
    void debeEjecutarCrudDeFuncionarioYCuentaAsociada()
            throws Exception {

        FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        String id = "TEST-FUNC-" + UUID.randomUUID();
        String claveInicial = "ClaveTemporal#A1";

        boolean creado = false;

        try {

            Funcionario inicial = new Funcionario(
                    id,
                    "Funcionario temporal",
                    "8888-1111"
            );

            funcionarioDAO.create(inicial, claveInicial);
            creado = true;


            assertTrue(funcionarioDAO.existsById(id));

            Optional<Funcionario> encontrado =
                    funcionarioDAO.findById(id);

            assertTrue(encontrado.isPresent());
            assertEquals(
                    "Funcionario temporal",
                    encontrado.get().getNombre()
            );
            assertEquals(
                    "8888-1111",
                    encontrado.get().getTelefono()
            );


            Optional<Usuario> usuario = usuarioDAO.findById(id);

            assertTrue(usuario.isPresent());
            assertEquals("FUNCIONARIO", usuario.get().getRol());


            assertTrue(
                    usuarioDAO.authenticate(id, claveInicial).isPresent()
            );


            assertTrue(
                    funcionarioDAO.findAll().stream()
                            .anyMatch(f -> id.equals(f.getId()))
            );


            Funcionario modificado = new Funcionario(
                    id,
                    "Funcionario modificado",
                    "8888-2222"
            );

            assertTrue(funcionarioDAO.update(modificado));

            Optional<Funcionario> actualizado =
                    funcionarioDAO.findById(id);

            assertTrue(actualizado.isPresent());
            assertEquals(
                    "Funcionario modificado",
                    actualizado.get().getNombre()
            );
            assertEquals(
                    "8888-2222",
                    actualizado.get().getTelefono()
            );


            assertTrue(funcionarioDAO.delete(id));

            assertFalse(funcionarioDAO.existsById(id));
            assertFalse(usuarioDAO.existsById(id));

        } finally {

            if (creado) {
                try (Connection connection = Database.getConnection()) {

                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         "DELETE FROM funcionarios WHERE id = ?")) {

                        statement.setString(1, id);
                        statement.executeUpdate();
                    }

                    try (PreparedStatement statement =
                                 connection.prepareStatement("""
                                         DELETE FROM usuarios
                                         WHERE id = ?
                                           AND rol = 'FUNCIONARIO'
                                         """)) {

                        statement.setString(1, id);
                        statement.executeUpdate();
                    }
                }
            }
        }
    }
}