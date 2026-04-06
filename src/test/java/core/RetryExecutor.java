package core;

import io.qameta.allure.Allure;
import io.restassured.response.Response;
import java.util.Set;
import java.util.function.Supplier;

public final class RetryExecutor {

    private static final Set<Integer> TRANSIENT_STATUS = Set.of(429, 500, 502, 503, 504);

    private RetryExecutor() {
    }

    public static Response executeWithTransientRetry(String endpoint, Supplier<Response> call) {
        Response response = null;
        RuntimeException runtimeException = null;

        for (int attempt = 1; attempt <= ConfigManager.retryAttempts(); attempt++) {
            try {
                response = call.get();
                if (!isTransientStatus(response.getStatusCode()) || attempt == ConfigManager.retryAttempts()) {
                    return response;
                }
                Allure.step("Retry devido a status transitorio: " + response.getStatusCode());
                AllureReportManager.attachTransientRetry(endpoint, attempt, "HTTP " + response.getStatusCode());
            } catch (RuntimeException exception) {
                runtimeException = exception;
                if (attempt == ConfigManager.retryAttempts()) {
                    break;
                }
                Allure.step("Retry devido a excecao transitoria: " + exception.getClass().getSimpleName());
                AllureReportManager.attachTransientRetry(endpoint, attempt, exception.getMessage());
            }

            sleepBackoff();
        }

        if (runtimeException != null) {
            throw runtimeException;
        }
        return response;
    }

    private static boolean isTransientStatus(int statusCode) {
        return TRANSIENT_STATUS.contains(statusCode);
    }

    private static void sleepBackoff() {
        try {
            Thread.sleep(ConfigManager.retryBackoffMs());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrompida durante retry", interruptedException);
        }
    }
}
