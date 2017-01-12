package com.bitso.exchange;

import java.math.BigDecimal;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class Balance {
    String currency;
    BigDecimal total;
    BigDecimal locked;
    BigDecimal available;

    public Balance(String currency, BigDecimal total, BigDecimal locked, BigDecimal available) {
        this.currency = currency;
        this.total = total;
        this.locked = locked;
        this.available = available;
    }

    public Balance(JSONObject o) {
        this.currency = o.getString("currency");
        this.total = new BigDecimal(o.getString("total"));
        this.locked = new BigDecimal(o.getString("locked"));
        this.available = new BigDecimal(o.getString("available"));
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
