package models;

public class ApiBaseResponse {

    private String status;
    private Object message;
    private Integer code;

    public String getStatus() {
        return status;
    }

    public Object getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }
}
