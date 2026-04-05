package utils;

import io.qameta.allure.Allure;
import io.restassured.response.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Properties;

public final class AllureReportManager {

    private static final String DEFAULT_RESULTS_DIR = "target/allure-results";

    private AllureReportManager() {
    }

    public static void initializeRunMetadata() {
        Path resultsDir = getResultsDirectory();
        createDirectoryIfNeeded(resultsDir);
        writeEnvironmentFile(resultsDir);
        writeExecutorFile(resultsDir);
        writeCategoriesFile(resultsDir);
    }

    public static void attachApiCall(String method, String endpoint, Response response) {
        String requestSummary = "method=" + method + System.lineSeparator()
            + "endpoint=" + endpoint + System.lineSeparator()
            + "baseUrl=" + ConfigManager.getBaseUrl();

        String responseSummary = "statusCode=" + response.getStatusCode() + System.lineSeparator()
            + "responseTimeMs=" + response.time() + System.lineSeparator()
            + "headers=" + response.getHeaders();

        Allure.addAttachment("Request Summary", "text/plain", requestSummary, ".txt");
        Allure.addAttachment("Response Summary", "text/plain", responseSummary, ".txt");
        Allure.addAttachment("Response Body", "application/json", response.asPrettyString(), ".json");
    }

    private static Path getResultsDirectory() {
        return Paths.get(System.getProperty("allure.results.directory", DEFAULT_RESULTS_DIR));
    }

    private static void createDirectoryIfNeeded(Path resultsDir) {
        try {
            Files.createDirectories(resultsDir);
        } catch (IOException ioException) {
            throw new IllegalStateException("Nao foi possivel criar diretorio de resultados do Allure", ioException);
        }
    }

    private static void writeEnvironmentFile(Path resultsDir) {
        Properties properties = new Properties();
        properties.setProperty("baseUrl", ConfigManager.getBaseUrl());
        properties.setProperty("maxResponseTimeMs", String.valueOf(ConfigManager.getMaxResponseTimeMs()));
        properties.setProperty("javaVersion", System.getProperty("java.version", "unknown"));
        properties.setProperty("osName", System.getProperty("os.name", "unknown"));
        properties.setProperty("osVersion", System.getProperty("os.version", "unknown"));

        Path file = resultsDir.resolve("environment.properties");
        try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            properties.store(writer, "Allure execution environment");
        } catch (IOException ioException) {
            throw new IllegalStateException("Nao foi possivel escrever environment.properties", ioException);
        }
    }

    private static void writeExecutorFile(Path resultsDir) {
        String runId = getEnvOrDefault("GITHUB_RUN_ID", "local-run");
        String serverUrl = getEnvOrDefault("GITHUB_SERVER_URL", "https://github.com");
        String repository = getEnvOrDefault("GITHUB_REPOSITORY", "local/dog-api-automation");
        String runUrl = serverUrl + "/" + repository + "/actions/runs/" + runId;

        String executorJson = "{\n"
            + "  \"name\": \"GitHub Actions\",\n"
            + "  \"type\": \"github\",\n"
            + "  \"url\": \"" + escapeJson(runUrl) + "\",\n"
            + "  \"buildOrder\": \"" + escapeJson(runId) + "\",\n"
            + "  \"buildName\": \"Dog API Automation\",\n"
            + "  \"buildUrl\": \"" + escapeJson(runUrl) + "\",\n"
            + "  \"reportName\": \"Dog API Allure Report\",\n"
            + "  \"timestamp\": \"" + escapeJson(Instant.now().toString()) + "\"\n"
            + "}";

        writeUtf8File(resultsDir.resolve("executor.json"), executorJson, "executor.json");
    }

    private static void writeCategoriesFile(Path resultsDir) {
        String categoriesJson = "[\n"
            + "  {\n"
            + "    \"name\": \"SLA breach\",\n"
            + "    \"matchedStatuses\": [\"failed\"],\n"
            + "    \"messageRegex\": \".*Tempo de resposta acima do limite.*\"\n"
            + "  },\n"
            + "  {\n"
            + "    \"name\": \"Contrato JSON invalido\",\n"
            + "    \"matchedStatuses\": [\"failed\"],\n"
            + "    \"messageRegex\": \".*schema.*|.*JSON.*\"\n"
            + "  },\n"
            + "  {\n"
            + "    \"name\": \"Falha de assercao funcional\",\n"
            + "    \"matchedStatuses\": [\"failed\"],\n"
            + "    \"messageRegex\": \".*Expected.*but:.*\"\n"
            + "  }\n"
            + "]";

        writeUtf8File(resultsDir.resolve("categories.json"), categoriesJson, "categories.json");
    }

    private static void writeUtf8File(Path targetFile, String content, String name) {
        try {
            Files.writeString(targetFile, content, StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            throw new IllegalStateException("Nao foi possivel escrever " + name, ioException);
        }
    }

    private static String getEnvOrDefault(String envName, String defaultValue) {
        String value = System.getenv(envName);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
