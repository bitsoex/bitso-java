package com.bitso.exceptions;

public class BitsoWebSocketExeption extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public BitsoWebSocketExeption(String message) {
        super(message);
    }
}