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

    public boolean ismStatus() {
        return mStatus;
    }

    public void setmStatus(boolean mStatus) {
        this.mStatus = mStatus;
    }

    public int getmErrorCode() {
        return mErrorCode;
    }

    public void setmErrorCode(int mErrorCode) {
        this.mErrorCode = mErrorCode;
    }

    public String getmErrorMessage() {
        return mErrorMessage;
    }

    public void setmErrorMessage(String mErrorMessage) {
        this.mErrorMessage = mErrorMessage;
    }

    @Override
    public String toString() {
        return "BitsoAPIException Status=" + mStatus + ", ErrorCode=" +
                mErrorCode + ", ErrorMessage=" + mErrorMessage;
    }
    
    
}
