package com.bitso;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoFunding {
    protected String fundingId;
    protected String status;
    protected Date fundingDate;
    protected String currency;
    protected String method;
    protected BigDecimal amount;
    protected JSONObject details;

    public BitsoFunding(JSONObject o) {
        fundingId = Helpers.getString(o, "fid");
        status = Helpers.getString(o, "status");
        fundingDate = Helpers.getZonedDatetime(o, "created_at");
        currency = Helpers.getString(o, "currency");
        method = Helpers.getString(o, "method");
        amount = Helpers.getBD(o, "amount");
        details = o.getJSONObject("details");
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }

    public String getFundingId() {
        return fundingId;
    }

    public void setFundingId(String fundingId) {
        this.fundingId = fundingId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getFundingDate() {
        return fundingDate;
    }

    public void setFundingDate(Date fundingDate) {
        this.fundingDate = fundingDate;
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
}
