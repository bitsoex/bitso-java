package com.bitso.exchange;

import java.math.BigDecimal;

import com.bitso.BitsoBook;
import com.bitso.helpers.Helpers;

public class BookInfo {

    public BitsoBook book;
    public BigDecimal minAmount;
    public BigDecimal maxAmount;
    public BigDecimal minPrice;
    public BigDecimal maxPrice;
    public BigDecimal minValue;
    public BigDecimal MaxValue;

    public String toString() {
        return Helpers.fieldPrinter(this);
    }

}
