package cr.ac.una.reservas.backend.service;

import cr.ac.una.reservas.backend.model.Categoria;
import cr.ac.una.reservas.backend.model.Funcionario;
import cr.ac.una.reservas.backend.model.Recurso;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PersistenciaServiceIT {

    @Test
    void debeIntegrarCategoriasRecursosUsuariosYFuncionarios()
            throws Exception {

        PersistenciaService service = new PersistenciaService();

        String sufijo = UUID.randomUUID().toString();
        String catA = "IT-CAT-A-" + sufijo;
        String catB = "IT-CAT-B-" + sufijo;
        String recursoId = "IT-REC-" + sufijo;
        String funcionarioId = "IT-FUNC-" + sufijo;

        String claveInicial = "ClaveIT#A2026";
        String claveNueva = "ClaveIT#B2026";

        boolean categoriaACreada = false;
        boolean categoriaBCreada = false;
        boolean recursoCreado = false;
        boolean funcionarioCreado = false;

        try {

            service.registrarCategoria(
                    new Categoria(catA, "Categoria de integración A")
            );
            categoriaACreada = true;

            service.registrarCategoria(
                    new Categoria(catB, "Categoria de integración B")
            );
            categoriaBCreada = true;

            assertTrue(service.buscarCategoria(catA).isPresent());
            assertTrue(
                    service.listarCategorias().stream()
                            .anyMatch(c -> catA.equals(c.getId()))
            );

            assertTrue(service.modificarCategoria(
                    new Categoria(catA, "Categoria A modificada")
            ));


            service.registrarRecurso(
                    new Recurso(
                            recursoId,
                            "Recurso de integración",
                            new Categoria(catA, "Categoria A modificada")
                    )
            );
            recursoCreado = true;

            assertTrue(service.buscarRecurso(recursoId).isPresent());
            assertTrue(
                    service.listarRecursos().stream()
                            .anyMatch(r -> recursoId.equals(r.getId()))
            );

            assertTrue(service.modificarRecurso(
                    new Recurso(
                            recursoId,
                            "Recurso actualizado",
                            new Categoria(catB, "Categoria de integración B")
                    )
            ));

            assertEquals(
                    catB,
                    service.buscarRecurso(recursoId)
                            .orElseThrow()
                            .getCategoria()
                            .getId()
            );


            service.registrarFuncionario(
                    new Funcionario(
                            funcionarioId,
                            "Funcionario de integración",
                            "8888-1234"
                    ),
                    claveInicial
            );
            funcionarioCreado = true;

            assertTrue(service.buscarFuncionario(funcionarioId).isPresent());
            assertTrue(
                    service.listarFuncionarios().stream()
                            .anyMatch(f -> funcionarioId.equals(f.getId()))
            );

            assertTrue(
                    service.iniciarSesion(funcionarioId, claveInicial)
                            .isPresent()
            );


            assertTrue(service.cambiarClave(
                    funcionarioId,
                    claveInicial,
                    claveNueva
            ));

            assertTrue(
                    service.iniciarSesion(funcionarioId, claveInicial)
                            .isEmpty()
            );
            assertTrue(
                    service.iniciarSesion(funcionarioId, claveNueva)
                            .isPresent()
            );


            assertTrue(service.modificarFuncionario(
                    new Funcionario(
                            funcionarioId,
                            "Funcionario actualizado",
                            "8888-5678"
                    )
            ));

            assertEquals(
                    "Funcionario actualizado",
                    service.buscarFuncionario(funcionarioId)
                            .orElseThrow()
                            .getNombre()
            );

        } finally {

            if (funcionarioCreado) {
                service.eliminarFuncionario(funcionarioId);
            }

            if (recursoCreado) {
                service.eliminarRecurso(recursoId);
            }

            if (categoriaBCreada) {
                service.eliminarCategoria(catB);
            }

            if (categoriaACreada) {
                service.eliminarCategoria(catA);
            }
        }
    }
}