package cr.ac.una.reservas.logic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservaTest {

    @Test
    void solapaCuandoElHorarioSeCruzaElMismoDia() {
        Reserva r = new Reserva();
        r.setFecha(LocalDate.of(2026, 9, 11));
        r.setHoraInicio(LocalTime.of(8, 0));
        r.setHoraFin(LocalTime.of(10, 0));

        assertTrue(r.solapaCon(LocalDate.of(2026, 9, 11), LocalTime.of(9, 0), LocalTime.of(11, 0)));
        assertFalse(r.solapaCon(LocalDate.of(2026, 9, 11), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        assertFalse(r.solapaCon(LocalDate.of(2026, 9, 12), LocalTime.of(8, 0), LocalTime.of(10, 0)));
    }
}
