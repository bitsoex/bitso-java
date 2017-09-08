package com.bitso.exceptions;

import com.bitso.helpers.Helpers;

@SuppressWarnings("serial")
public class BitsoAPIException extends Exception {
    private int mErrorCode;

    public BitsoAPIException(int errorCode, String apiErrorMessage) {
        super(apiErrorMessage);
        this.mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this, BitsoAPIException.class);
    }
}
