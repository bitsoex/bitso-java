package com.bitso;

public enum BitsoBook {
    BTC_MXN, ETH_MXN;

    public String toString() {
        return this.name().toLowerCase();
    }
}
