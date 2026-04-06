package models;

import java.util.List;
import java.util.Map;

public class BreedListResponse {

    private String status;
    private Map<String, List<String>> message;

    public String getStatus() {
        return status;
    }

    public Map<String, List<String>> getMessage() {
        return message;
    }
}
