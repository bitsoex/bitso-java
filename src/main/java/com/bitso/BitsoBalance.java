package com.bitso;

import java.math.BigDecimal;
import java.util.HashMap;

import lombok.Data;
import lombok.Value;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoBalance {
    private HashMap<String, Balance> mBalances;

    public BitsoBalance(JSONObject o) {
        mBalances = new HashMap<String, Balance>();
        JSONArray jsonBalances = o.getJSONArray("balances");
        int totalElements = jsonBalances.length();
        for (int i = 0; i < totalElements; i++) {
            JSONObject balance = jsonBalances.getJSONObject(i);
            String currency = Helpers.getString(balance, "currency");
            Balance currentBalance = new Balance(currency, Helpers.getBD(balance, "total"),
                    Helpers.getBD(balance, "locked"), Helpers.getBD(balance, "available"));
            mBalances.put(currency, currentBalance);
        }
    }

    public HashMap<String, Balance> getBalances() {
        return mBalances;
    }

    public void setBalances(HashMap<String, Balance> mBalances) {
        this.mBalances = mBalances;
    }

    public String toString() {
        return Helpers.fieldPrinter(this, BitsoBalance.class);
    }

    @Value
    public class Balance {
        String currency;
        BigDecimal total;
        BigDecimal locked;
        BigDecimal available;
    }
}
