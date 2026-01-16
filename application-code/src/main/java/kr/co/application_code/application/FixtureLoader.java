package kr.co.application_code.application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class FixtureLoader {

    private FixtureLoader() {
    }

    public static String loadUtf8(String classpathLocation) {
        try (InputStream is = FixtureLoader.class.getClassLoader().getResourceAsStream(classpathLocation)) {
            if (is == null) {
                throw new IllegalArgumentException("Fixture not found: " + classpathLocation);
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load fixture: " + classpathLocation, e);
        }
    }
}
