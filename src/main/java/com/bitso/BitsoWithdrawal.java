package com.bitso;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoWithdrawal {
    protected String withdrawalId;
    protected String status;
    protected Date withdrawalDate;
    protected String currency;
    protected String method;
    protected BigDecimal amount;
    protected HashMap<String, String> details;

    public BitsoWithdrawal(JSONObject o) {
        withdrawalId = Helpers.getString(o, "wid");
        status = Helpers.getString(o, "status");
        withdrawalDate = Helpers.getZonedDatetime(o, "created_at");
        currency = Helpers.getString(o, "currency");
        method = Helpers.getString(o, "method");
        amount = Helpers.getBD(o, "amount");
        details = getOperationDetails(o.getJSONObject("details"));
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

    private HashMap<String, String> getOperationDetails(JSONObject o) {
        if (o == null) {
            return null;
        }

        HashMap<String, String> details = new HashMap<>();

        for (Object key : o.keySet()) {
            String value = null;
            Object object = o.get((String) key);

            if (object == null) {
                value = "";
            } else {
                if (object instanceof String) {
                    value = (String) object;
                }

                if (object instanceof JSONObject) {
                    value = ((JSONObject) object).toString();
                }
            }

            details.put((String) key, value);
        }

        return details;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
