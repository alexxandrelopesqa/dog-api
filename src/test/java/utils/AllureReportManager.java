package utils;

import io.qameta.allure.Allure;
import io.restassured.response.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
            + "statusLine=" + response.getStatusLine() + System.lineSeparator()
            + "responseTimeMs=" + response.time() + System.lineSeparator()
            + "contentType=" + response.getContentType() + System.lineSeparator()
            + "headers=" + response.getHeaders();

        Allure.addAttachment("Request Summary", "text/plain", requestSummary, ".txt");
        Allure.addAttachment("Response Summary", "text/plain", responseSummary, ".txt");
        Allure.addAttachment("Response Body", "application/json", response.asPrettyString(), ".json");
    }

    public static void attachRetryTimeline(String endpoint, List<Long> attemptsMs, long thresholdMs) {
        StringBuilder timeline = new StringBuilder();
        timeline.append("{").append(System.lineSeparator())
            .append("  \"endpoint\": \"").append(endpoint).append("\",").append(System.lineSeparator())
            .append("  \"thresholdMs\": ").append(thresholdMs).append(",").append(System.lineSeparator())
            .append("  \"attempts\": [").append(System.lineSeparator());

        for (int i = 0; i < attemptsMs.size(); i++) {
            long duration = attemptsMs.get(i);
            timeline.append("    {\"attempt\": ").append(i + 1)
                .append(", \"durationMs\": ").append(duration)
                .append(", \"withinThreshold\": ").append(duration < thresholdMs).append("}");
            if (i < attemptsMs.size() - 1) {
                timeline.append(",");
            }
            timeline.append(System.lineSeparator());
        }

        timeline.append("  ]").append(System.lineSeparator()).append("}");
        Allure.addAttachment("SLA Retry Timeline", "application/json", timeline.toString(), ".json");
    }

    public static void attachAssertionContext(String assertionName, Object expected, Object actual) {
        String context = "{"
            + "\"assertion\":\"" + escapeJson(assertionName) + "\","
            + "\"expected\":\"" + escapeJson(String.valueOf(expected)) + "\","
            + "\"actual\":\"" + escapeJson(String.valueOf(actual)) + "\""
            + "}";
        Allure.addAttachment("Assertion Context", "application/json", context, ".json");
    }

    public static void attachExecutionContext(Map<String, String> values) {
        StringBuilder builder = new StringBuilder();
        builder.append("{").append(System.lineSeparator());
        int index = 0;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            builder.append("  \"").append(escapeJson(entry.getKey())).append("\": \"")
                .append(escapeJson(entry.getValue())).append("\"");
            if (index < values.size() - 1) {
                builder.append(",");
            }
            builder.append(System.lineSeparator());
            index++;
        }
        builder.append("}");
        Allure.addAttachment("Execution Context", "application/json", builder.toString(), ".json");
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
