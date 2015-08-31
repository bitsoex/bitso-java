package com.bitso;

import java.math.BigDecimal;

import org.json.JSONObject;

import com.bitso.exchange.Ticker;

public class BitsoTicker extends Ticker {

    public BitsoTicker(String json) {
        JSONObject o = new JSONObject(json);
        last = new BigDecimal(o.getString("last"));
        high = new BigDecimal(o.getString("high"));
        low = new BigDecimal(o.getString("low"));
        vwap = new BigDecimal(o.getString("vwap"));
        volume = new BigDecimal(o.getString("volume"));
        bid = new BigDecimal(o.getString("bid"));
        ask = new BigDecimal(o.getString("ask"));
    }
}
