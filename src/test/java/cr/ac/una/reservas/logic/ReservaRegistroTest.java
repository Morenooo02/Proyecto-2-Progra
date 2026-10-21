package cr.ac.una.reservas.logic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservaRegistroTest {

    @Test
    void reservaExitosaAsignaPrimerRecursoLibre() {
        ServiceModel service = ServiceModel.getInstance();
        Funcionario f = service.buscarFuncionario("100");
        Reserva r = new Reserva();
        r.setActividad("Reunion de prueba");
        r.setFecha(LocalDate.now().plusDays(20));
        r.setHoraInicio(LocalTime.of(9, 0));
        r.setHoraFin(LocalTime.of(10, 0));
        r.setFuncionario(f);
        assertTrue(service.registrarReserva(r, List.of("1")));
        assertTrue(service.cancelarReserva(r.getId()));
    }

    @Test
    void reservaFallaSiHorasInvalidas() {
        ServiceModel service = ServiceModel.getInstance();
        Reserva r = new Reserva();
        r.setActividad("Mala");
        r.setFecha(LocalDate.now().plusDays(21));
        r.setHoraInicio(LocalTime.of(11, 0));
        r.setHoraFin(LocalTime.of(10, 0));
        r.setFuncionario(service.buscarFuncionario("100"));
        assertFalse(service.registrarReserva(r, List.of("1")));
    }

    @Test
    void reservaFallaSiLaFechaEsAnteriorAHoy() {
        ServiceModel service = ServiceModel.getInstance();
        Reserva r = new Reserva();
        r.setActividad("Reserva pasada");
        r.setFecha(LocalDate.now().minusDays(1));
        r.setHoraInicio(LocalTime.of(11, 0));
        r.setHoraFin(LocalTime.of(12, 0));
        r.setFuncionario(service.buscarFuncionario("100"));

        assertFalse(service.registrarReserva(r, List.of("1")));
    }
}
