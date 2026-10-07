package com.bitso;

import lombok.Getter;
import lombok.ToString;
import org.json.JSONObject;

import com.bitso.exchange.Ticker;
import com.bitso.helpers.Helpers;

/** The data returned from the ticker endpoint. */
@ToString
public class BitsoTicker extends Ticker {

    /** The order book ID. */
    @Getter
    private final String book;

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
