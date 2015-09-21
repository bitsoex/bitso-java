package com.bitso;

import java.math.BigDecimal;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoTransfer {
    public String walletAddress;
    public BigDecimal currencyAmount;
    public BigDecimal btcPending;
    public String confirmationCode;
    public String paymentOutletId;
    public String qrImgUri;
    public long createdAt;
    public BigDecimal currencyFees;
    public BigDecimal btcReceived;
    public BigDecimal btcAmount;
    public String currency;
    public String id;
    public String userUri;
    public HashMap<String, Object> fields;
    public BigDecimal currencySettled;
    public long expiresEpoch;

    public BitsoTransfer(JSONObject json) {
        JSONObject o = json.getJSONObject("order");
        walletAddress = o.getString("wallet_address");
        currencyAmount = new BigDecimal(o.getString("currency_amount"));
        btcPending = new BigDecimal(o.getString("btc_pending"));
        confirmationCode = o.getString("confirmation_code");
        paymentOutletId = o.getString("payment_outlet_id");
        qrImgUri = o.getString("qr_img_uri");
        createdAt = Long.valueOf(o.getString("created_at"));
        currencyFees = new BigDecimal(o.getString("currency_fees"));
        btcReceived = new BigDecimal(o.getString("btc_received"));
        btcAmount = new BigDecimal(o.getString("btc_amount"));
        currency = o.getString("currency");
        id = o.getString("id");
        userUri = o.getString("user_uri");
        if (o.has("fields")) {
            Object fields = o.get("fields");
            if (fields.getClass() == JSONObject.class) {
                JSONObject f = o.getJSONObject("fields");
                this.fields = new HashMap<String, Object>();
                for (String key : f.keySet()) {
                    this.fields.put(key, f.get(key));
                }
            } else if (fields.getClass() == JSONArray.class) {
                JSONArray f = o.getJSONArray("fields");
                if (f.length() > 0) {
                    System.err.println("Unknown fields format " + json.toString());
                }
            }
        }
        currencySettled = new BigDecimal(o.getString("currency_settled"));
        expiresEpoch = o.getLong("currency_settled");
    }

    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
