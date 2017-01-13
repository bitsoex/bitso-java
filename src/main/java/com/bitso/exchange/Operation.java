package com.bitso.exchange;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
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
    public ZonedDateTime operationDate;
    public BalanceUpdate[] afterOperationBalances;
    public Map<String, Object> details;

    public Operation(JSONObject o) {
        entryId = Helpers.getString(o, "eid");
        operationDescription = Helpers.getString(o, "operation");
        operationDate = Helpers.getZonedDatetime(Helpers.getString(o, "created_at"));
        afterOperationBalances = getOperationBalances(o.getJSONArray("balance_updates"));
        // TODO:
        // Key verification is not needed, this workaround is
        // valid meanwhile there is a server error.
        if(o.has("details")){
            details = getOperationDetails(o.getJSONObject("details"));
        }else{
            details = new HashMap<String, Object>();
        }
    }

    private BalanceUpdate[] getOperationBalances(JSONArray array){
        int totalBalances =  array.length();
        BalanceUpdate[] balances =  new BalanceUpdate[totalBalances];
        for(int i=0; i<totalBalances; i++){
            balances[i] =  new BalanceUpdate(array.getJSONObject(i));
        }
        return balances;
    }

    private HashMap<String, Object> getOperationDetails(JSONObject o){
        HashMap<String, Object> detailsMap =  new HashMap<String, Object>();
        Iterator<String> detailsIterator = o.keys();
        String currentKey = "";
        while(detailsIterator.hasNext()){
            currentKey = detailsIterator.next();
            // TODO:
            // With operation: trade or fee
            // in details tid key value is a number
            // Workaround, change HashMap values to object
            // User usage will be more complicated.
            // Suggestion: change server response to String
            // value.
            detailsMap.put(currentKey, o.get(currentKey));
        }
        return detailsMap;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
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