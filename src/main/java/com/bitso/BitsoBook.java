package com.bitso;

public enum BitsoBook {
    BTC_MXN;

    public String toString() {
        return this.name().toLowerCase();
    }
}
