package cr.ac.una.reservas.backend.dao;

import cr.ac.una.reservas.backend.data.Database;
import cr.ac.una.reservas.backend.model.Categoria;
import cr.ac.una.reservas.backend.model.EstadoReserva;
import cr.ac.una.reservas.backend.model.Funcionario;
import cr.ac.una.reservas.backend.model.Recurso;
import cr.ac.una.reservas.backend.model.Reserva;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservaDAOIT {

    @Test
    void debeEjecutarCrudDeReservaYRelaciones() throws Exception {
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        RecursoDAO recursoDAO = new RecursoDAO();
        FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
        ReservaDAO reservaDAO = new ReservaDAO();

        String sufijo = UUID.randomUUID().toString();

        String categoriaAId = "IT-CAT-A-" + sufijo;
        String categoriaBId = "IT-CAT-B-" + sufijo;
        String recursoAId = "IT-REC-A-" + sufijo;
        String recursoBId = "IT-REC-B-" + sufijo;
        String funcionarioId = "IT-FUNC-" + sufijo;
        String reservaId = "IT-RES-" + sufijo;

        try {

            Categoria categoriaA =
                    new Categoria(categoriaAId, "Categoria temporal A");
            Categoria categoriaB =
                    new Categoria(categoriaBId, "Categoria temporal B");

            categoriaDAO.create(categoriaA);
            categoriaDAO.create(categoriaB);


            Recurso recursoA = new Recurso(
                    recursoAId, "Recurso temporal A", categoriaA);

            Recurso recursoB = new Recurso(
                    recursoBId, "Recurso temporal B", categoriaB);

            recursoDAO.create(recursoA);
            recursoDAO.create(recursoB);


            Funcionario funcionario = new Funcionario(
                    funcionarioId,
                    "Funcionario temporal",
                    "8888-1234"
            );

            funcionarioDAO.create(funcionario, "ClaveIT#A2026");


            Reserva reserva = new Reserva();
            reserva.setId(reservaId);
            reserva.setActividad("Actividad temporal de integración");
            reserva.setFecha(LocalDate.now().plusDays(2));
            reserva.setHoraInicio(LocalTime.of(10, 0));
            reserva.setHoraFin(LocalTime.of(11, 0));
            reserva.setEstado(EstadoReserva.ACTIVA);
            reserva.setFuncionario(funcionario);
            reserva.setRecursosAsignados(List.of(recursoA));
            reserva.setCategoriasSolicitadas(List.of(categoriaA));

            reservaDAO.create(reserva);


            Reserva encontrada = reservaDAO.findById(reservaId)
                    .orElseThrow();

            assertEquals(reservaId, encontrada.getId());
            assertEquals(
                    "Actividad temporal de integración",
                    encontrada.getActividad()
            );
            assertEquals(EstadoReserva.ACTIVA, encontrada.getEstado());
            assertEquals(funcionarioId, encontrada.getFuncionario().getId());

            assertEquals(1, encontrada.getRecursosAsignados().size());
            assertEquals(
                    recursoAId,
                    encontrada.getRecursosAsignados().get(0).getId()
            );

            assertEquals(1, encontrada.getCategoriasSolicitadas().size());
            assertEquals(
                    categoriaAId,
                    encontrada.getCategoriasSolicitadas().get(0).getId()
            );


            assertTrue(
                    reservaDAO.findAll().stream()
                            .anyMatch(r -> reservaId.equals(r.getId()))
            );


            Reserva modificada = new Reserva();
            modificada.setId(reservaId);
            modificada.setActividad("Actividad temporal modificada");
            modificada.setFecha(LocalDate.now().plusDays(3));
            modificada.setHoraInicio(LocalTime.of(12, 0));
            modificada.setHoraFin(LocalTime.of(13, 0));
            modificada.setEstado(EstadoReserva.ACTIVA);
            modificada.setFuncionario(funcionario);
            modificada.setRecursosAsignados(List.of(recursoB));
            modificada.setCategoriasSolicitadas(List.of(categoriaB));

            assertTrue(reservaDAO.update(modificada));

            Reserva actualizada = reservaDAO.findById(reservaId)
                    .orElseThrow();

            assertEquals(
                    "Actividad temporal modificada",
                    actualizada.getActividad()
            );
            assertEquals(recursoBId,
                    actualizada.getRecursosAsignados().get(0).getId());
            assertEquals(categoriaBId,
                    actualizada.getCategoriasSolicitadas().get(0).getId());


            assertTrue(reservaDAO.cancel(reservaId));

            Reserva cancelada = reservaDAO.findById(reservaId)
                    .orElseThrow();

            assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());


            assertTrue(reservaDAO.delete(reservaId));
            assertTrue(reservaDAO.findById(reservaId).isEmpty());

        } finally {

            limpiarDatos(
                    reservaId,
                    recursoAId,
                    recursoBId,
                    categoriaAId,
                    categoriaBId,
                    funcionarioId
            );
        }
    }

    private void limpiarDatos(
            String reservaId,
            String recursoAId,
            String recursoBId,
            String categoriaAId,
            String categoriaBId,
            String funcionarioId
    ) throws Exception {

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {

                eliminar(connection,
                        "DELETE FROM reserva_recurso WHERE reserva_id = ?",
                        reservaId);

                eliminar(connection,
                        "DELETE FROM reserva_categoria WHERE reserva_id = ?",
                        reservaId);


                eliminar(connection,
                        "DELETE FROM reservas WHERE id = ?",
                        reservaId);

                eliminar(connection,
                        "DELETE FROM recursos WHERE id = ?",
                        recursoAId);

                eliminar(connection,
                        "DELETE FROM recursos WHERE id = ?",
                        recursoBId);


                eliminar(connection,
                        "DELETE FROM categorias WHERE id = ?",
                        categoriaAId);

                eliminar(connection,
                        "DELETE FROM categorias WHERE id = ?",
                        categoriaBId);


                eliminar(connection,
                        "DELETE FROM funcionarios WHERE id = ?",
                        funcionarioId);

                eliminar(connection,
                        "DELETE FROM usuarios WHERE id = ?",
                        funcionarioId);

                connection.commit();

            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    private void eliminar(
            Connection connection,
            String sql,
            String id
    ) throws Exception {

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);
            statement.executeUpdate();
        }
    }
}