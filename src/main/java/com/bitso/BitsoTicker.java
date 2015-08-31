package com.bitso;

import java.math.BigDecimal;

import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoTicker {

    public BigDecimal last;
    public BigDecimal high;
    public BigDecimal low;
    public BigDecimal vwap;
    public BigDecimal volume;
    public BigDecimal bid;
    public BigDecimal ask;

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

    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
