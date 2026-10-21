package cr.ac.una.reservas.backend.service;

import cr.ac.una.reservas.backend.data.Database;
import cr.ac.una.reservas.backend.model.*;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PersistenciaServiceReservasIT {

    @Test
    void debeProbarOperacionesDeReservaDesdeElServicio()
            throws Exception {

        PersistenciaService service = new PersistenciaService();

        String sufijo = UUID.randomUUID().toString();
        String catA = "IT-CAT-A-" + sufijo;
        String catB = "IT-CAT-B-" + sufijo;
        String recA = "IT-REC-A-" + sufijo;
        String recB = "IT-REC-B-" + sufijo;
        String funcionarioId = "IT-FUNC-" + sufijo;
        String reservaId = "IT-RES-" + sufijo;

        try {
            Categoria categoriaA =
                    new Categoria(catA, "Categoria temporal A");
            Categoria categoriaB =
                    new Categoria(catB, "Categoria temporal B");

            service.registrarCategoria(categoriaA);
            service.registrarCategoria(categoriaB);

            Recurso recursoA =
                    new Recurso(recA, "Recurso temporal A", categoriaA);
            Recurso recursoB =
                    new Recurso(recB, "Recurso temporal B", categoriaB);

            service.registrarRecurso(recursoA);
            service.registrarRecurso(recursoB);

            Funcionario funcionario = new Funcionario(
                    funcionarioId, "Funcionario temporal", "8888-1234");

            service.registrarFuncionario(funcionario, "ClaveIT#A2026");


            Reserva reserva = new Reserva();
            reserva.setId(reservaId);
            reserva.setActividad("Actividad temporal");
            reserva.setFecha(LocalDate.now().plusDays(2));
            reserva.setHoraInicio(LocalTime.of(10, 0));
            reserva.setHoraFin(LocalTime.of(11, 0));
            reserva.setEstado(EstadoReserva.ACTIVA);
            reserva.setFuncionario(funcionario);
            reserva.setRecursosAsignados(List.of(recursoA));
            reserva.setCategoriasSolicitadas(List.of(categoriaA));

            service.registrarReserva(reserva);


            Reserva encontrada =
                    service.buscarReserva(reservaId).orElseThrow();

            assertEquals("Actividad temporal", encontrada.getActividad());
            assertEquals(recA,
                    encontrada.getRecursosAsignados().get(0).getId());

            assertTrue(service.listarReservas().stream()
                    .anyMatch(r -> reservaId.equals(r.getId())));


            reserva.setActividad("Actividad modificada");
            reserva.setRecursosAsignados(List.of(recursoB));
            reserva.setCategoriasSolicitadas(List.of(categoriaB));

            assertTrue(service.modificarReserva(reserva));

            Reserva actualizada =
                    service.buscarReserva(reservaId).orElseThrow();

            assertEquals("Actividad modificada",
                    actualizada.getActividad());
            assertEquals(recB,
                    actualizada.getRecursosAsignados().get(0).getId());
            assertEquals(catB,
                    actualizada.getCategoriasSolicitadas().get(0).getId());


            assertTrue(service.cancelarReserva(reservaId));

            assertEquals(EstadoReserva.CANCELADA,
                    service.buscarReserva(reservaId)
                            .orElseThrow().getEstado());


            assertTrue(service.eliminarReserva(reservaId));
            assertTrue(service.buscarReserva(reservaId).isEmpty());

        } finally {
            limpiarDatos(reservaId, recA, recB, catA, catB, funcionarioId);
        }
    }

    private void limpiarDatos(
            String reservaId,
            String recA,
            String recB,
            String catA,
            String catB,
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
                        "DELETE FROM recursos WHERE id = ?", recA);
                eliminar(connection,
                        "DELETE FROM recursos WHERE id = ?", recB);

                eliminar(connection,
                        "DELETE FROM categorias WHERE id = ?", catA);
                eliminar(connection,
                        "DELETE FROM categorias WHERE id = ?", catB);

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