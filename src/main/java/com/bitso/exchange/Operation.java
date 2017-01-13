package com.bitso.exchange;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class Operation {
    public String entryId;
    public String operationDescription;
    public Date operationDate;
    public BalanceUpdate[] afterOperationBalances;
    public Map<String, String> details;

    public Operation(JSONObject o) {
        entryId = Helpers.getString(o, "eid");
        operationDescription = Helpers.getString(o, "operation");
        operationDate = Helpers.getDate(Helpers.getString(o, "created_at"));
        afterOperationBalances = getOperationBalances(o.getJSONArray("balance_updates"));
        details = getOperationDetails(o.getJSONObject("details"));
    }

    private BalanceUpdate[] getOperationBalances(JSONArray array){
        int totalBalances =  array.length();
        BalanceUpdate[] balances =  new BalanceUpdate[totalBalances];
        for(int i=0; i<totalBalances; i++){
            balances[i] =  new BalanceUpdate(array.getJSONObject(i));
        }
        return balances;
    }

    private HashMap<String, String> getOperationDetails(JSONObject o){
        HashMap<String, String> detailsMap =  new HashMap<String, String>();
        Iterator<String> detailsIterator = o.keys();
        String currentKey = "";
        while(detailsIterator.hasNext()){
            currentKey = detailsIterator.next();
            detailsMap.put(currentKey, Helpers.getString(o, currentKey));
        }
        return detailsMap;
    }

    private class BalanceUpdate{
        String currency;
        BigDecimal amount;

        public BalanceUpdate(JSONObject o) {
            this.currency = Helpers.getString(o, "currency");
            this.amount = Helpers.getBD(o, "amount");
        }
    }
}