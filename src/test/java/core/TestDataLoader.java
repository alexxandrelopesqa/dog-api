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

    public static String validSubBreed() {
        return readNode("/testdata/breeds.json").path("validSubBreed").asText("afghan");
    }

    public static String breedWithoutSubBreeds() {
        return readNode("/testdata/breeds.json").path("breedWithoutSubBreeds").asText("beagle");
    }

    public static String invalidSubBreed() {
        return readNode("/testdata/breeds.json").path("invalidSubBreed").asText("notasubbreedxyz");
    }

    private static JsonNode readNode(String classpathFile) {
        try (InputStream inputStream = TestDataLoader.class.getResourceAsStream(classpathFile)) {
            if (inputStream == null) {
                throw new IllegalStateException("Arquivo nao encontrado: " + classpathFile);
            }
            return OBJECT_MAPPER.readTree(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Falha ao ler " + classpathFile, exception);
        }
    }
}
