package com.bitso.exceptions;

@SuppressWarnings("serial")
public class BitsoExceptionNotExpectedValue extends RuntimeException {
    public BitsoExceptionNotExpectedValue(String message){
        super(message);
    }
}
