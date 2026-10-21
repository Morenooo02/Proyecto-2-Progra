package cr.ac.una.reservas.backend.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String FORMAT = "pbkdf2-sha256";

    private static final int ITERATIONS = 310_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña no puede estar vacía."
            );
        }

        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        byte[] derivedKey = deriveKey(
                password.toCharArray(),
                salt,
                ITERATIONS
        );

        return FORMAT + "$"
                + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derivedKey);
    }

    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }

        String[] parts = storedHash.split("\\$", -1);

        if (parts.length != 4 || !FORMAT.equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);

            if (iterations < 10_000 || iterations > 2_000_000) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedKey = Base64.getDecoder().decode(parts[3]);

            if (salt.length != SALT_LENGTH || expectedKey.length != 32) {
                return false;
            }

            byte[] calculatedKey = deriveKey(
                    password.toCharArray(),
                    salt,
                    iterations
            );

            return MessageDigest.isEqual(expectedKey, calculatedKey);

        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] deriveKey(
            char[] password,
            byte[] salt,
            int iterations
    ) {
        PBEKeySpec spec = new PBEKeySpec(
                password,
                salt,
                iterations,
                KEY_LENGTH_BITS
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(ALGORITHM);

            return factory.generateSecret(spec).getEncoded();

        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(
                    "No se pudo calcular el hash de la contraseña.",
                    ex
            );

        } finally {
            spec.clearPassword();
            Arrays.fill(password, '\0');
        }
    }
}