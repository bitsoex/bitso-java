package com.bitso.exceptions;

@SuppressWarnings("serial")
public class BitsoAPIException extends Exception {
    private int mErrorCode;

    public BitsoAPIException() {
        super();
        this.mErrorCode = 101;
    }

    public BitsoAPIException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.mErrorCode = 101;
    }

    public BitsoAPIException(String message, Throwable cause) {
        super(message, cause);
        this.mErrorCode = 101;
    }

    public BitsoAPIException(String message) {
        super(message);
        this.mErrorCode = 101;
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
        this.mErrorCode = 101;
    }

    public int getErrorCode() {
        return mErrorCode;
    }
}
