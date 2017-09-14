package com.bitso.exceptions;

public class BitsoValidationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -4067877367043312417L;

    public BitsoValidationException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public BitsoValidationException(String className, String cause) {
        super(className + ": " + cause);
    }

    public BitsoValidationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public BitsoValidationException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public BitsoValidationException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public BitsoValidationException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
