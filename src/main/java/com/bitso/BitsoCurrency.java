package com.bitso;

public enum BitsoCurrency {

    BTC, MXN, ETH, XRP;

    public String toString() {
        return this.name().toLowerCase();
    }
}
