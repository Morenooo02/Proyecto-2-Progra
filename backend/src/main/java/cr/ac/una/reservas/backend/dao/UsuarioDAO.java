package cr.ac.una.reservas.backend.dao;

import cr.ac.una.reservas.backend.data.Database;
import cr.ac.una.reservas.backend.model.Usuario;
import cr.ac.una.reservas.backend.security.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO {


    public void create(String id, String password, String rol)
            throws SQLException {

        validarId(id);
        validarRol(rol);
        validarPassword(password);

        String sql = """
                INSERT INTO usuarios (id, clave_hash, rol)
                VALUES (?, ?, ?)
                """;

        String hash = PasswordUtil.hash(password);

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);
            statement.setString(2, hash);
            statement.setString(3, rol);

            statement.executeUpdate();
        }
    }


    public Optional<Usuario> findById(String id)
            throws SQLException {

        validarId(id);

        String sql = """
                SELECT id, rol
                FROM usuarios
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapearUsuario(result));
                }
            }
        }

        return Optional.empty();
    }


    public List<Usuario> findAll() throws SQLException {
        String sql = """
                SELECT id, rol
                FROM usuarios
                ORDER BY id
                """;

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                usuarios.add(mapearUsuario(result));
            }
        }

        return usuarios;
    }


    public Optional<Usuario> authenticate(
            String id,
            String password
    ) throws SQLException {

        if (id == null || id.isBlank()
                || password == null || password.isEmpty()) {
            return Optional.empty();
        }

        validarId(id);

        String sql = """
                SELECT id, clave_hash, rol
                FROM usuarios
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                String hashGuardado = result.getString("clave_hash");

                if (!PasswordUtil.verify(password, hashGuardado)) {
                    return Optional.empty();
                }

                return Optional.of(mapearUsuario(result));
            }
        }
    }


    public boolean changePassword(
            String id,
            String currentPassword,
            String newPassword
    ) throws SQLException {

        validarId(id);
        validarPassword(currentPassword);
        validarPassword(newPassword);

        String selectSql = """
                SELECT clave_hash
                FROM usuarios
                WHERE id = ?
                FOR UPDATE
                """;

        String updateSql = """
                UPDATE usuarios
                SET clave_hash = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String hashGuardado;

                try (PreparedStatement statement =
                             connection.prepareStatement(selectSql)) {

                    statement.setString(1, id);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }

                        hashGuardado = result.getString("clave_hash");
                    }
                }

                if (!PasswordUtil.verify(
                        currentPassword, hashGuardado)) {
                    connection.rollback();
                    return false;
                }

                String nuevoHash = PasswordUtil.hash(newPassword);

                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {

                    statement.setString(1, nuevoHash);
                    statement.setString(2, id);

                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
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
                FROM usuarios
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

    private Usuario mapearUsuario(ResultSet result)
            throws SQLException {

        return new Usuario(
                result.getString("id"),
                result.getString("rol")
        );
    }

    private void validarId(String id) {
        if (id == null || id.isBlank() || id.length() > 50) {
            throw new IllegalArgumentException(
                    "El identificador es obligatorio y admite "
                            + "hasta 50 caracteres."
            );
        }
    }

    private void validarRol(String rol) {
        if (!"ADMINISTRADOR".equals(rol)
                && !"FUNCIONARIO".equals(rol)) {
            throw new IllegalArgumentException(
                    "El rol debe ser ADMINISTRADOR o FUNCIONARIO."
            );
        }
    }

    private void validarPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }
    }
}