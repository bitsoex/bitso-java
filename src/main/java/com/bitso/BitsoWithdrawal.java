package com.bitso;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoWithdrawal {
    protected String withdrawalId;
    protected String status;
    protected Date withdrawalDate;
    protected String currency;
    protected String method;
    protected BigDecimal amount;
    protected JSONObject details;

    public BitsoWithdrawal(JSONObject o) {
        withdrawalId = Helpers.getString(o, "wid");
        status = Helpers.getString(o, "status");
        withdrawalDate = Helpers.getZonedDatetime(o, "created_at");
        currency = Helpers.getString(o, "currency");
        method = Helpers.getString(o, "method");
        amount = Helpers.getBD(o, "amount");
        details = o.getJSONObject("details");
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

    public JSONObject getDetails() {
        return details;
    }

    public void setDetails(JSONObject details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
