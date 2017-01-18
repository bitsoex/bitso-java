package com.bitso.exceptions;

public class NotExpectedValue extends RuntimeException {
    public NotExpectedValue(String message){
        super(message);
    }
}
