package com.bitso.exceptions;

public class BitsoWebSocketException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public BitsoWebSocketException(String message) {
        super(message);
    }
}