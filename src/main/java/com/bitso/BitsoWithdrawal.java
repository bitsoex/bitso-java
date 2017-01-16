package com.bitso;

import org.json.JSONArray;
import org.json.JSONObject;
import com.bitso.exchange.Withdrawal;
import com.bitso.helpers.Helpers;

public class BitsoWithdrawal {
    Withdrawal[] withdrawals;

    public BitsoWithdrawal(JSONObject o) {
        JSONArray withdrawalsJson = o.getJSONArray("payload");
        withdrawals = retrieveWithdrawals(withdrawalsJson);
    }

    private Withdrawal[] retrieveWithdrawals(JSONArray array){
        int totalElements = array.length();
        Withdrawal[] withdrawals = new Withdrawal[totalElements];
        for(int i=0; i<totalElements; i++){
            withdrawals[i] =  new Withdrawal(array.getJSONObject(i));
        }
        return withdrawals;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
