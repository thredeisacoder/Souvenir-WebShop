package project.demo.exception;

public class ShipmentException extends RuntimeException {
    private final String code;

    public ShipmentException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}