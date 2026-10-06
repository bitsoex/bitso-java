package com.bitso.exchange;

import java.math.BigDecimal;

import lombok.Data;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

@Data
public class BookInfo {

    private String book;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minValue;
    private BigDecimal maxValue;

    public BookInfo(JSONObject o) {
        minAmount = Helpers.getBD(o, "minimum_amount");
        maxAmount = Helpers.getBD(o, "maximum_amount");
        minPrice = Helpers.getBD(o, "minimum_price");
        maxPrice = Helpers.getBD(o, "maximum_price");
        minValue = Helpers.getBD(o, "minimum_value");
        maxValue = Helpers.getBD(o, "maximum_value");
        book = Helpers.getString(o, "book");
    }
}
