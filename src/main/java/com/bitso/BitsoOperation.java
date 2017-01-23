package com.bitso;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoOperation {
    public String entryId;
    public String operationDescription;
    public ZonedDateTime operationDate;
    public BalanceUpdate[] afterOperationBalances;
    public JSONObject details;

    public BitsoOperation(JSONObject o) {
        entryId = Helpers.getString(o, "eid");
        operationDescription = Helpers.getString(o, "operation");
        operationDate = Helpers.getZonedDatetime(o, "created_at");
        afterOperationBalances = getOperationBalances(o.getJSONArray("balance_updates"));
        details = o.getJSONObject("details");
    }

    private BalanceUpdate[] getOperationBalances(JSONArray array){
        int totalBalances =  array.length();
        BalanceUpdate[] balances =  new BalanceUpdate[totalBalances];
        for(int i=0; i<totalBalances; i++){
            balances[i] =  new BalanceUpdate(array.getJSONObject(i));
        }
        return balances;
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