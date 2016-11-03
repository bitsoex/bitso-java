package com.bitso;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.json.JSONObject;

import com.bitso.exchange.Ticker;
import com.bitso.helpers.Helpers;

public class BitsoTicker extends Ticker {

    public BitsoBook book;

    public BitsoTicker(JSONObject o) {
        last = getBD(o, "last");
        high = getBD(o, "high");
        low = getBD(o, "low");
        vwap = getBD(o, "vwap");
        volume = getBD(o, "volume");
        bid = getBD(o, "bid");
        ask = getBD(o, "ask");
        createdAt = ZonedDateTime.parse(getString(o, "created_at"));
        book = BitsoBook.valueOf(getString(o, "book").toUpperCase());
    }

    private String getString(JSONObject o, String key) {
        if (o.has(key)) {
            return o.getString(key);
        } else {
            System.err.println("No " + key + ": " + o);
            Helpers.printStackTrace();
        }
        return null;
    }

    private BigDecimal getBD(JSONObject o, String key) {
        if (o.has(key)) {
            return new BigDecimal(o.getString(key));
        } else {
            System.err.println("No " + key + ": " + o);
            Helpers.printStackTrace();
        }
        return null;
    }
}
