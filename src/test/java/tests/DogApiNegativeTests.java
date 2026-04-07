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

@Epic("Catálogo de Raças e Imagens")
@Feature("Fluxos de robustez e erro da Dog API")
@Owner("alexxandrelopesqa")
public class DogApiNegativeTests extends BaseApiTest {

    @Test
    @Tag("regression")
    @Story("Consulta com raça inexistente")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida resposta de erro para raça inexistente em /breed/{breed}/images.")
    @TmsLink("breed-list")
    @DisplayName("Retorna status error para raça inválida")
    void shouldReturnErrorForInvalidBreed() {
        String invalidBreed = TestDataLoader.invalidBreed();
        String endpoint = "/breed/" + invalidBreed + "/images";
        AllureReportManager.attachExecutionContext("negative-invalid-breed", endpoint);

        Response response = RetryExecutor.executeWithTransientRetry(endpoint, () -> dogApiClient.getBreedImages(invalidBreed));
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
    @Story("Validação defensiva do payload")
    @Severity(SeverityLevel.NORMAL)
    @Description("Executa validações defensivas para garantir formato mínimo e tipos esperados.")
    @TmsLink("random")
    @DisplayName("Valida estrutura defensiva com campos obrigatórios e tipos")
    void shouldValidatePayloadDefensively() {
        String endpoint = "/breeds/image/random";
        AllureReportManager.attachExecutionContext("defensive-payload-validation", endpoint);

        Response response = RetryExecutor.executeWithTransientRetry(endpoint, dogApiClient::getRandomImage);
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
}
