package com.bitso.exceptions;

@SuppressWarnings("serial")
public class BitsoExceptionJSONPayload extends RuntimeException {
    public BitsoExceptionJSONPayload(String message){
        super(message);
    }
}
