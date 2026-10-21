package cr.ac.una.reservas.logic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceModelEstadisticasTest {

    @Test
    void estadisticasRecursosContabilizaReservaActiva() {
        ServiceModel service = ServiceModel.getInstance();
        LocalDate fecha = LocalDate.now().plusDays(25);
        Reserva r = new Reserva();
        r.setActividad("Reunion estadisticas recursos");
        r.setFecha(fecha);
        r.setHoraInicio(LocalTime.of(9, 0));
        r.setHoraFin(LocalTime.of(10, 0));
        r.setFuncionario(service.buscarFuncionario("100"));
        assertTrue(service.registrarReserva(r, List.of("1")));

        Map<String, Integer> mapa = service.obtenerEstadisticasRecursos(fecha.minusDays(1), fecha.plusDays(1));
        assertTrue(mapa.getOrDefault("Sala para 10 personas", 0) >= 1);

        service.cancelarReserva(r.getId());
    }

    @Test
    void estadisticasActividadesContabilizaPorSemana() {
        ServiceModel service = ServiceModel.getInstance();
        LocalDate fecha = LocalDate.now().plusDays(26);
        Reserva r = new Reserva();
        r.setActividad("Reunion estadisticas actividades");
        r.setFecha(fecha);
        r.setHoraInicio(LocalTime.of(11, 0));
        r.setHoraFin(LocalTime.of(12, 0));
        r.setFuncionario(service.buscarFuncionario("100"));
        assertTrue(service.registrarReserva(r, List.of("2")));

        Map<String, Integer> mapa = service.obtenerEstadisticasActividades(fecha.minusDays(1), fecha.plusDays(1));
        assertTrue(mapa.keySet().stream().anyMatch(k -> k.startsWith("Semana")));

        service.cancelarReserva(r.getId());
    }
}
