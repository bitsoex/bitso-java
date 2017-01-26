package com.bitso.exceptions;

public class BitsoExceptionNotExpectedValue extends RuntimeException {
    public BitsoExceptionNotExpectedValue(String message){
        super(message);
    }
}
