package com.bitso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoTransfer {
    protected String walletAddress;
    protected BigDecimal currencyAmount;
    protected BigDecimal btcPending;
    protected String confirmationCode;
    protected String paymentOutletId;
    protected String qrImgUri;
    protected long createdAt;
    protected BigDecimal currencyFees;
    protected BigDecimal btcReceived;
    protected BigDecimal btcAmount;
    protected String currency;
    protected String id;
    protected String userUri;
    protected HashMap<String, Object> fields;
    protected BigDecimal currencySettled;
    protected long expiresEpoch;

    public enum STATUS {
        pending, confirming, completed, expired
    }

    public STATUS status;

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
                Iterator<String> keys = f.keys();
                String currentKey;
                while(keys.hasNext()){
                    currentKey = keys.next();
                    this.fields.put(currentKey, f.get(currentKey));
                }
            } else if (fields.getClass() == JSONArray.class) {
                JSONArray f = o.getJSONArray("fields");
                if (f.length() > 0) {
                    System.err.println("Unknown fields format " + json.toString());
                }
            }
        }
        currencySettled = new BigDecimal(o.getString("currency_settled"));
        expiresEpoch = Long.valueOf(o.getString("expires_epoch"));
        if (o.has("status")) {
            status = STATUS.valueOf(o.getString("status"));
        }
    }

    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
