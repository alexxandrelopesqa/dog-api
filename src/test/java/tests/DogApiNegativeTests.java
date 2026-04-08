package tests;

import core.AllureReportManager;
import core.ApiAssertions;
import core.BaseApiTest;
import core.RetryExecutor;
import core.TestDataLoader;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.Map;
import models.ApiBaseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Dog API")
@Feature("404 / payload")
@Owner("alexxandrelopesqa")
public class DogApiNegativeTests extends BaseApiTest {

    @Test
    @Tag("regression")
    @Story("Raça inválida em /breed/{breed}/images")
    @Severity(SeverityLevel.CRITICAL)
    @Description("404, status error e mensagem sobre breed.")
    @TmsLink("breed-list")
    @DisplayName("Retorna status error para raça inválida")
    void shouldReturnErrorForInvalidBreed() {
        String invalidBreed = TestDataLoader.invalidBreed();
        String endpoint = "/breed/" + invalidBreed + "/images";
        AllureReportManager.attachExecutionContext("negative-invalid-breed", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getBreedImages(invalidBreed));
        ApiBaseResponse body = response.as(ApiBaseResponse.class);

        ApiAssertions.assertHttpAndContentType(response, 404);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-images-error-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "error");
        ApiAssertions.assertErrorMessage(response, "breed");
        org.junit.jupiter.api.Assertions.assertNotNull(body.getMessage(), "Desserializacao de erro nao deve ser nula");
    }

    @Test
    @Tag("regression")
    @Story("Campos do JSON em /breeds/image/random")
    @Severity(SeverityLevel.NORMAL)
    @Description("status e message presentes e string.")
    @TmsLink("random")
    @DisplayName("Tipos básicos no payload do random")
    void shouldMatchRandomPayloadShape() {
        String endpoint = "/breeds/image/random";
        AllureReportManager.attachExecutionContext("random-payload-shape", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, dogApiClient::getRandomImage);
        JsonPath jsonPath = response.jsonPath();
        Map<String, Object> payload = jsonPath.getMap("$");

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertMandatoryKeys(response, "success");

        org.junit.jupiter.api.Assertions.assertNotNull(payload, "Payload nao deve ser nulo");
        org.junit.jupiter.api.Assertions.assertTrue(payload.containsKey("status"), "Campo status deve existir");
        org.junit.jupiter.api.Assertions.assertTrue(payload.containsKey("message"), "Campo message deve existir");
        org.junit.jupiter.api.Assertions.assertInstanceOf(String.class, payload.get("status"), "Campo status deve ser string");
        org.junit.jupiter.api.Assertions.assertInstanceOf(String.class, payload.get("message"), "Campo message deve ser string");
    }

    @Test
    @Tag("regression")
    @Story("404 GET /breed/{breed}/list")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Lista de sub-racas 404 para raca invalida")
    void shouldReturn404ForInvalidBreedList() {
        String invalidBreed = TestDataLoader.invalidBreed();
        String endpoint = "/breed/" + invalidBreed + "/list";
        AllureReportManager.attachExecutionContext("neg-breed-list", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getSubBreedList(invalidBreed));

        ApiAssertions.assertHttpAndContentType(response, 404);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-images-error-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "error");
    }

    @Test
    @Tag("regression")
    @Story("404 random raca invalida")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Random por raca 404 quando raca nao existe")
    void shouldReturn404ForInvalidBreedRandom() {
        String invalidBreed = TestDataLoader.invalidBreed();
        String endpoint = "/breed/" + invalidBreed + "/images/random";
        AllureReportManager.attachExecutionContext("neg-breed-random", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getBreedRandomImage(invalidBreed));

        ApiAssertions.assertHttpAndContentType(response, 404);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-images-error-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "error");
    }

    @Test
    @Tag("regression")
    @Story("404 random N raca invalida")
    @DisplayName("Random N por raca 404 quando raca nao existe")
    void shouldReturn404ForInvalidBreedRandomN() {
        String invalidBreed = TestDataLoader.invalidBreed();
        String endpoint = "/breed/" + invalidBreed + "/images/random/3";
        AllureReportManager.attachExecutionContext("neg-breed-random-n", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getBreedRandomImages(invalidBreed, 3));

        ApiAssertions.assertHttpAndContentType(response, 404);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-images-error-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "error");
    }

    @Test
    @Tag("regression")
    @Story("404 sub-raca invalida /images")
    @DisplayName("Imagens 404 para sub-raca inexistente")
    void shouldReturn404ForInvalidSubBreedImages() {
        String breed = TestDataLoader.validBreed();
        String badSub = TestDataLoader.invalidSubBreed();
        String endpoint = "/breed/" + breed + "/" + badSub + "/images";
        AllureReportManager.attachExecutionContext("neg-sub-images", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getSubBreedImages(breed, badSub));

        ApiAssertions.assertHttpAndContentType(response, 404);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-images-error-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "error");
    }

    @Test
    @Tag("regression")
    @Story("404 sub-raca invalida /random")
    @DisplayName("Random sub 404 quando sub nao existe")
    void shouldReturn404ForInvalidSubBreedRandom() {
        String breed = TestDataLoader.validBreed();
        String badSub = TestDataLoader.invalidSubBreed();
        String endpoint = "/breed/" + breed + "/" + badSub + "/images/random";
        AllureReportManager.attachExecutionContext("neg-sub-random", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getSubBreedRandomImage(breed, badSub));

        ApiAssertions.assertHttpAndContentType(response, 404);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-images-error-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "error");
    }

    @Test
    @Tag("regression")
    @Story("404 sub-raca invalida /random/N")
    @DisplayName("Random N sub 404 quando sub nao existe")
    void shouldReturn404ForInvalidSubBreedRandomN() {
        String breed = TestDataLoader.validBreed();
        String badSub = TestDataLoader.invalidSubBreed();
        String endpoint = "/breed/" + breed + "/" + badSub + "/images/random/2";
        AllureReportManager.attachExecutionContext("neg-sub-random-n", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getSubBreedRandomImages(breed, badSub, 2));

        ApiAssertions.assertHttpAndContentType(response, 404);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-images-error-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "error");
    }
}
