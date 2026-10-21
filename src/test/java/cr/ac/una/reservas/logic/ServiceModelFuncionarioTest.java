package cr.ac.una.reservas.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceModelFuncionarioTest {

    @Test
    void agregarFuncionarioNuevoEsExitoso() {
        ServiceModel service = ServiceModel.getInstance();
        Funcionario f = new Funcionario("F-TEST-1", "Carlos Vindas", "8888-2222");
        assertTrue(service.agregarFuncionario(f));
        assertTrue(service.eliminarFuncionario("F-TEST-1"));
    }

    @Test
    void agregarFuncionarioConIdRepetidoFalla() {
        ServiceModel service = ServiceModel.getInstance();
        Funcionario f = new Funcionario("100", "Otro Nombre", "8888-3333");
        assertFalse(service.agregarFuncionario(f));
    }

    @Test
    void modificarFuncionarioActualizaNombreYTelefono() {
        ServiceModel service = ServiceModel.getInstance();
        service.agregarFuncionario(new Funcionario("F-TEST-2", "Nombre Viejo", "1111-1111"));

        Funcionario cambios = new Funcionario("F-TEST-2", "Nombre Nuevo", "2222-2222");
        assertTrue(service.modificarFuncionario(cambios));
        assertTrue(service.buscarFuncionario("F-TEST-2").getNombre().equals("Nombre Nuevo"));

        service.eliminarFuncionario("F-TEST-2");
    }
}
