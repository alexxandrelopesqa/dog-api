package clients;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import utils.AllureReportManager;
import utils.RequestSpecFactory;

public class DogApiClient {

    private final RequestSpecification requestSpecification;

    public DogApiClient() {
        this.requestSpecification = RequestSpecFactory.defaultSpec();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    public Response getAllBreeds() {
        Response response = RestAssured.given(requestSpecification)
            .when()
            .get("/breeds/list/all")
            .then()
            .extract()
            .response();
        AllureReportManager.attachApiCall("GET", "/breeds/list/all", response);
        return response;
    }

    public Response getBreedImages(String breed) {
        Response response = RestAssured.given(requestSpecification)
            .pathParam("breed", breed)
            .when()
            .get("/breed/{breed}/images")
            .then()
            .extract()
            .response();
        AllureReportManager.attachApiCall("GET", "/breed/" + breed + "/images", response);
        return response;
    }

    public Response getRandomImage() {
        Response response = RestAssured.given(requestSpecification)
            .when()
            .get("/breeds/image/random")
            .then()
            .extract()
            .response();
        AllureReportManager.attachApiCall("GET", "/breeds/image/random", response);
        return response;
    }
}
