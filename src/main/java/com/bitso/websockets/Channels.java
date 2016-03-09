package com.bitso.websockets;

public enum Channels {
    TRADES("trades"), DIFF_ORDERS("diff-orders"), ORDERS("orders");

    private final String id;

    private Channels(String id) {
        this.id = id;
    }

    public String toString() {
        return this.id;
    }

}
