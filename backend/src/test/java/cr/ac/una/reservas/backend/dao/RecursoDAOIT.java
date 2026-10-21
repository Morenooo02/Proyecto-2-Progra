package cr.ac.una.reservas.backend.dao;

import cr.ac.una.reservas.backend.model.Categoria;
import cr.ac.una.reservas.backend.model.Recurso;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecursoDAOIT {

    @Test
    void debeEjecutarCrudDeRecursoConCategoria() throws Exception {
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        RecursoDAO recursoDAO = new RecursoDAO();

        String sufijo = UUID.randomUUID().toString();

        String categoriaAId = "TEST-CAT-A-" + sufijo;
        String categoriaBId = "TEST-CAT-B-" + sufijo;
        String recursoId = "TEST-REC-" + sufijo;

        boolean categoriaACreada = false;
        boolean categoriaBCreada = false;
        boolean recursoCreado = false;

        try {
            // CREATE: crear dos categorías temporales.
            categoriaDAO.create(
                    new Categoria(categoriaAId, "Categoria prueba A")
            );
            categoriaACreada = true;

            categoriaDAO.create(
                    new Categoria(categoriaBId, "Categoria prueba B")
            );
            categoriaBCreada = true;

            // CREATE: registrar un recurso en la categoría A.
            Recurso inicial = new Recurso(
                    recursoId,
                    "Recurso temporal de prueba",
                    new Categoria(categoriaAId, "Categoria prueba A")
            );

            recursoDAO.create(inicial);
            recursoCreado = true;

            // READ: buscarlo y comprobar su categoría.
            Optional<Recurso> encontrado = recursoDAO.findById(recursoId);

            assertTrue(encontrado.isPresent());
            assertEquals(
                    "Recurso temporal de prueba",
                    encontrado.get().getDescripcion()
            );
            assertEquals(
                    categoriaAId,
                    encontrado.get().getCategoria().getId()
            );

            // READ ALL: comprobar que aparezca en la lista.
            assertTrue(
                    recursoDAO.findAll().stream()
                            .anyMatch(r -> recursoId.equals(r.getId()))
            );

            // UPDATE: cambiar descripción y categoría.
            Recurso modificado = new Recurso(
                    recursoId,
                    "Recurso modificado de prueba",
                    new Categoria(categoriaBId, "Categoria prueba B")
            );

            assertTrue(recursoDAO.update(modificado));

            Optional<Recurso> actualizado =
                    recursoDAO.findById(recursoId);

            assertTrue(actualizado.isPresent());
            assertEquals(
                    "Recurso modificado de prueba",
                    actualizado.get().getDescripcion()
            );
            assertEquals(
                    categoriaBId,
                    actualizado.get().getCategoria().getId()
            );

        } finally {
            // Limpiar los registros temporales respetando las FK.
            try {
                if (recursoCreado) {
                    recursoDAO.delete(recursoId);
                }
            } finally {
                try {
                    if (categoriaBCreada) {
                        categoriaDAO.delete(categoriaBId);
                    }
                } finally {
                    if (categoriaACreada) {
                        categoriaDAO.delete(categoriaAId);
                    }
                }
            }
        }

        // DELETE: comprobar que el recurso ya no exista.
        assertTrue(recursoDAO.findById(recursoId).isEmpty());
    }
}