package com.bitso.exceptions;

public class BitsoServerException extends Exception{
    private static final long serialVersionUID = 1L;

    public BitsoServerException() {
        super();
    }

    public BitsoServerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BitsoServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public BitsoServerException(String message) {
        super(message);
    }

    public BitsoServerException(Throwable cause) {
        super(cause);
    }

}
