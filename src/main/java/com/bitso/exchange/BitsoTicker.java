package com.bitso.exchange;

import lombok.Value;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

import java.math.BigDecimal;
import java.util.Date;

/** The data returned from the ticker endpoint. */
@Value
public class BitsoTicker {

    /** The order book ID. */
    String book;
    /** The price at which the last trade happened. */
    BigDecimal last;
    /** The highest price at which a trade happened. */
    BigDecimal high;
    /** The lowest price at which a trade happened. */
    BigDecimal low;
    /** Volume-weighted average price. */
    BigDecimal vwap;
    /** The total amount that has been traded, in major currency. */
    BigDecimal volume;
    /** The price of the top buy order. */
    BigDecimal bid;
    /** The price of the top sell order. */
    BigDecimal ask;
    /** The time at which the ticker was created. */
    Date createdAt;

    public BitsoTicker(JSONObject o) {
        last = Helpers.getBD(o, "last");
        high = Helpers.getBD(o, "high");
        low = Helpers.getBD(o, "low");
        vwap = Helpers.getBD(o, "vwap");
        volume = Helpers.getBD(o, "volume");
        bid = Helpers.getBD(o, "bid");
        ask = Helpers.getBD(o, "ask");
        createdAt = Helpers.getZonedDatetime(o, "created_at");
        book = Helpers.getString(o, "book");
    }
}
