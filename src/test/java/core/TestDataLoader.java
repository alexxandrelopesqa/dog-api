package core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

public final class TestDataLoader {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TestDataLoader() {
    }

    public static String validBreed() {
        return readNode("/testdata/breeds.json").path("validBreed").asText("hound");
    }

    public static String invalidBreed() {
        return readNode("/testdata/breeds.json").path("invalidBreed").asText("invalidbreed");
    }

    private static JsonNode readNode(String classpathFile) {
        try (InputStream inputStream = TestDataLoader.class.getResourceAsStream(classpathFile)) {
            if (inputStream == null) {
                throw new IllegalStateException("Arquivo de massa nao encontrado: " + classpathFile);
            }
            return OBJECT_MAPPER.readTree(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Falha ao ler massa de testes: " + classpathFile, exception);
        }
    }
}
