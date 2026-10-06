package com.bitso;

import com.bitso.helpers.Helpers;

import lombok.Data;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Data
public class BitsoFunding {
    private String fundingId;
    private String status;
    private Date fundingDate;
    private String currency;
    private String method;
    private BigDecimal amount;
    private final Map<String, String> details;

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

    public void addDetails(String key, String value) {
        details.put(key, value);
    }

    public void addDetails(Map<String, String> newDetails){
        details.putAll(newDetails);
    }

    private HashMap<String, String> retrieveOperationDetails(JSONObject o) {
        var m = new HashMap<String, String>();

        if (o == null) {
            return m;
        }

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
            m.put(currentKey, currentValue);
        }

        return m;
    }

    public void setDetails(Map<String, String> value) {
        if (details == value) {
            return;
        }
        details.clear();
        details.putAll(value);
    }
}
