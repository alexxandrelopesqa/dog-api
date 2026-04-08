package core;

import io.qameta.allure.Allure;
import io.restassured.response.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class AllureReportManager {

    private static final String RESULTS_DIR = "target/allure-results";

    private AllureReportManager() {
    }

    public static void initializeMetadata() {
        Path outputDir = Paths.get(System.getProperty("allure.results.directory", RESULTS_DIR));
        try {
            Files.createDirectories(outputDir);
            writeEnvironment(outputDir);
            writeExecutor(outputDir);
            copyCategories(outputDir);
        } catch (IOException exception) {
            throw new IllegalStateException("não consegui escrever em allure-results", exception);
        }
    }

    public static void attachRequestResponse(String method, String endpoint, Response response) {
        String requestAttachment = "method=" + method + System.lineSeparator()
            + "endpoint=" + endpoint + System.lineSeparator()
            + "baseUrl=" + ConfigManager.baseUrl();
        String responseAttachment = "statusCode=" + response.getStatusCode() + System.lineSeparator()
            + "statusLine=" + response.getStatusLine() + System.lineSeparator()
            + "contentType=" + response.getContentType() + System.lineSeparator()
            + "responseTimeMs=" + response.time() + System.lineSeparator()
            + "headers=" + response.getHeaders();

        Allure.addAttachment("Request Summary", "text/plain", requestAttachment, ".txt");
        Allure.addAttachment("Response Summary", "text/plain", responseAttachment, ".txt");
        Allure.addAttachment("Response Body", "application/json", response.asPrettyString(), ".json");
    }

    public static void attachTransientRetry(String endpoint, int attempt, String reason) {
        String payload = "{"
            + "\"endpoint\":\"" + endpoint + "\","
            + "\"attempt\":" + attempt + ","
            + "\"reason\":\"" + escape(reason) + "\""
            + "}";
        Allure.addAttachment("Retry Attempt", "application/json", payload, ".json");
    }

    public static void attachAssertionContext(String field, Object expected, Object actual) {
        String payload = "{"
            + "\"field\":\"" + escape(field) + "\","
            + "\"expected\":\"" + escape(String.valueOf(expected)) + "\","
            + "\"actual\":\"" + escape(String.valueOf(actual)) + "\""
            + "}";
        Allure.addAttachment("Assertion Context", "application/json", payload, ".json");
    }

    public static void attachExecutionContext(String scenario, String endpoint) {
        Map<String, String> context = new LinkedHashMap<>();
        context.put("scenario", scenario);
        context.put("endpoint", endpoint);
        context.put("baseUrl", ConfigManager.baseUrl());
        context.put("maxResponseTimeMs", String.valueOf(ConfigManager.maxResponseTimeMs()));
        context.put("retryAttempts", String.valueOf(ConfigManager.retryAttempts()));
        context.put("retryBackoffMs", String.valueOf(ConfigManager.retryBackoffMs()));

        StringBuilder builder = new StringBuilder();
        builder.append("{").append(System.lineSeparator());
        int index = 0;
        for (Map.Entry<String, String> item : context.entrySet()) {
            builder.append("  \"").append(escape(item.getKey())).append("\": \"")
                .append(escape(item.getValue())).append("\"");
            if (index < context.size() - 1) {
                builder.append(",");
            }
            builder.append(System.lineSeparator());
            index++;
        }
        builder.append("}");
        Allure.addAttachment("Execution Context", "application/json", builder.toString(), ".json");
    }

    private static void writeEnvironment(Path outputDir) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("baseUrl", ConfigManager.baseUrl());
        properties.setProperty("maxResponseTimeMs", String.valueOf(ConfigManager.maxResponseTimeMs()));
        properties.setProperty("retryAttempts", String.valueOf(ConfigManager.retryAttempts()));
        properties.setProperty("retryBackoffMs", String.valueOf(ConfigManager.retryBackoffMs()));
        properties.setProperty("javaVersion", System.getProperty("java.version", "unknown"));
        properties.setProperty("osName", System.getProperty("os.name", "unknown"));
        properties.setProperty("osArch", System.getProperty("os.arch", "unknown"));

        Path target = outputDir.resolve("environment.properties");
        try (var writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            properties.store(writer, "Allure environment");
        }
    }

    private static void writeExecutor(Path outputDir) throws IOException {
        String runId = env("GITHUB_RUN_ID", "local");
        String runNumber = env("GITHUB_RUN_NUMBER", "0");
        String server = env("GITHUB_SERVER_URL", "https://github.com");
        String repository = env("GITHUB_REPOSITORY", "local/dog-api");
        String runUrl = server + "/" + repository + "/actions/runs/" + runId;

        String executor = "{\n"
            + "  \"name\": \"GitHub Actions\",\n"
            + "  \"type\": \"github\",\n"
            + "  \"buildName\": \"dog-api\",\n"
            + "  \"buildOrder\": \"" + runNumber + "\",\n"
            + "  \"buildUrl\": \"" + escape(runUrl) + "\",\n"
            + "  \"reportName\": \"Allure\",\n"
            + "  \"url\": \"" + escape(runUrl) + "\",\n"
            + "  \"timestamp\": \"" + Instant.now() + "\"\n"
            + "}";
        Files.writeString(outputDir.resolve("executor.json"), executor, StandardCharsets.UTF_8);
    }

    private static void copyCategories(Path outputDir) throws IOException {
        Path target = outputDir.resolve("categories.json");
        try (InputStream inputStream = AllureReportManager.class.getClassLoader()
            .getResourceAsStream("allure/categories.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("falta allure/categories.json no classpath");
            }
            Files.copy(inputStream, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String env(String variable, String defaultValue) {
        String value = System.getenv(variable);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
