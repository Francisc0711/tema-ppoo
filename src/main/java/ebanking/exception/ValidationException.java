package ebanking.exception;

/** Thrown when input data is invalid; callers must handle it. */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
