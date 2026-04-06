package client;

import core.AllureReportManager;
import core.RequestSpecFactory;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class DogApiClient {

    private final RequestSpecification specification = RequestSpecFactory.defaultSpec();

    public DogApiClient() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    public Response getAllBreeds() {
        String endpoint = "/breeds/list/all";
        Response response = RestAssured.given(specification)
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
        AllureReportManager.attachRequestResponse("GET", endpoint, response);
        return response;
    }

    public Response getBreedImages(String breed) {
        String endpoint = "/breed/" + breed + "/images";
        Response response = RestAssured.given(specification)
            .pathParam("breed", breed)
            .when()
            .get("/breed/{breed}/images")
            .then()
            .extract()
            .response();
        AllureReportManager.attachRequestResponse("GET", endpoint, response);
        return response;
    }

    public Response getRandomImage() {
        String endpoint = "/breeds/image/random";
        Response response = RestAssured.given(specification)
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
        AllureReportManager.attachRequestResponse("GET", endpoint, response);
        return response;
    }
}
