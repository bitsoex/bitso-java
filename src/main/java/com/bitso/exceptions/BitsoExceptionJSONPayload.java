package com.bitso.exceptions;

public class BitsoExceptionJSONPayload extends RuntimeException {
    public BitsoExceptionJSONPayload(String message){
        super(message);
    }
}
