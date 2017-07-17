package com.bitso.exceptions;

public class BitsoAPIException extends Exception {
    private boolean mStatus;
    private int mErrorCode;
    private String mErrorMessage;
    
    public BitsoAPIException(boolean status, int errorCode, String errorMessage){
        this.mStatus = status;
        this.mErrorCode = errorCode;
        this.mErrorMessage = errorMessage;
    }

    public boolean isStatus() {
        return mStatus;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    @Override
    public String toString() {
        return "BitsoAPIException Status=" + mStatus + ", ErrorCode=" +
                mErrorCode + ", ErrorMessage=" + mErrorMessage;
    }
    
    
}
