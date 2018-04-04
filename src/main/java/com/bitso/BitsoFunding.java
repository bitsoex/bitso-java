package com.bitso;

import com.bitso.helpers.Helpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class BitsoFunding {
    private String fundingId;
    private String status;
    private Date fundingDate;
    private String currency;
    private String method;
    private BigDecimal amount;
    private HashMap<String, String> details;

    public BitsoFunding(JSONObject o) {
        fundingId = Helpers.getString(o, "fid");
        status = Helpers.getString(o, "status");
        fundingDate = Helpers.getZonedDatetime(o, "created_at");
        currency = Helpers.getString(o, "currency");
        method = Helpers.getString(o, "method");
        amount = Helpers.getBD(o, "amount");
        details = retrieveOperationDetails(o.getJSONObject("details"));
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this, BitsoFunding.class);
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

    public void addDetails(String key, String value){
        if(details != null){
            details.put(key, value);
        }
    }

    public void addDetails(HashMap<String, String> newDetails){
        details.putAll(newDetails);
    }

    private HashMap<String, String> retrieveOperationDetails(JSONObject o) {
        if (o == null) {
            return null;
        }

        HashMap<String, String> details = new HashMap<String, String>();

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

    public void setDetails(HashMap<String, String> details) {
        this.details = details;
    }
}
