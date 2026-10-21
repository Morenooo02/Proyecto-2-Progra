package cr.ac.una.reservas.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceModelCategoriaTest {

    @Test
    void agregarCategoriaAsignaIdAutogenerado() {
        ServiceModel service = ServiceModel.getInstance();
        Categoria c = new Categoria(null, "Categoria de prueba");
        assertTrue(service.agregarCategoria(c));
        assertTrue(c.getId() != null && !c.getId().isBlank());

        service.eliminarCategoria(c.getId());
    }

    @Test
    void eliminarCategoriaFallaSiTieneRecursoAsociado() {
        ServiceModel service = ServiceModel.getInstance();
        assertFalse(service.eliminarCategoria("1"));
    }

    @Test
    void buscarCategoriasFiltraPorDescripcion() {
        ServiceModel service = ServiceModel.getInstance();
        assertTrue(service.buscarCategorias("laptop").size() >= 1);
        assertTrue(service.buscarCategorias("categoria-que-no-existe").isEmpty());
    }
}
