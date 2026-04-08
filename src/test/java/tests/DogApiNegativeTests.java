package tests;

import core.AllureReportManager;
import core.ApiAssertions;
import core.BaseApiTest;
import core.RetryExecutor;
import core.TestDataLoader;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.Map;
import models.ApiBaseResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Dog API")
@Feature("Erros 404")
@Owner("alexxandrelopesqa")
public class DogApiNegativeTests extends BaseApiTest {

    @Test
    @Tag("regression")
    @Story("/breed/{breed}/images inválido")
    @DisplayName("404 para raça inexistente (images)")
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
        Assertions.assertNotNull(body.getMessage(), "mensagem de erro nula");
    }

    @Test
    @Tag("regression")
    @Story("/breeds/image/random — shape JSON")
    @DisplayName("Random: status e message string")
    void shouldMatchRandomPayloadShape() {
        String endpoint = "/breeds/image/random";
        AllureReportManager.attachExecutionContext("random-payload-shape", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, dogApiClient::getRandomImage);
        JsonPath jsonPath = response.jsonPath();
        Map<String, Object> payload = jsonPath.getMap("$");

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertMandatoryKeys(response, "success");

        Assertions.assertNotNull(payload, "payload nulo");
        Assertions.assertTrue(payload.containsKey("status"));
        Assertions.assertTrue(payload.containsKey("message"));
        Assertions.assertInstanceOf(String.class, payload.get("status"));
        Assertions.assertInstanceOf(String.class, payload.get("message"));
    }

    @Test
    @Tag("regression")
    @Story("/breed/{breed}/list inválido")
    @DisplayName("404 na lista de sub-raças com raça inválida")
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
    @Story("/breed/{breed}/images/random inválido")
    @DisplayName("404 no random por raça inexistente")
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
    @Story("/breed/{breed}/images/random/N inválido")
    @DisplayName("404 no random N com raça inexistente")
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
    @Story("sub-raça inválida /images")
    @DisplayName("404 em imagens com sub-raça inexistente")
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
    @Story("sub-raça inválida /random")
    @DisplayName("404 no random de sub-raça inexistente")
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
    @Story("sub-raça inválida /random/N")
    @DisplayName("404 no random N com sub-raça inexistente")
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
