package com.bitso.exceptions;

import com.bitso.helpers.Helpers;

@SuppressWarnings("serial")
public class BitsoAPIException extends Exception {
    private int mErrorCode;
    private String mSimpleErrorMessage;
    private String mDetailedErrorMessage;

    public BitsoAPIException(int errorCode, String simpleErrorMessage) {
        this(errorCode, simpleErrorMessage, "BITSO-API no detailed message");
    }

    public BitsoAPIException(int errorCode, String simpleErrorMessage, String detailedErrorMessage) {
        this.mErrorCode = errorCode;
        this.mSimpleErrorMessage = simpleErrorMessage;
        this.mDetailedErrorMessage = detailedErrorMessage;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getSimpleErrorMessage() {
        return mSimpleErrorMessage;
    }

    public String getDetailedErrorMessage() {
        return mDetailedErrorMessage;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this, BitsoAPIException.class);
    }
}
