package com.bitso.exchange;

import java.math.BigDecimal;

import com.bitso.helpers.Helpers;

public abstract class Ticker {

    public BigDecimal last;
    public BigDecimal high;
    public BigDecimal low;
    public BigDecimal vwap;
    public BigDecimal volume;
    public BigDecimal bid;
    public BigDecimal ask;

    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
