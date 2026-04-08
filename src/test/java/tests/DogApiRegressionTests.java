package tests;

import core.AllureReportManager;
import core.ApiAssertions;
import core.BaseApiTest;
import core.RetryExecutor;
import core.TestDataLoader;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Dog API")
@Feature("Outros GETs")
@Owner("alexxandrelopesqa")
public class DogApiRegressionTests extends BaseApiTest {

    @Test
    @Tag("regression")
    @Story("GET /breeds/image/random/{n}")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Random global com N=3 retorna 3 URLs")
    void randomGlobal_returnsNImages() {
        int n = 3;
        String endpoint = "/breeds/image/random/" + n;
        AllureReportManager.attachExecutionContext("rnd-global-n", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getRandomImages(n));

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/message-url-array-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertMessageUrlArray(response, n, n);
    }

    @Test
    @Tag("regression")
    @Story("GET /breeds/image/random/51")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Random global N=51 nao passa de 50 itens")
    void randomGlobal_fiftyOneCappedAtFifty() {
        int requested = 51;
        String endpoint = "/breeds/image/random/" + requested;
        AllureReportManager.attachExecutionContext("rnd-global-cap", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getRandomImages(requested));

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/message-url-array-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertMessageUrlArray(response, 1, 50);
    }

    @Test
    @Tag("regression")
    @Tag("smoke")
    @Story("GET /breed/{breed}/images/random")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Random de uma raca retorna uma URL")
    void breedRandom_returnsSingleUrl() {
        String breed = TestDataLoader.validBreed();
        String endpoint = "/breed/" + breed + "/images/random";
        AllureReportManager.attachExecutionContext("brd-random-1", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getBreedRandomImage(breed));

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/random-image-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertRandomImageStructure(response);
    }

    @Test
    @Tag("regression")
    @Story("GET /breed/{breed}/images/random/{n}")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Random de raca com N=5")
    void breedRandom_returnsNImages() {
        String breed = TestDataLoader.validBreed();
        int n = 5;
        String endpoint = "/breed/" + breed + "/images/random/" + n;
        AllureReportManager.attachExecutionContext("brd-random-n", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getBreedRandomImages(breed, n));

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/message-url-array-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertMessageUrlArray(response, n, n);
    }

    @Test
    @Tag("regression")
    @Tag("smoke")
    @Story("GET /breed/{breed}/list")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Lista de sub-racas do hound inclui afghan")
    void subBreedList_houndHasExpectedSub() {
        String breed = TestDataLoader.validBreed();
        String endpoint = "/breed/" + breed + "/list";
        AllureReportManager.attachExecutionContext("sub-list-hound", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getSubBreedList(breed));

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-sub-list-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertSubBreedListContains(response, TestDataLoader.validSubBreed());
    }

    @Test
    @Tag("regression")
    @Story("GET /breed/beagle/list")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Beagle: list retorna array vazio")
    void subBreedList_emptyForBreedWithoutSubs() {
        String breed = TestDataLoader.breedWithoutSubBreeds();
        String endpoint = "/breed/" + breed + "/list";
        AllureReportManager.attachExecutionContext("sub-list-empty", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getSubBreedList(breed));

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-sub-list-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        List<?> subs = response.jsonPath().getList("message");
        Assertions.assertNotNull(subs);
        Assertions.assertTrue(subs.isEmpty(), "message vazio");
    }

    @Test
    @Tag("regression")
    @Story("GET /breed/hound/afghan/images")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Todas as imagens da sub-raca afghan")
    void subBreedImages_returnsList() {
        String breed = TestDataLoader.validBreed();
        String sub = TestDataLoader.validSubBreed();
        String endpoint = "/breed/" + breed + "/" + sub + "/images";
        AllureReportManager.attachExecutionContext("sub-images", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getSubBreedImages(breed, sub));

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/breed-images-success-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertSubBreedImageUrls(response, breed, sub);
    }

    @Test
    @Tag("regression")
    @Story("GET /breed/hound/afghan/images/random")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Random single da sub-raca afghan")
    void subBreedRandom_returnsOneUrl() {
        String breed = TestDataLoader.validBreed();
        String sub = TestDataLoader.validSubBreed();
        String endpoint = "/breed/" + breed + "/" + sub + "/images/random";
        AllureReportManager.attachExecutionContext("sub-random-1", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getSubBreedRandomImage(breed, sub));

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/random-image-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertRandomImageStructure(response);
    }

    @Test
    @Tag("regression")
    @Story("GET /breed/hound/afghan/images/random/2")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Random N=2 da sub-raca afghan")
    void subBreedRandom_returnsNUrls() {
        String breed = TestDataLoader.validBreed();
        String sub = TestDataLoader.validSubBreed();
        int n = 2;
        String endpoint = "/breed/" + breed + "/" + sub + "/images/random/" + n;
        AllureReportManager.attachExecutionContext("sub-random-n", endpoint);

        Response response = RetryExecutor.executeWithRetry(endpoint, () -> dogApiClient.getSubBreedRandomImages(breed, sub, n));

        ApiAssertions.assertHttpAndContentType(response, 200);
        ApiAssertions.assertResponseTime(response);
        ApiAssertions.assertSchema(response, "schemas/message-url-array-schema.json");
        ApiAssertions.assertMandatoryKeys(response, "success");
        ApiAssertions.assertMessageUrlArray(response, n, n);
    }
}
