package com.bitso;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoFunding {
    protected String fundingId;
    protected String status;
    protected Date fundingDate;
    protected String currency;
    protected String method;
    protected BigDecimal amount;
    protected HashMap<String, String> details;

    public BitsoFunding(JSONObject o) {
        fundingId = Helpers.getString(o, "fid");
        status = Helpers.getString(o, "status");
        fundingDate = Helpers.getZonedDatetime(o, "created_at");
        currency = Helpers.getString(o, "currency");
        method = Helpers.getString(o, "method");
        amount = Helpers.getBD(o, "amount");
        details = getOperationDetails(o.getJSONObject("details"));
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

    public HashMap<String, String> getDetails() {
        return details;
    }

    private HashMap<String, String> getOperationDetails(JSONObject o) {
        if (o == null) {
            return null;
        }

        HashMap<String, String> details = new HashMap<>();

        for (Object key : o.keySet()) {
            String value;
            try {
                value = Helpers.getString(o, (String) key);
            } catch (JSONException exception) {
                value = String.valueOf(Helpers.getInt(o, (String) key));
            }
            details.put((String) key, value);
        }

        return details;
    }

    public void setDetails(HashMap<String, String> details) {
        this.details = details;
    }
}
