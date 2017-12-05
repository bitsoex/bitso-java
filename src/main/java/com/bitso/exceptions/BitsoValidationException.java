package com.bitso.exceptions;

public class BitsoValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    public BitsoValidationException() {
        super();
    }

    public BitsoValidationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BitsoValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BitsoValidationException(String message) {
        super(message);
    }

    public BitsoValidationException(Throwable cause) {
        super(cause);
    }

}
