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

@Epic("Catálogo de Raças e Imagens")
@Feature("Fluxos positivos da Dog API")
@Owner("alexxandrelopesqa")
public class DogApiPositiveTests extends BaseApiTest {

    @Test
    @Tag("smoke")
    @Tag("regression")
    @Story("Consulta de catálogo de raças")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Confere status HTTP, contrato JSON e estrutura do payload de raças/sub-raças.")
    @TmsLink("breed")
    @DisplayName("Retorna mapa de raças com status success")
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
    @Story("Consulta de imagens por raça existente")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida retorno de lista de imagens para uma raça existente.")
    @TmsLink("breed-list")
    @DisplayName("Retorna lista de URLs válidas para raça existente")
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
    @Story("Exibição de imagem aleatória")
    @Severity(SeverityLevel.NORMAL)
    @Description("Confere se o endpoint random entrega uma URL de imagem válida.")
    @TmsLink("random")
    @DisplayName("Retorna URL válida ao solicitar imagem randômica")
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
