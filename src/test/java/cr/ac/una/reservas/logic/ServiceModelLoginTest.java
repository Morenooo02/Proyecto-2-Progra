package cr.ac.una.reservas.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServiceModelLoginTest {

    @Test
    void loginAdminCorrecto() {
        Usuario u = ServiceModel.getInstance().login("admin", "admin");
        assertNotNull(u);
        assertEquals(Rol.ADMINISTRADOR, u.getRol());
    }

    @Test
    void loginClaveIncorrecta() {
        assertNull(ServiceModel.getInstance().login("admin", "mala"));
    }
}
