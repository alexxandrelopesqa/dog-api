package tests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

import clients.DogApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Owner;
import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import java.util.stream.Stream;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import utils.AllureReportManager;
import utils.ConfigManager;

@Epic("API Test Automation")
@Feature("Dog API")
@Owner("alexxandrelopesqa")
class DogApiTests {

    private static final int MAX_SLA_ATTEMPTS = Integer.parseInt(System.getProperty("dog.api.slaAttempts", "10"));
    private static final long RETRY_BACKOFF_MS = Long.parseLong(System.getProperty("dog.api.slaBackoffMs", "200"));
    private final DogApiClient dogApiClient = new DogApiClient();

    @BeforeAll
    static void initAllureMetadata() {
        AllureReportManager.initializeRunMetadata();
    }

    @Test
    @Story("Catalogo de racas")
    @Severity(SeverityLevel.CRITICAL)
    @Link(name = "Dog API docs", url = "https://dog.ceo/dog-api/documentation/breed")
    @DisplayName("GET /breeds/list/all deve retornar sucesso e contrato valido")
    @Description("Valida status code, schema, tempo de resposta e payload do endpoint de listagem completa de racas.")
    void shouldReturnAllBreedsSuccessfully() {
        Response response = executeWithSlaRetry(dogApiClient::getAllBreeds);

        validateStatusCode(response, 200);
        validateResponseTime(response);
        validateJsonSchema(response, "schemas/breeds-list-all-schema.json");
        validatePayloadStatus(response, "success");
    }

    @ParameterizedTest(name = "[{index}] breed={0} deve retornar status HTTP {1}")
    @MethodSource("breedProvider")
    @Story("Imagens por raca")
    @Severity(SeverityLevel.NORMAL)
    @Link(name = "Dog API docs", url = "https://dog.ceo/dog-api/documentation/breed")
    @DisplayName("GET /breed/{breed}/images com racas validas e invalidas")
    @Description("Valida comportamento para racas existentes e inexistentes via teste parametrizado.")
    void shouldHandleBreedImagesByBreedValidity(
        String breed,
        int expectedStatusCode,
        String expectedApiStatus,
        String expectedSchema
    ) {
        Response response = executeWithSlaRetry(() -> dogApiClient.getBreedImages(breed));

        validateStatusCode(response, expectedStatusCode);
        validateResponseTime(response);
        validateJsonSchema(response, expectedSchema);
        validatePayloadStatus(response, expectedApiStatus);

        if (expectedStatusCode == 200) {
            validateSuccessfulBreedPayload(response, breed);
        } else {
            validateErrorPayload(response, breed);
        }
    }

    @Test
    @Story("Imagem aleatoria")
    @Severity(SeverityLevel.CRITICAL)
    @Link(name = "Dog API docs", url = "https://dog.ceo/dog-api/documentation/random")
    @DisplayName("GET /breeds/image/random deve retornar uma imagem valida")
    @Description("Valida status code, schema, tempo de resposta e payload do endpoint de imagem aleatoria.")
    void shouldReturnRandomImageSuccessfully() {
        Response response = executeWithSlaRetry(dogApiClient::getRandomImage);

        validateStatusCode(response, 200);
        validateResponseTime(response);
        validateJsonSchema(response, "schemas/random-image-schema.json");
        validatePayloadStatus(response, "success");
        validateRandomImagePayload(response);
    }

    private static Stream<Arguments> breedProvider() {
        return Stream.of(
            Arguments.of("hound", 200, "success", "schemas/breed-images-success-schema.json"),
            Arguments.of("pug", 200, "success", "schemas/breed-images-success-schema.json"),
            Arguments.of("invalidbreedxyz", 404, "error", "schemas/breed-images-error-schema.json")
        );
    }

    private Response executeWithSlaRetry(Supplier<Response> requestCall) {
        Response response = null;

        for (int attempt = 1; attempt <= MAX_SLA_ATTEMPTS; attempt++) {
            response = requestCall.get();
            if (response.time() < ConfigManager.getMaxResponseTimeMs()) {
                return response;
            }

            // Pequeno backoff para reduzir efeito de rajada e jitter da API publica.
            if (attempt < MAX_SLA_ATTEMPTS) {
                try {
                    Thread.sleep(RETRY_BACKOFF_MS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return response;
    }

    private void validateStatusCode(Response response, int expectedStatusCode) {
        Allure.step("Validar status code esperado: " + expectedStatusCode, () ->
            assertThat("Status code invalido", response.getStatusCode(), equalTo(expectedStatusCode))
        );
    }

    private void validateResponseTime(Response response) {
        Allure.step("Validar tempo de resposta menor que SLA", () ->
            assertThat(
                "Tempo de resposta acima do limite",
                response.time(),
                lessThan(ConfigManager.getMaxResponseTimeMs())
            )
        );
    }

    private void validateJsonSchema(Response response, String schemaPath) {
        Allure.step("Validar contrato JSON pelo schema: " + schemaPath, () ->
            response.then().body(matchesJsonSchemaInClasspath(schemaPath))
        );
    }

    private void validatePayloadStatus(Response response, String expectedApiStatus) {
        Allure.step("Validar payload status: " + expectedApiStatus, () ->
            assertThat("Status do payload invalido", response.jsonPath().getString("status"), equalTo(expectedApiStatus))
        );
    }

    private void validateSuccessfulBreedPayload(Response response, String breed) {
        Allure.step("Validar payload de sucesso para raca: " + breed, () -> {
            assertThat(
                "Lista de imagens vazia para raca valida",
                response.jsonPath().getList("message").isEmpty(),
                equalTo(false)
            );
            String firstImage = response.jsonPath().getList("message", String.class).get(0);
            assertThat("Primeira imagem deve conter o nome da raca", firstImage, containsString(breed));
        });
    }

    private void validateErrorPayload(Response response, String breed) {
        Allure.step("Validar payload de erro para raca invalida: " + breed, () ->
            assertThat("Mensagem de erro deve existir", response.jsonPath().getString("message"), not(emptyOrNullString()))
        );
    }

    private void validateRandomImagePayload(Response response) {
        Allure.step("Validar payload de imagem random", () -> {
            String randomImageUrl = response.jsonPath().getString("message");
            assertThat("URL da imagem deve existir", randomImageUrl, not(emptyOrNullString()));
            assertThat("URL da imagem random nao possui formato esperado", randomImageUrl, containsString("images.dog.ceo"));
        });
    }
}
