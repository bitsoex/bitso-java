package com.bitso.exceptions;

@SuppressWarnings("serial")
public class BitsoAPIException extends Exception {
    private int mErrorCode = 101;

    public BitsoAPIException() {
        super();
    }

    public BitsoAPIException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BitsoAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public BitsoAPIException(String message) {
        super(message);
    }

    public BitsoAPIException(int errorCode, String message, Throwable initialException) {
        super(message, initialException);
        this.mErrorCode = errorCode;
    }

    public BitsoAPIException(int errorCode, String message) {
        super(message);
        this.mErrorCode = errorCode;
    }

    public BitsoAPIException(Throwable cause) {
        super(cause);
    }

    public int getErrorCode() {
        return mErrorCode;
    }
}
