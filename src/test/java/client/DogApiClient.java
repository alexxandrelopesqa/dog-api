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

    public Response getRandomImages(int count) {
        String endpoint = "/breeds/image/random/" + count;
        Response response = RestAssured.given(specification)
            .pathParam("count", count)
            .when()
            .get("/breeds/image/random/{count}")
            .then()
            .extract()
            .response();
        AllureReportManager.attachRequestResponse("GET", endpoint, response);
        return response;
    }

    public Response getBreedRandomImage(String breed) {
        String endpoint = "/breed/" + breed + "/images/random";
        Response response = RestAssured.given(specification)
            .pathParam("breed", breed)
            .when()
            .get("/breed/{breed}/images/random")
            .then()
            .extract()
            .response();
        AllureReportManager.attachRequestResponse("GET", endpoint, response);
        return response;
    }

    public Response getBreedRandomImages(String breed, int count) {
        String endpoint = "/breed/" + breed + "/images/random/" + count;
        Response response = RestAssured.given(specification)
            .pathParam("breed", breed)
            .pathParam("count", count)
            .when()
            .get("/breed/{breed}/images/random/{count}")
            .then()
            .extract()
            .response();
        AllureReportManager.attachRequestResponse("GET", endpoint, response);
        return response;
    }

    public Response getSubBreedList(String breed) {
        String endpoint = "/breed/" + breed + "/list";
        Response response = RestAssured.given(specification)
            .pathParam("breed", breed)
            .when()
            .get("/breed/{breed}/list")
            .then()
            .extract()
            .response();
        AllureReportManager.attachRequestResponse("GET", endpoint, response);
        return response;
    }

    public Response getSubBreedImages(String breed, String subBreed) {
        String endpoint = "/breed/" + breed + "/" + subBreed + "/images";
        Response response = RestAssured.given(specification)
            .pathParam("breed", breed)
            .pathParam("subBreed", subBreed)
            .when()
            .get("/breed/{breed}/{subBreed}/images")
            .then()
            .extract()
            .response();
        AllureReportManager.attachRequestResponse("GET", endpoint, response);
        return response;
    }

    public Response getSubBreedRandomImage(String breed, String subBreed) {
        String endpoint = "/breed/" + breed + "/" + subBreed + "/images/random";
        Response response = RestAssured.given(specification)
            .pathParam("breed", breed)
            .pathParam("subBreed", subBreed)
            .when()
            .get("/breed/{breed}/{subBreed}/images/random")
            .then()
            .extract()
            .response();
        AllureReportManager.attachRequestResponse("GET", endpoint, response);
        return response;
    }

    public Response getSubBreedRandomImages(String breed, String subBreed, int count) {
        String endpoint = "/breed/" + breed + "/" + subBreed + "/images/random/" + count;
        Response response = RestAssured.given(specification)
            .pathParam("breed", breed)
            .pathParam("subBreed", subBreed)
            .pathParam("count", count)
            .when()
            .get("/breed/{breed}/{subBreed}/images/random/{count}")
            .then()
            .extract()
            .response();
        AllureReportManager.attachRequestResponse("GET", endpoint, response);
        return response;
    }
}
