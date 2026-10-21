package cr.ac.una.reservas.backend.dao;

import cr.ac.una.reservas.backend.model.Categoria;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaDAOIT {

    @Test
    void debeEjecutarCrudDeCategoria() throws Exception {
        CategoriaDAO dao = new CategoriaDAO();

        // Identificador único para no interferir con datos existentes.
        String id = "TEST-" + UUID.randomUUID();

        Categoria inicial =
                new Categoria(id, "Categoria temporal de prueba");

        try {
            // CREATE: registrar una categoría.
            dao.create(inicial);

            // READ: buscarla por identificador.
            Optional<Categoria> encontrada = dao.findById(id);

            assertTrue(encontrada.isPresent());
            assertEquals(
                    "Categoria temporal de prueba",
                    encontrada.get().getDescripcion()
            );

            // READ ALL: verificar que aparezca en la lista.
            assertTrue(
                    dao.findAll().stream().anyMatch(c -> id.equals(c.getId()))
            );

            // UPDATE: modificar su descripción.
            Categoria modificada = new Categoria(id, "Categoria modificada de prueba");

            assertTrue(dao.update(modificada));

            Optional<Categoria> actualizada = dao.findById(id);

            assertTrue(actualizada.isPresent());
            assertEquals("Categoria modificada de prueba", actualizada.get().getDescripcion());

        } finally {
            // DELETE: limpiar el registro temporal, incluso si una
            // aserción falla después de haberlo creado.
            Optional<Categoria> existente = dao.findById(id);

            if (existente.isPresent()) {
                assertTrue(dao.delete(id));
            }
        }

        // Confirmar que ya no existe.
        assertTrue(dao.findById(id).isEmpty());
    }
}