package com.bitso.exchange;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

/** A ticker entry. */
@Data
public class Ticker {

    protected BigDecimal last;
    protected BigDecimal high;
    protected BigDecimal low;
    protected BigDecimal vwap;
    protected BigDecimal volume;
    protected BigDecimal bid;
    protected BigDecimal ask;
    protected Date createdAt;
}
