package dev.burikk.carrentz.engine.exception;

/**
 * @author Muhammad Irfan
 * @since 8/30/2017 6:39 PM
 */
public class WynixException extends RuntimeException {
    private int code = 9999;

    public WynixException(String message) {
        super(message);
    }

    public WynixException(String message, int code) {
        super(message);

        this.code = code;
    }

    public int getCode() {
        return code;
    }
}