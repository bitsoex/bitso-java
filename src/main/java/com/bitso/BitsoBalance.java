package com.bitso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import lombok.Value;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

/** A container for a user's balances. */
@Value
public class BitsoBalance {
    /** A map from currencies to balances. */
    Map<String, Balance> balances;

    public BitsoBalance(JSONObject o) {
        var mBalances = new HashMap<String, Balance>();
        JSONArray jsonBalances = o.getJSONArray("balances");
        int totalElements = jsonBalances.length();
        for (int i = 0; i < totalElements; i++) {
            JSONObject balance = jsonBalances.getJSONObject(i);
            String currency = Helpers.getString(balance, "currency");
            Balance currentBalance = new Balance(currency, Helpers.getBD(balance, "total"),
                    Helpers.getBD(balance, "locked"), Helpers.getBD(balance, "available"));
            mBalances.put(currency, currentBalance);
        }
        this.balances = Map.copyOf(mBalances);
    }

    /** The balance of a single currency. */
    @Value
    public class Balance {
        /** The currency code. */
        String currency;
        /** The total balance. */
        BigDecimal total;
        /** The balance that's locked in open orders. */
        BigDecimal locked;
        /** The available balance, that is, total minus available. */
        BigDecimal available;
    }
}
