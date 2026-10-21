package cr.ac.una.reservas.backend.dao;

import cr.ac.una.reservas.backend.data.Database;
import cr.ac.una.reservas.backend.model.Funcionario;
import cr.ac.una.reservas.backend.security.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FuncionarioDAO {


    public void create(Funcionario funcionario, String claveInicial)
            throws SQLException {

        validarFuncionario(funcionario);

        if (claveInicial == null || claveInicial.isBlank()) {
            throw new IllegalArgumentException(
                    "La clave inicial es obligatoria."
            );
        }

        String sqlUsuario = """
                INSERT INTO usuarios (id, clave_hash, rol)
                VALUES (?, ?, 'FUNCIONARIO')
                """;

        String sqlFuncionario = """
                INSERT INTO funcionarios (id, nombre, telefono)
                VALUES (?, ?, ?)
                """;

        String hash = PasswordUtil.hash(claveInicial);

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // Primero se crea la cuenta de usuario.
                try (PreparedStatement statement =
                             connection.prepareStatement(sqlUsuario)) {

                    statement.setString(1, funcionario.getId());
                    statement.setString(2, hash);
                    statement.executeUpdate();
                }

                // Después se crea el registro del funcionario.
                try (PreparedStatement statement =
                             connection.prepareStatement(sqlFuncionario)) {

                    statement.setString(1, funcionario.getId());
                    statement.setString(2, funcionario.getNombre());
                    statement.setString(3, funcionario.getTelefono());
                    statement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }


    public Optional<Funcionario> findById(String id)
            throws SQLException {

        validarId(id);

        String sql = """
                SELECT f.id, f.nombre, f.telefono
                FROM funcionarios f
                INNER JOIN usuarios u ON u.id = f.id
                WHERE f.id = ?
                  AND u.rol = 'FUNCIONARIO'
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapearFuncionario(result));
                }
            }
        }

        return Optional.empty();
    }


    public List<Funcionario> findAll() throws SQLException {
        String sql = """
                SELECT f.id, f.nombre, f.telefono
                FROM funcionarios f
                INNER JOIN usuarios u ON u.id = f.id
                WHERE u.rol = 'FUNCIONARIO'
                ORDER BY f.id
                """;

        List<Funcionario> funcionarios = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                funcionarios.add(mapearFuncionario(result));
            }
        }

        return funcionarios;
    }


    public boolean update(Funcionario funcionario)
            throws SQLException {

        validarFuncionario(funcionario);

        String sql = """
                UPDATE funcionarios f
                INNER JOIN usuarios u ON u.id = f.id
                SET f.nombre = ?, f.telefono = ?
                WHERE f.id = ?
                  AND u.rol = 'FUNCIONARIO'
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, funcionario.getNombre());
            statement.setString(2, funcionario.getTelefono());
            statement.setString(3, funcionario.getId());

            return statement.executeUpdate() == 1;
        }
    }


    public boolean delete(String id) throws SQLException {
        validarId(id);

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                boolean existe;

                String comprobarSql = """
                        SELECT 1
                        FROM funcionarios f
                        INNER JOIN usuarios u ON u.id = f.id
                        WHERE f.id = ?
                          AND u.rol = 'FUNCIONARIO'
                        FOR UPDATE
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(comprobarSql)) {

                    statement.setString(1, id);

                    try (ResultSet result = statement.executeQuery()) {
                        existe = result.next();
                    }
                }

                if (!existe) {
                    connection.rollback();
                    return false;
                }


                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     "DELETE FROM funcionarios WHERE id = ?")) {

                    statement.setString(1, id);
                    statement.executeUpdate();
                }


                try (PreparedStatement statement =
                             connection.prepareStatement("""
                                     DELETE FROM usuarios
                                     WHERE id = ? AND rol = 'FUNCIONARIO'
                                     """)) {

                    statement.setString(1, id);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo eliminar la cuenta del funcionario."
                        );
                    }
                }

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }


    public boolean existsById(String id) throws SQLException {
        validarId(id);

        String sql = """
                SELECT 1
                FROM funcionarios
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private Funcionario mapearFuncionario(ResultSet result)
            throws SQLException {

        return new Funcionario(
                result.getString("id"),
                result.getString("nombre"),
                result.getString("telefono")
        );
    }

    private void validarFuncionario(Funcionario funcionario) {
        if (funcionario == null) {
            throw new IllegalArgumentException(
                    "El funcionario no puede ser nulo."
            );
        }

        validarId(funcionario.getId());

        String nombre = funcionario.getNombre();

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        if (nombre.length() > 120) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar 120 caracteres."
            );
        }

        String telefono = funcionario.getTelefono();

        if (telefono != null && telefono.length() > 30) {
            throw new IllegalArgumentException(
                    "El teléfono no puede superar 30 caracteres."
            );
        }
    }

    private void validarId(String id) {
        if (id == null || id.isBlank() || id.length() > 50) {
            throw new IllegalArgumentException(
                    "El identificador es obligatorio y admite "
                            + "hasta 50 caracteres."
            );
        }
    }
}