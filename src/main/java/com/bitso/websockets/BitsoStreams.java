package com.bitso.websockets;

public enum BitsoStreams {
    DIFF_ORDERS, ORDERS, TRADES, KA;

    public String toString() {
        return this.name().toLowerCase();
    }
}