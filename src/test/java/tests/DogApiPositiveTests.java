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
import io.restassured.response.Response;
import models.BreedImagesResponse;
import models.BreedListResponse;
import models.RandomImageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("QA API Challenge")
@Feature("Dog API - Cenarios positivos")
@Owner("qa-automation")
public class DogApiPositiveTests extends BaseApiTest {

    @Test
    @Tag("smoke")
    @Tag("regression")
    @Story("Listar todas as racas")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida contrato e estrutura do endpoint GET /breeds/list/all.")
    @TmsLink("QA-API-001")
    @DisplayName("Deve retornar mapa de racas e sub-racas com status success")
    void shouldReturnBreedsListWithExpectedStructure() {
        String endpoint = "/breeds/list/all";
        AllureReportManager.attachExecutionContext("positive-list-all-breeds", endpoint);

        Response response = RetryExecutor.executeWithTransientRetry(endpoint, dogApiClient::getAllBreeds);
        BreedListResponse body = response.as(BreedListResponse.class);

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breeds-list-all-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertBreedsListStructure(response);
        org.junit.jupiter.api.Assertions.assertNotNull(body.getMessage(), "Desserializacao de message nao deve ser nula");
    }

    @Test
    @Tag("regression")
    @Story("Buscar imagens por raca valida")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida retorno de imagens para raca existente em GET /breed/{breed}/images.")
    @TmsLink("QA-API-002")
    @DisplayName("Deve retornar lista de URLs validas para raca existente")
    void shouldReturnImagesForExistingBreed() {
        String breed = TestDataLoader.validBreed();
        String endpoint = "/breed/" + breed + "/images";
        AllureReportManager.attachExecutionContext("positive-breed-images", endpoint);

        Response response = RetryExecutor.executeWithTransientRetry(endpoint, () -> dogApiClient.getBreedImages(breed));
        BreedImagesResponse body = response.as(BreedImagesResponse.class);

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-images-success-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertBreedImagesStructure(response, breed);
        org.junit.jupiter.api.Assertions.assertFalse(body.getMessage().isEmpty(), "Desserializacao deve conter imagens");
    }

    @Test
    @Tag("smoke")
    @Tag("regression")
    @Story("Buscar imagem aleatoria")
    @Severity(SeverityLevel.NORMAL)
    @Description("Valida estrutura de URL retornada pelo endpoint GET /breeds/image/random.")
    @TmsLink("QA-API-003")
    @DisplayName("Deve retornar uma URL valida ao solicitar imagem randomica")
    void shouldReturnValidRandomImage() {
        String endpoint = "/breeds/image/random";
        AllureReportManager.attachExecutionContext("positive-random-image", endpoint);

        Response response = RetryExecutor.executeWithTransientRetry(endpoint, dogApiClient::getRandomImage);
        RandomImageResponse body = response.as(RandomImageResponse.class);

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/random-image-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertRandomImageStructure(response);
        org.junit.jupiter.api.Assertions.assertNotNull(body.getMessage(), "Desserializacao de URL random nao deve ser nula");
    }
}
