package cr.ac.una.reservas.backend.dao;

import cr.ac.una.reservas.backend.data.Database;
import cr.ac.una.reservas.backend.model.Categoria;
import cr.ac.una.reservas.backend.model.Recurso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecursoDAO {

    private static final String SELECT_CON_CATEGORIA = """
            SELECT r.id,
                   r.descripcion,
                   c.id AS categoria_id,
                   c.descripcion AS categoria_descripcion
            FROM recursos r
            INNER JOIN categorias c ON c.id = r.categoria_id
            """;

    // CREATE
    public void create(Recurso recurso) throws SQLException {
        validarRecurso(recurso);

        String sql = """
                INSERT INTO recursos (id, descripcion, categoria_id)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, recurso.getId());
            statement.setString(2, recurso.getDescripcion());
            statement.setString(3, recurso.getCategoria().getId());

            statement.executeUpdate();
        }
    }

    // READ: buscar por identificador
    public Optional<Recurso> findById(String id) throws SQLException {
        validarId(id);

        String sql = SELECT_CON_CATEGORIA + " WHERE r.id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapearRecurso(result));
                }
            }
        }

        return Optional.empty();
    }

    // READ: listar todos
    public List<Recurso> findAll() throws SQLException {
        String sql = SELECT_CON_CATEGORIA + " ORDER BY r.id";

        List<Recurso> recursos = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                recursos.add(mapearRecurso(result));
            }
        }

        return recursos;
    }

    // UPDATE
    public boolean update(Recurso recurso) throws SQLException {
        validarRecurso(recurso);

        String sql = """
                UPDATE recursos
                SET descripcion = ?, categoria_id = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, recurso.getDescripcion());
            statement.setString(2, recurso.getCategoria().getId());
            statement.setString(3, recurso.getId());

            return statement.executeUpdate() == 1;
        }
    }

    // DELETE
    public boolean delete(String id) throws SQLException {
        validarId(id);

        String sql = "DELETE FROM recursos WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            return statement.executeUpdate() == 1;
        }
    }

    // Convertir una fila SQL en un objeto Java
    private Recurso mapearRecurso(ResultSet result) throws SQLException {
        Categoria categoria = new Categoria(
                result.getString("categoria_id"),
                result.getString("categoria_descripcion")
        );

        return new Recurso(
                result.getString("id"),
                result.getString("descripcion"),
                categoria
        );
    }

    // Validaciones básicas
    private void validarRecurso(Recurso recurso) {
        if (recurso == null) {
            throw new IllegalArgumentException(
                    "El recurso no puede ser nulo."
            );
        }

        validarId(recurso.getId());

        String descripcion = recurso.getDescripcion();

        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException(
                    "La descripción es obligatoria."
            );
        }

        if (descripcion.length() > 150) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar 150 caracteres."
            );
        }

        if (recurso.getCategoria() == null) {
            throw new IllegalArgumentException(
                    "El recurso debe tener una categoría."
            );
        }

        validarId(recurso.getCategoria().getId());
    }

    private void validarId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "El identificador es obligatorio."
            );
        }

        if (id.length() > 50) {
            throw new IllegalArgumentException(
                    "El identificador no puede superar 50 caracteres."
            );
        }
    }
}