package cr.ac.una.reservas.backend.dao;

import cr.ac.una.reservas.backend.data.Database;
import cr.ac.una.reservas.backend.model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAO {

    // Registrar una categoría.
    public void create(Categoria categoria) throws SQLException {
        validarCategoria(categoria);

        String sql = """
                INSERT INTO categorias (id, descripcion)
                VALUES (?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, categoria.getId());
            statement.setString(2, categoria.getDescripcion());

            statement.executeUpdate();
        }
    }

    // Buscar una categoría por su identificador.
    public Optional<Categoria> findById(String id)
            throws SQLException {

        validarId(id);

        String sql = """
                SELECT id, descripcion
                FROM categorias
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapearCategoria(result));
                }
            }
        }

        return Optional.empty();
    }

    // Listar todas las categorías.
    public List<Categoria> findAll() throws SQLException {
        String sql = """
                SELECT id, descripcion
                FROM categorias
                ORDER BY id
                """;

        List<Categoria> categorias = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                categorias.add(mapearCategoria(result));
            }
        }

        return categorias;
    }

    // Modificar la descripción de una categoría existente.
    public boolean update(Categoria categoria) throws SQLException {
        validarCategoria(categoria);

        String sql = """
                UPDATE categorias
                SET descripcion = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, categoria.getDescripcion());
            statement.setString(2, categoria.getId());

            return statement.executeUpdate() == 1;
        }
    }

    // Eliminar una categoría.
    public boolean delete(String id) throws SQLException {
        validarId(id);

        String sql = """
                DELETE FROM categorias
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);

            return statement.executeUpdate() == 1;
        }
    }

    // Convertir una fila SQL en un objeto Java.
    private Categoria mapearCategoria(ResultSet result)
            throws SQLException {

        return new Categoria(
                result.getString("id"),
                result.getString("descripcion")
        );
    }

    // Validaciones básicas antes de enviar datos a MySQL.
    private void validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException(
                    "La categoría no puede ser nula."
            );
        }

        validarId(categoria.getId());

        String descripcion = categoria.getDescripcion();

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