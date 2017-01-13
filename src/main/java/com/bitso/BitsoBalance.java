package com.bitso;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exchange.Balance;
import com.bitso.helpers.Helpers;

public class BitsoBalance {

    Balance balances[];

    public BitsoBalance(JSONObject obj) {
        JSONObject payload = obj.getJSONObject("payload");
        JSONArray jsonBalances = payload.getJSONArray("balances");
        balances = retrieveBalances(jsonBalances);
    }

    private Balance[] retrieveBalances(JSONArray array){
        int totalElements = array.length();
        Balance[] balances = new Balance[totalElements];
        for(int i=0; i<totalElements; i++){
            balances[i] = new Balance(array.getJSONObject(i));
        }
        return balances;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
