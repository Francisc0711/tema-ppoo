package ebanking.exception;

/** Thrown when an operation would take an account balance below zero. */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
