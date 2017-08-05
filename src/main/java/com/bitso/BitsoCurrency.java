package com.bitso;

public enum BitsoCurrency {

    BCH, BTC, ETH, MXN, XRP;

    public String toString() {
        return this.name().toLowerCase();
    }
}
