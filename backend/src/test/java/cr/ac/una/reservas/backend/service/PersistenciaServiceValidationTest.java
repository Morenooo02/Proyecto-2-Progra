package cr.ac.una.reservas.backend.service;

import cr.ac.una.reservas.backend.model.Categoria;
import cr.ac.una.reservas.backend.model.Recurso;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersistenciaServiceValidationTest {

    @Test
    void debeRechazarUnRecursoNulo() {
        PersistenciaService service = new PersistenciaService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarRecurso(null)
        );
    }

    @Test
    void debeRechazarUnRecursoSinCategoria() {
        PersistenciaService service = new PersistenciaService();
        Recurso recurso = new Recurso(
                "REC-TEST",
                "Recurso de prueba",
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarRecurso(recurso)
        );
    }

    @Test
    void debeRechazarUnaCategoriaConIdNulo() {
        PersistenciaService service = new PersistenciaService();
        Categoria categoria = new Categoria(
                null,
                "Categoría de prueba"
        );
        Recurso recurso = new Recurso(
                "REC-TEST",
                "Recurso de prueba",
                categoria
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarRecurso(recurso)
        );
    }

    @Test
    void debeRechazarUnaCategoriaConIdVacio() {
        PersistenciaService service = new PersistenciaService();
        Categoria categoria = new Categoria(
                "   ",
                "Categoría de prueba"
        );
        Recurso recurso = new Recurso(
                "REC-TEST",
                "Recurso de prueba",
                categoria
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarRecurso(recurso)
        );
    }

    @Test
    void debeRechazarUnaReservaNula() {
        PersistenciaService service = new PersistenciaService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarReserva(null)
        );
    }
}