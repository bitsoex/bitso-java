package com.bitso;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoWithdrawal {
    private String withdrawalId;
    private String status;
    private Date withdrawalDate;
    private String currency;
    private String method;
    private BigDecimal amount;
    private HashMap<String, String> details;

    public BitsoWithdrawal(JSONObject o) {
        withdrawalId = Helpers.getString(o, "wid");
        status = Helpers.getString(o, "status");
        withdrawalDate = Helpers.getZonedDatetime(o, "created_at");
        currency = Helpers.getString(o, "currency");
        method = Helpers.getString(o, "method");
        amount = Helpers.getBD(o, "amount");
        details = o.has("details") ? retrieveOperationDetails(o.getJSONObject("details")) : null;
    }

    public String getWithdrawalId() {
        return withdrawalId;
    }

    public void setWithdrawalId(String withdrawalId) {
        this.withdrawalId = withdrawalId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(Date withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public HashMap<String, String> getDetails() {
        return details;
    }

    public void setDetails(HashMap<String, String> details) {
        this.details = details;
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, String> retrieveOperationDetails(JSONObject o) {
        HashMap<String, String> details = new HashMap<String, String>();

        String currentKey;
        String currentValue = null;
        Object object;
        Iterator<String> detailsKeys = o.keys();

        while (detailsKeys.hasNext()) {
            currentKey = detailsKeys.next();
            object = o.get(currentKey);

            if (object == null) {
                continue;
            }

            if (object instanceof String) {
                currentValue = (String) object;
            }

            if (object instanceof JSONObject) {
                currentValue = ((JSONObject) object).toString();
            }

            details.put(currentKey, currentValue);
        }
        return details;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this, BitsoWithdrawal.class);
    }
}
