package com.bitso.exchange;

import java.math.BigDecimal;

import org.json.JSONObject;

import com.bitso.BitsoBook;
import com.bitso.helpers.Helpers;

public class BookInfo {

    public BitsoBook book;
    public BigDecimal minAmount;
    public BigDecimal maxAmount;
    public BigDecimal minPrice;
    public BigDecimal maxPrice;
    public BigDecimal minValue;
    public BigDecimal maxValue;

    public BookInfo(JSONObject o) {
        minAmount = Helpers.getBD(o, "minimum_amount");
        maxAmount = Helpers.getBD(o, "maximum_amount");
        minPrice = Helpers.getBD(o, "minimum_price");
        maxPrice = Helpers.getBD(o, "maximum_price");
        minValue = Helpers.getBD(o, "minimum_value");
        maxValue = Helpers.getBD(o, "maximum_value");
        book = BitsoBook.valueOf(Helpers.getString(o, "book").toUpperCase());
    }

    public String toString() {
        return Helpers.fieldPrinter(this);
    }

}
