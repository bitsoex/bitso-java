package com.bitso.websockets;

public enum BitsoChannels {
    TRADES("trades"), DIFF_ORDERS("diff-orders"), ORDERS("orders");

    private final String id;

    private BitsoChannels(String id) {
        this.id = id;
    }

    public String toString() {
        return this.id;
    }

}
