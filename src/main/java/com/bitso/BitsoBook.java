package com.bitso;

public enum BitsoBook {
    BTC_MXN, ETH_MXN, XRP_BTC, XRP_MXN, ETH_BTC;

    public String toString() {
        return this.name().toLowerCase();
    }
}
