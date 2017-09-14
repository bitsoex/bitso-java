package com.bitso.exceptions;

public class BitsoPayloadException extends Exception {
    private static final long serialVersionUID = 1L;

    public BitsoPayloadException() {
        super();
    }

    public BitsoPayloadException(String className, String method) {
        super(className + ": " + method + " payload key was not found");
    }

    public BitsoPayloadException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BitsoPayloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public BitsoPayloadException(String message) {
        super(message);
    }

    public BitsoPayloadException(Throwable cause) {
        super(cause);
    }

}
