package utils;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;

public final class RequestSpecFactory {

    private RequestSpecFactory() {
    }

    public static RequestSpecification defaultSpec() {
        return new RequestSpecBuilder()
            .setBaseUri(ConfigManager.getBaseUrl())
            .setAccept("application/json")
            .setContentType("application/json")
            .log(LogDetail.URI)
            .addFilter(new AllureRestAssured())
            .build();
    }
}
