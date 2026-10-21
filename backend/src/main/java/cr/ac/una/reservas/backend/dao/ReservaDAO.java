package cr.ac.una.reservas.backend.dao;

import cr.ac.una.reservas.backend.data.Database;
import cr.ac.una.reservas.backend.model.Categoria;
import cr.ac.una.reservas.backend.model.EstadoReserva;
import cr.ac.una.reservas.backend.model.Funcionario;
import cr.ac.una.reservas.backend.model.Recurso;
import cr.ac.una.reservas.backend.model.Reserva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ReservaDAO {

    private static final String SELECT_RESERVA = """
            SELECT r.id,
                   r.actividad,
                   r.fecha,
                   r.hora_inicio,
                   r.hora_fin,
                   r.estado,
                   f.id AS funcionario_id,
                   f.nombre AS funcionario_nombre,
                   f.telefono AS funcionario_telefono
            FROM reservas r
            INNER JOIN funcionarios f ON f.id = r.funcionario_id
            """;


    public void create(Reserva reserva) throws SQLException {
        validarReserva(reserva);

        String sql = """
                INSERT INTO reservas
                    (id, actividad, fecha, hora_inicio, hora_fin,
                     estado, funcionario_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    asignarDatosReserva(statement, reserva);
                    statement.executeUpdate();
                }

                insertarRecursos(connection, reserva);
                insertarCategorias(connection, reserva);

                connection.commit();

            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }


    public Optional<Reserva> findById(String id) throws SQLException {
        validarIdReserva(id);

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             SELECT_RESERVA + " WHERE r.id = ?")) {

            statement.setString(1, id);

            Reserva reserva;

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                reserva = mapearReserva(result);
            }

            cargarRelaciones(connection, reserva);
            return Optional.of(reserva);
        }
    }


    public List<Reserva> findAll() throws SQLException {
        List<Reserva> reservas = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SELECT_RESERVA + " ORDER BY r.fecha, r.hora_inicio");
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                reservas.add(mapearReserva(result));
            }

            // El ResultSet principal se termina antes de consultar
            // las relaciones de cada reserva.
            result.close();

            for (Reserva reserva : reservas) {
                cargarRelaciones(connection, reserva);
            }
        }

        return reservas;
    }


    public boolean update(Reserva reserva) throws SQLException {
        validarReserva(reserva);

        String sql = """
                UPDATE reservas
                SET actividad = ?,
                    fecha = ?,
                    hora_inicio = ?,
                    hora_fin = ?,
                    estado = ?,
                    funcionario_id = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                if (!existeReserva(connection, reserva.getId())) {
                    connection.rollback();
                    return false;
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(1, reserva.getActividad());
                    statement.setDate(
                            2, Date.valueOf(reserva.getFecha()));
                    statement.setTime(
                            3, Time.valueOf(reserva.getHoraInicio()));
                    statement.setTime(
                            4, Time.valueOf(reserva.getHoraFin()));
                    statement.setString(
                            5, reserva.getEstado().name());
                    statement.setString(
                            6, reserva.getFuncionario().getId());
                    statement.setString(7, reserva.getId());

                    statement.executeUpdate();
                }

                eliminarRelaciones(connection, reserva.getId());
                insertarRecursos(connection, reserva);
                insertarCategorias(connection, reserva);

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }


    public boolean delete(String id) throws SQLException {
        validarIdReserva(id);

        String sql = "DELETE FROM reservas WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);
            return statement.executeUpdate() == 1;
        }
    }


    public boolean cancel(String id) throws SQLException {
        validarIdReserva(id);

        String sql = """
                UPDATE reservas
                SET estado = 'CANCELADA'
                WHERE id = ? AND estado = 'ACTIVA'
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private void asignarDatosReserva(
            PreparedStatement statement,
            Reserva reserva
    ) throws SQLException {

        statement.setString(1, reserva.getId());
        statement.setString(2, reserva.getActividad());
        statement.setDate(3, Date.valueOf(reserva.getFecha()));
        statement.setTime(4, Time.valueOf(reserva.getHoraInicio()));
        statement.setTime(5, Time.valueOf(reserva.getHoraFin()));
        statement.setString(6, reserva.getEstado().name());
        statement.setString(7, reserva.getFuncionario().getId());
    }

    private Reserva mapearReserva(ResultSet result) throws SQLException {
        Reserva reserva = new Reserva();

        reserva.setId(result.getString("id"));
        reserva.setActividad(result.getString("actividad"));
        reserva.setFecha(result.getDate("fecha").toLocalDate());
        reserva.setHoraInicio(
                result.getTime("hora_inicio").toLocalTime());
        reserva.setHoraFin(
                result.getTime("hora_fin").toLocalTime());
        reserva.setEstado(
                EstadoReserva.valueOf(result.getString("estado")));

        Funcionario funcionario = new Funcionario(
                result.getString("funcionario_id"),
                result.getString("funcionario_nombre"),
                result.getString("funcionario_telefono")
        );

        reserva.setFuncionario(funcionario);
        return reserva;
    }

    private void cargarRelaciones(
            Connection connection,
            Reserva reserva
    ) throws SQLException {

        String sqlRecursos = """
                SELECT r.id,
                       r.descripcion,
                       c.id AS categoria_id,
                       c.descripcion AS categoria_descripcion
                FROM reserva_recurso rr
                INNER JOIN recursos r ON r.id = rr.recurso_id
                INNER JOIN categorias c ON c.id = r.categoria_id
                WHERE rr.reserva_id = ?
                ORDER BY r.id
                """;

        List<Recurso> recursos = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sqlRecursos)) {

            statement.setString(1, reserva.getId());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Categoria categoria = new Categoria(
                            result.getString("categoria_id"),
                            result.getString("categoria_descripcion")
                    );

                    recursos.add(new Recurso(
                            result.getString("id"),
                            result.getString("descripcion"),
                            categoria
                    ));
                }
            }
        }

        reserva.setRecursosAsignados(recursos);

        String sqlCategorias = """
                SELECT c.id, c.descripcion
                FROM reserva_categoria rc
                INNER JOIN categorias c ON c.id = rc.categoria_id
                WHERE rc.reserva_id = ?
                ORDER BY c.id
                """;

        List<Categoria> categorias = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sqlCategorias)) {

            statement.setString(1, reserva.getId());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    categorias.add(new Categoria(
                            result.getString("id"),
                            result.getString("descripcion")
                    ));
                }
            }
        }

        reserva.setCategoriasSolicitadas(categorias);
    }

    private void insertarRecursos(
            Connection connection,
            Reserva reserva
    ) throws SQLException {

        String sql = """
                INSERT INTO reserva_recurso (reserva_id, recurso_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (Recurso recurso : reserva.getRecursosAsignados()) {
                statement.setString(1, reserva.getId());
                statement.setString(2, recurso.getId());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void insertarCategorias(
            Connection connection,
            Reserva reserva
    ) throws SQLException {

        String sql = """
                INSERT INTO reserva_categoria (reserva_id, categoria_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (Categoria categoria : reserva.getCategoriasSolicitadas()) {
                statement.setString(1, reserva.getId());
                statement.setString(2, categoria.getId());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void eliminarRelaciones(
            Connection connection,
            String reservaId
    ) throws SQLException {

        try (PreparedStatement recursos = connection.prepareStatement(
                "DELETE FROM reserva_recurso WHERE reserva_id = ?");
             PreparedStatement categorias = connection.prepareStatement(
                     "DELETE FROM reserva_categoria WHERE reserva_id = ?")) {

            recursos.setString(1, reservaId);
            recursos.executeUpdate();

            categorias.setString(1, reservaId);
            categorias.executeUpdate();
        }
    }

    private boolean existeReserva(
            Connection connection,
            String id
    ) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             "SELECT 1 FROM reservas WHERE id = ? FOR UPDATE")) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private void validarReserva(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException(
                    "La reserva no puede ser nula.");
        }

        validarIdReserva(reserva.getId());

        if (reserva.getActividad() == null
                || reserva.getActividad().isBlank()
                || reserva.getActividad().length() > 255) {
            throw new IllegalArgumentException(
                    "La actividad es obligatoria y admite hasta 255 caracteres.");
        }

        if (reserva.getFecha() == null
                || reserva.getHoraInicio() == null
                || reserva.getHoraFin() == null) {
            throw new IllegalArgumentException(
                    "La fecha y las horas son obligatorias.");
        }

        if (!reserva.getHoraFin().isAfter(reserva.getHoraInicio())) {
            throw new IllegalArgumentException(
                    "La hora final debe ser posterior a la hora inicial.");
        }

        if (reserva.getEstado() == null) {
            throw new IllegalArgumentException(
                    "El estado de la reserva es obligatorio.");
        }

        if (reserva.getFuncionario() == null
                || reserva.getFuncionario().getId() == null
                || reserva.getFuncionario().getId().isBlank()) {
            throw new IllegalArgumentException(
                    "La reserva debe tener un funcionario.");
        }

        if (reserva.getRecursosAsignados() == null
                || reserva.getCategoriasSolicitadas() == null) {
            throw new IllegalArgumentException(
                    "Las listas de recursos y categorías son obligatorias.");
        }

        Set<String> idsRecursos = new HashSet<>();

        for (Recurso recurso : reserva.getRecursosAsignados()) {
            if (recurso == null || recurso.getId() == null
                    || recurso.getId().isBlank()) {
                throw new IllegalArgumentException(
                        "Todas las asignaciones deben tener un recurso válido.");
            }

            if (!idsRecursos.add(recurso.getId())) {
                throw new IllegalArgumentException(
                        "La reserva contiene un recurso duplicado.");
            }
        }

        Set<String> idsCategorias = new HashSet<>();

        for (Categoria categoria : reserva.getCategoriasSolicitadas()) {
            if (categoria == null || categoria.getId() == null
                    || categoria.getId().isBlank()) {
                throw new IllegalArgumentException(
                        "Todas las categorías solicitadas deben ser válidas.");
            }

            if (!idsCategorias.add(categoria.getId())) {
                throw new IllegalArgumentException(
                        "La reserva contiene una categoría duplicada.");
            }
        }
    }

    private void validarIdReserva(String id) {
        if (id == null || id.isBlank() || id.length() > 100) {
            throw new IllegalArgumentException(
                    "El identificador de reserva es obligatorio "
                            + "y admite hasta 100 caracteres.");
        }
    }
}