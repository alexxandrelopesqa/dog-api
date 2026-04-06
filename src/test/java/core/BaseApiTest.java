package core;

import client.DogApiClient;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseApiTest {

    protected static DogApiClient dogApiClient;

    @BeforeAll
    static void initializeSuite() {
        AllureReportManager.initializeMetadata();
        dogApiClient = new DogApiClient();
    }

    @BeforeEach
    void beforeEachScenario() {
        Allure.parameter("baseUrl", ConfigManager.baseUrl());
        Allure.parameter("retryAttempts", String.valueOf(ConfigManager.retryAttempts()));
        Allure.parameter("retryBackoffMs", String.valueOf(ConfigManager.retryBackoffMs()));
        Allure.parameter("maxResponseTimeMs", String.valueOf(ConfigManager.maxResponseTimeMs()));
    }
}
