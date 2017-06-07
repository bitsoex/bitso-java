package com.bitso;

public enum BitsoSide{
    SELL, BUY;

    public String toString() {
        return this.name().toLowerCase();
    }
}