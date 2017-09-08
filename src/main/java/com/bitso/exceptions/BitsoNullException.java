package com.bitso.exceptions;

public class BitsoNullException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public BitsoNullException() {
        super();
    }

    public BitsoNullException(String className, String method) {
        super(className + ": " + method + " parameter is null");
    }

    public BitsoNullException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BitsoNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public BitsoNullException(String message) {
        super(message);
    }

    public BitsoNullException(Throwable cause) {
        super(cause);
    }

}
