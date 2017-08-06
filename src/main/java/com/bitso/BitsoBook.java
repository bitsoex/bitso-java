package com.bitso;

public enum BitsoBook {
    BTC_MXN, ETH_MXN, XRP_BTC, XRP_MXN, ETH_BTC, BCH_BTC;

    public String toString() {
        return this.name().toLowerCase();
    }

    public BitsoCurrency getMajor() {
        switch (this) {
            case BTC_MXN:
                return BitsoCurrency.BTC;
            case ETH_MXN:
                return BitsoCurrency.ETH;
            case XRP_BTC:
                return BitsoCurrency.XRP;
            case XRP_MXN:
                return BitsoCurrency.XRP;
            case ETH_BTC:
                return BitsoCurrency.ETH;
            case BCH_BTC:
                return BitsoCurrency.BCH;
            default:
                throw new IllegalStateException("No major specified for book: " + this.name());
        }
    }

    public BitsoCurrency getMinor() {
        switch (this) {
            case BTC_MXN:
                return BitsoCurrency.MXN;
            case ETH_MXN:
                return BitsoCurrency.MXN;
            case XRP_BTC:
                return BitsoCurrency.BTC;
            case XRP_MXN:
                return BitsoCurrency.MXN;
            case ETH_BTC:
                return BitsoCurrency.BTC;
            case BCH_BTC:
                return BitsoCurrency.BTC;
            default:
                throw new IllegalStateException("No minor specified for book: " + this.name());
        }
    }
}
