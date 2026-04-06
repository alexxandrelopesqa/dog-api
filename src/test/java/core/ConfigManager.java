package core;

public final class ConfigManager {

    private static final String DEFAULT_BASE_URL = "https://dog.ceo/api";
    private static final int DEFAULT_MAX_RESPONSE_TIME_MS = 3000;
    private static final int DEFAULT_RETRY_ATTEMPTS = 3;
    private static final long DEFAULT_RETRY_BACKOFF_MS = 250L;

    private ConfigManager() {
    }

    public static String baseUrl() {
        return System.getProperty("dog.api.baseUrl", DEFAULT_BASE_URL);
    }

    public static int maxResponseTimeMs() {
        return Integer.parseInt(
            System.getProperty("dog.api.maxResponseTimeMs", String.valueOf(DEFAULT_MAX_RESPONSE_TIME_MS))
        );
    }

    public static int retryAttempts() {
        return Integer.parseInt(
            System.getProperty("dog.api.retryAttempts", String.valueOf(DEFAULT_RETRY_ATTEMPTS))
        );
    }

    public static long retryBackoffMs() {
        return Long.parseLong(
            System.getProperty("dog.api.retryBackoffMs", String.valueOf(DEFAULT_RETRY_BACKOFF_MS))
        );
    }
}
