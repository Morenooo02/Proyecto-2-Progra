package cr.ac.una.reservas.backend.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void debeVerificarLaClaveCorrecta() {
        String clave = "ClaveSegura123";
        String hash = PasswordUtil.hash(clave);

        assertNotNull(hash);
        assertTrue(hash.startsWith("pbkdf2-sha256$"));
        assertNotEquals(clave, hash);
        assertTrue(PasswordUtil.verify(clave, hash));
    }

    @Test
    void debeRechazarUnaClaveIncorrecta() {
        String hash = PasswordUtil.hash("ClaveSegura123");

        assertFalse(PasswordUtil.verify("OtraClave456", hash));
    }

    @Test
    void debeGenerarHashesDiferentesParaLaMismaClave() {
        String primerHash = PasswordUtil.hash("ClaveSegura123");
        String segundoHash = PasswordUtil.hash("ClaveSegura123");

        assertNotEquals(primerHash, segundoHash);
    }

    @Test
    void debeRechazarClavesNulasOVacias() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PasswordUtil.hash(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> PasswordUtil.hash("")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> PasswordUtil.hash("   ")
        );
    }

    @Test
    void debeRechazarHashesInvalidos() {
        assertFalse(PasswordUtil.verify(null, "hash"));
        assertFalse(PasswordUtil.verify("clave", null));
        assertFalse(PasswordUtil.verify("clave", ""));
        assertFalse(PasswordUtil.verify("clave", "formato-invalido"));

        assertFalse(PasswordUtil.verify(
                "clave",
                "pbkdf2-sha256$999$abc$def"
        ));
    }
}