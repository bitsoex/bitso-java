package com.bitso;

import java.math.BigDecimal;

import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoTransferQuote {
    public BigDecimal gross;
    public BigDecimal rate;
    public BigDecimal btcAmount;
    public String currency;
    public long timestamp;
    public long expiresEpoch;

    public BitsoTransferQuote(JSONObject json) {
        JSONObject o = json.getJSONObject("quote");
        gross = new BigDecimal(o.getString("gross"));
        rate = new BigDecimal(o.getString("rate"));
        btcAmount = new BigDecimal(o.getString("btc_amount"));
        currency = o.getString("currency");
        timestamp = Long.valueOf(o.getString("timestamp"));
        expiresEpoch = o.getLong("expires_epoch");
    }

    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
