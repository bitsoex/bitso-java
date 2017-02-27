package com.bitso.exchange;

import java.math.BigDecimal;
import java.util.Date;

import com.bitso.helpers.Helpers;

public abstract class Ticker {

    public BigDecimal last;
    public BigDecimal high;
    public BigDecimal low;
    public BigDecimal vwap;
    public BigDecimal volume;
    public BigDecimal bid;
    public BigDecimal ask;
    public Date createdAt;

    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
