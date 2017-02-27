package com.bitso;

import org.json.JSONObject;

import com.bitso.exchange.Ticker;
import com.bitso.helpers.Helpers;

public class BitsoTicker extends Ticker {

    public BitsoBook book;

    public BitsoTicker(JSONObject o) {
        last = Helpers.getBD(o, "last");
        high = Helpers.getBD(o, "high");
        low = Helpers.getBD(o, "low");
        vwap = Helpers.getBD(o, "vwap");
        volume = Helpers.getBD(o, "volume");
        bid = Helpers.getBD(o, "bid");
        ask = Helpers.getBD(o, "ask");
        createdAt = Helpers.getZonedDatetime(o, "created_at");
        book = BitsoBook.valueOf(Helpers.getString(o, "book").toUpperCase());
    }
}
