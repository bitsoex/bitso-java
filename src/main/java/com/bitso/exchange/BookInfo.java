package com.bitso.exchange;

import java.math.BigDecimal;

import lombok.Data;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

/**
 * Information about an order book.
 */
@Data
public class BookInfo {

    /** The book ID, in major_minor form. */
    private String book;
    /** The smallest amount acceptable for an order, in major currency. */
    private BigDecimal minAmount;
    /** The largest amount acceptable for an order, in major currency. */
    private BigDecimal maxAmount;
    /** The lowest price acceptable for an order, in minor currency. */
    private BigDecimal minPrice;
    /** The highest price acceptable for an order, in minor currency. */
    private BigDecimal maxPrice;
    /** The smallest value acceptable for an order, in minor currency. */
    private BigDecimal minValue;
    /** The largest value acceptable for an order, in minor currency. */
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
