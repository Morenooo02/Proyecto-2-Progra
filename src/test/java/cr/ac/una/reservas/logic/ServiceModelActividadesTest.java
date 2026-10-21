package cr.ac.una.reservas.logic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceModelActividadesTest {

    @Test
    void obtenerMatrizActividadesMuestraReservaEnDiaYHoraCorrectos() {
        ServiceModel service = ServiceModel.getInstance();
        LocalDate fecha = LocalDate.now().plusDays(42);
        Reserva r = new Reserva();
        r.setActividad("Reunion actividades");
        r.setFecha(fecha);
        r.setHoraInicio(LocalTime.of(11, 0));
        r.setHoraFin(LocalTime.of(12, 0));
        r.setFuncionario(service.buscarFuncionario("100"));
        assertTrue(service.registrarReserva(r, List.of("2")));

        Object[][] matriz = service.obtenerMatrizActividades(fecha);
        int filaOnce = 11;
        int columnaDia = fecha.getDayOfWeek().getValue();

        assertEquals("Reunion actividades - Ana Perez", matriz[filaOnce][columnaDia]);

        service.cancelarReserva(r.getId());
    }

    @Test
    void obtenerMatrizActividadesConcatenaVariasReservasEnMismoSlot() {
        ServiceModel service = ServiceModel.getInstance();
        LocalDate fecha = LocalDate.now().plusDays(43);
        Reserva r1 = new Reserva();
        r1.setActividad("Reunion actividades A");
        r1.setFecha(fecha);
        r1.setHoraInicio(LocalTime.of(14, 0));
        r1.setHoraFin(LocalTime.of(15, 0));
        r1.setFuncionario(service.buscarFuncionario("100"));
        assertTrue(service.registrarReserva(r1, List.of("1")));

        Reserva r2 = new Reserva();
        r2.setActividad("Reunion actividades B");
        r2.setFecha(fecha);
        r2.setHoraInicio(LocalTime.of(14, 0));
        r2.setHoraFin(LocalTime.of(15, 0));
        r2.setFuncionario(service.buscarFuncionario("100"));
        assertTrue(service.registrarReserva(r2, List.of("3")));

        Object[][] matriz = service.obtenerMatrizActividades(fecha);
        int filaCatorce = 14;
        int columnaDia = fecha.getDayOfWeek().getValue();
        String celda = (String) matriz[filaCatorce][columnaDia];

        assertTrue(celda.contains("Reunion actividades A - Ana Perez"));
        assertTrue(celda.contains("Reunion actividades B - Ana Perez"));
        assertTrue(celda.contains(" / "));

        service.cancelarReserva(r1.getId());
        service.cancelarReserva(r2.getId());
    }

    @Test
    void obtenerMatrizActividadesIncluyeReservaNocturna() {
        ServiceModel service = ServiceModel.getInstance();
        LocalDate fecha = LocalDate.now().plusDays(45);
        Reserva r = new Reserva();
        r.setActividad("Actividad nocturna");
        r.setFecha(fecha);
        r.setHoraInicio(LocalTime.of(20, 0));
        r.setHoraFin(LocalTime.of(21, 0));
        r.setFuncionario(service.buscarFuncionario("100"));
        assertTrue(service.registrarReserva(r, List.of("2")));

        Object[][] matriz = service.obtenerMatrizActividades(fecha);

        assertEquals(24, matriz.length);
        int columnaDia = fecha.getDayOfWeek().getValue();
        assertEquals("Actividad nocturna - Ana Perez", matriz[20][columnaDia]);

        service.cancelarReserva(r.getId());
    }
}
