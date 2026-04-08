package core;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.qameta.allure.Allure;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;

public final class ApiAssertions {

    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("^https?://.+\\.(jpg|jpeg|png|webp)$", Pattern.CASE_INSENSITIVE);

    private ApiAssertions() {
    }

    public static void assertHttpAndContentType(Response response, int expectedHttpStatus) {
        Allure.step("Status HTTP e Content-Type", () -> {
            assertEqualsWithContext("statusCode", expectedHttpStatus, response.getStatusCode());
            String contentType = response.getContentType();
            assertNotNull(contentType, "Content-Type nulo");
            assertTrue(contentType.toLowerCase().contains("application/json"), "Content-Type deve conter application/json");
        });
    }

    public static void assertResponseTime(Response response) {
        Allure.step("Tempo de resposta dentro do limite", () ->
            assertTrue(
                response.time() <= ConfigManager.maxResponseTimeMs(),
                "Tempo de resposta acima do limite configurado: " + response.time() + "ms"
            )
        );
    }

    public static void assertSchema(Response response, String schemaPath) {
        Allure.step("Schema " + schemaPath, () ->
            response.then().body(matchesJsonSchemaInClasspath(schemaPath))
        );
    }

    public static void assertMandatoryKeys(Response response, String expectedStatus) {
        Allure.step("Chaves status e message", () -> {
            JsonPath jsonPath = response.jsonPath();
            Map<String, Object> map = jsonPath.getMap("$");
            assertNotNull(map, "payload nulo");
            assertTrue(map.containsKey("status"), "Payload deve conter chave 'status'");
            assertTrue(map.containsKey("message"), "Payload deve conter chave 'message'");
            assertEqualsWithContext("payload.status", expectedStatus, jsonPath.getString("status"));
            assertNotNull(jsonPath.get("message"), "message nulo");
        });
    }

    public static void assertBreedsListStructure(Response response) {
        Allure.step("Mapa de raças em message", () -> {
            Map<String, List<String>> breeds = response.jsonPath().getMap("message");
            assertNotNull(breeds, "Campo message deve ser um mapa");
            assertFalse(breeds.isEmpty(), "mapa de raças vazio");
            assertTrue(breeds.containsKey("hound"), "falta raça hound");
            for (Map.Entry<String, List<String>> entry : breeds.entrySet()) {
                assertNotNull(entry.getKey(), "nome de raça nulo");
                assertNotNull(entry.getValue(), "lista de sub-raças nula");
            }
        });
    }

    public static void assertBreedImagesStructure(Response response, String breed) {
        Allure.step("URLs da raça " + breed, () -> {
            List<String> images = response.jsonPath().getList("message");
            assertNotNull(images, "lista de imagens nula");
            assertFalse(images.isEmpty(), "lista de imagens vazia");
            String firstImage = images.get(0);
            assertTrue(firstImage.contains(breed), "URL sem a raça pedida");
            assertTrue(IMAGE_URL_PATTERN.matcher(firstImage).matches(), "URL com formato estranho");
        });
    }

    public static void assertRandomImageStructure(Response response) {
        Allure.step("URL no random", () -> {
            String imageUrl = response.jsonPath().getString("message");
            assertNotNull(imageUrl, "message nulo");
            assertTrue(imageUrl.contains("images.dog.ceo"), "fora do CDN dog.ceo");
            assertTrue(IMAGE_URL_PATTERN.matcher(imageUrl).matches(), "URL com formato estranho");
        });
    }

    public static void assertErrorMessage(Response response, String expectedMessageFragment) {
        Allure.step("Mensagem de erro", () -> {
            String message = response.jsonPath().getString("message");
            assertNotNull(message, "mensagem de erro nula");
            assertFalse(message.isBlank(), "mensagem de erro vazia");
            assertTrue(
                message.toLowerCase().contains(expectedMessageFragment.toLowerCase()),
                "mensagem sem '" + expectedMessageFragment + "'"
            );
        });
    }

    public static void assertMessageUrlArray(Response response, int minSize, Integer maxSize) {
        Allure.step("Lista message: tamanho e URLs", () -> {
            List<String> urls = response.jsonPath().getList("message");
            assertNotNull(urls, "message deve ser lista");
            assertTrue(urls.size() >= minSize, "menos de " + minSize + " itens");
            if (maxSize != null) {
                assertTrue(urls.size() <= maxSize, "mais de " + maxSize + " itens: " + urls.size());
            }
            for (String url : urls) {
                assertNotNull(url);
                assertTrue(url.startsWith("https://") || url.startsWith("http://"), "URL estranha: " + url);
                assertTrue(
                    url.contains("images.dog.ceo"),
                    "fora de images.dog.ceo: " + url
                );
            }
        });
    }

    public static void assertSubBreedListContains(Response response, String... expectedSubBreeds) {
        Allure.step("Sub-racas esperadas na lista", () -> {
            List<String> subs = response.jsonPath().getList("message");
            assertNotNull(subs, "message deve ser lista");
            for (String expected : expectedSubBreeds) {
                boolean found = subs.stream().anyMatch(s -> s.equalsIgnoreCase(expected));
                assertTrue(found, "falta sub-raca '" + expected + "': " + subs);
            }
        });
    }

    public static void assertSubBreedImageUrls(Response response, String breed, String sub) {
        Allure.step("URLs da sub-raca " + breed + "/" + sub, () -> {
            List<String> images = response.jsonPath().getList("message");
            assertNotNull(images, "lista nula");
            assertFalse(images.isEmpty(), "lista vazia");
            String first = images.get(0).toLowerCase();
            assertTrue(
                Stream.of(breed, sub).anyMatch(part -> first.contains(part.toLowerCase())),
                "URL sem trecho da raça/sub: " + first
            );
        });
    }

    private static void assertEqualsWithContext(String field, Object expected, Object actual) {
        try {
            Assertions.assertEquals(expected, actual, field + " diferente");
        } catch (AssertionError error) {
            AllureReportManager.attachAssertionContext(field, expected, actual);
            throw error;
        }
    }
}
