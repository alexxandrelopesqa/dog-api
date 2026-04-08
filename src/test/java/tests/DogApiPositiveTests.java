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
import io.restassured.response.Response;
import models.BreedImagesResponse;
import models.BreedListResponse;
import models.RandomImageResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Dog API")
@Feature("Happy path")
@Owner("alexxandrelopesqa")
public class DogApiPositiveTests extends BaseApiTest {

    @Test
    @Tag("smoke")
    @Tag("regression")
    @Story("/breeds/list/all")
    @DisplayName("Lista completa de raças")
    void shouldReturnBreedsListWithExpectedStructure() {
        String endpoint = "/breeds/list/all";
        AllureReportManager.attachExecutionContext("positive-list-all-breeds", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, dogApiClient::getAllBreeds);
        BreedListResponse body = response.as(BreedListResponse.class);

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breeds-list-all-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertBreedsListStructure(response);
        Assertions.assertNotNull(body.getMessage(), "campo message nulo");
    }

    @Test
    @Tag("regression")
    @Story("/breed/{breed}/images")
    @DisplayName("Imagens de uma raça válida")
    void shouldReturnImagesForExistingBreed() {
        String breed = TestDataLoader.validBreed();
        String endpoint = "/breed/" + breed + "/images";
        AllureReportManager.attachExecutionContext("positive-breed-images", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getBreedImages(breed));
        BreedImagesResponse body = response.as(BreedImagesResponse.class);

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-images-success-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertBreedImagesStructure(response, breed);
        Assertions.assertFalse(body.getMessage().isEmpty(), "lista de imagens vazia");
    }

    @Test
    @Tag("smoke")
    @Tag("regression")
    @Story("/breeds/image/random")
    @DisplayName("Uma imagem aleatória")
    void shouldReturnValidRandomImage() {
        String endpoint = "/breeds/image/random";
        AllureReportManager.attachExecutionContext("positive-random-image", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, dogApiClient::getRandomImage);
        RandomImageResponse body = response.as(RandomImageResponse.class);

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/random-image-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertRandomImageStructure(response);
        Assertions.assertNotNull(body.getMessage(), "URL nula");
    }
}
