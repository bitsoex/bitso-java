package com.bitso.exceptions;

public class BitsoExceptionJSONPayload extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BitsoExceptionJSONPayload(String message){
        super(message);
    }
}
