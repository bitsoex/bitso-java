package com.bitso;

public enum BitsoBook {
    BTC_MXN, ETH_MXN, XRP_BTC, XRP_MXN, ETH_BTC, BCH_BTC, LTC_MXN, LTC_BTC, BCH_MXN;

    public String toString() {
        return this.name().toLowerCase();
    }

    public BitsoCurrency getMajor() {
        switch (this) {
            case BTC_MXN:
                return BitsoCurrency.BTC;
            case XRP_BTC:
            case XRP_MXN:
                return BitsoCurrency.XRP;
            case ETH_BTC:
            case ETH_MXN:
                return BitsoCurrency.ETH;
            case BCH_BTC:
            case BCH_MXN:
                return BitsoCurrency.BCH;
            case LTC_BTC:
            case LTC_MXN:
                return BitsoCurrency.BCH;
            default:
                throw new IllegalStateException("No major specified for book: " + this.name());
        }
    }

    public BitsoCurrency getMinor() {
        switch (this) {
            case BTC_MXN:
            case ETH_MXN:
            case LTC_MXN:
            case XRP_MXN:
            case BCH_MXN:
                return BitsoCurrency.MXN;
            case BCH_BTC:
            case ETH_BTC:
            case LTC_BTC:
            case XRP_BTC:
                return BitsoCurrency.BTC;
            default:
                throw new IllegalStateException("No minor specified for book: " + this.name());
        }
    }
}
