package utils;

public final class ConfigManager {

    private static final String DEFAULT_BASE_URL = "https://dog.ceo/api";
    private static final long DEFAULT_MAX_RESPONSE_TIME_MS = 2000L;

    private ConfigManager() {
    }

    public static String getBaseUrl() {
        return System.getProperty("dog.api.baseUrl", DEFAULT_BASE_URL);
    }

    public static long getMaxResponseTimeMs() {
        return Long.parseLong(
            System.getProperty("dog.api.maxResponseTimeMs", String.valueOf(DEFAULT_MAX_RESPONSE_TIME_MS))
        );
    }
}
