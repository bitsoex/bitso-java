package com.bitso.exchange;

import java.math.BigDecimal;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class Balance {
    public String currency;
    public BigDecimal total;
    public BigDecimal locked;
    public BigDecimal available;

    public Balance(String currency, BigDecimal total, BigDecimal locked, BigDecimal available) {
        this.currency = currency;
        this.total = total;
        this.locked = locked;
        this.available = available;
    }

    public Balance(JSONObject o) {
        this.currency = Helpers.getString(o, "currency");
        this.total = Helpers.getBD(o, "total");
        this.locked = Helpers.getBD(o, "locked");
        this.available = Helpers.getBD(o, "available");
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
