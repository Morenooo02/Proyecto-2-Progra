package cr.ac.una.reservas.logic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceModelRecursoTest {

    @Test
    void agregarRecursoValidoEsExitoso() {
        ServiceModel service = ServiceModel.getInstance();
        Categoria cat = service.buscarCategoria("1");
        Recurso r = new Recurso("REC-TEST-1", "Recurso de prueba", cat);
        assertTrue(service.agregarRecurso(r));
        assertTrue(service.eliminarRecurso("REC-TEST-1"));
    }

    @Test
    void agregarRecursoConCategoriaInexistenteFalla() {
        ServiceModel service = ServiceModel.getInstance();
        Recurso r = new Recurso("REC-TEST-2", "Recurso invalido", new Categoria("999", "No existe"));
        assertFalse(service.agregarRecurso(r));
    }

    @Test
    void eliminarRecursoFallaSiTieneReservaActiva() {
        ServiceModel service = ServiceModel.getInstance();
        Categoria cat = new Categoria(null, "Categoria recurso reservado");
        service.agregarCategoria(cat);
        service.agregarRecurso(new Recurso("REC-TEST-3", "Unico recurso de la categoria", cat));

        Reserva r = new Reserva();
        r.setActividad("Reunion recurso reservado");
        r.setFecha(LocalDate.now().plusDays(22));
        r.setHoraInicio(LocalTime.of(9, 0));
        r.setHoraFin(LocalTime.of(10, 0));
        r.setFuncionario(service.buscarFuncionario("100"));
        assertTrue(service.registrarReserva(r, List.of(cat.getId())));

        assertFalse(service.eliminarRecurso("REC-TEST-3"));

        service.cancelarReserva(r.getId());
        service.eliminarRecurso("REC-TEST-3");
        service.eliminarCategoria(cat.getId());
    }
}
