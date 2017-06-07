package com.bitso;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bitso.exchange.Ticker;
import com.bitso.helpers.Helpers;

public class BitsoOperation {
    private String entryId;
    private String operationDescription;
    private Date operationDate;
    private BalanceUpdate[] afterOperationBalances;
    private HashMap<String, String> details;

    public BitsoOperation(JSONObject o) {
        entryId = Helpers.getString(o, "eid");
        operationDescription = Helpers.getString(o, "operation");
        operationDate = Helpers.getZonedDatetime(o, "created_at");
        afterOperationBalances = retrieveOperationBalances(o.getJSONArray("balance_updates"));
        details = retrieveOperationDetails(o.getJSONObject("details"));
    }

    private BalanceUpdate[] retrieveOperationBalances(JSONArray array) {
        int totalBalances = array.length();
        BalanceUpdate[] balances = new BalanceUpdate[totalBalances];
        for (int i = 0; i < totalBalances; i++) {
            balances[i] = new BalanceUpdate(array.getJSONObject(i));
        }
        return balances;
    }

    private HashMap<String, String> retrieveOperationDetails(JSONObject o) {
        if (o == null) {
            return null;
        }

        HashMap<String, String> details = new HashMap<>();

        String currentKey;
        String currentValue;
        Iterator<String> detailsKeys = o.keys();

        while (detailsKeys.hasNext()) {
            currentKey = detailsKeys.next();
            try {
                currentValue = Helpers.getString(o, currentKey);
            } catch (JSONException exception) {
                currentValue = String.valueOf(Helpers.getInt(o, currentKey));
            }
            details.put(currentKey, currentValue);
        }

        return details;
    }

    public String toString() {
        return Helpers.fieldPrinter(this, BitsoOperation.class);
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getOperationDescription() {
        return operationDescription;
    }

    public void setOperationDescription(String operationDescription) {
        this.operationDescription = operationDescription;
    }

    public Date getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(Date operationDate) {
        this.operationDate = operationDate;
    }

    public BalanceUpdate[] getAfterOperationBalances() {
        return afterOperationBalances;
    }

    public void setAfterOperationBalances(BalanceUpdate[] afterOperationBalances) {
        this.afterOperationBalances = afterOperationBalances;
    }

    public HashMap<String, String> getDetails() {
        return details;
    }

    public void setDetails(HashMap<String, String> details) {
        this.details = details;
    }

    public class BalanceUpdate {
        private String currency;
        private BigDecimal amount;

        public BalanceUpdate(JSONObject o) {
            this.currency = Helpers.getString(o, "currency");
            this.amount = Helpers.getBD(o, "amount");
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String toString() {
            return Helpers.fieldPrinter(this, BitsoOperation.BalanceUpdate.class);
        }
    }
}
