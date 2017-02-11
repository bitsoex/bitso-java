package com.bitso.exceptions;

public class BitsoWebSocketExeption extends RuntimeException{
    public BitsoWebSocketExeption(String message) {
        super(message);
    }
}