package com.bitso;

import com.bitso.helpers.Helpers;

import lombok.Value;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

@Value
public class BitsoWithdrawal {
    String withdrawalId;
    String status;
    Date withdrawalDate;
    String currency;
    String method;
    BigDecimal amount;
    HashMap<String, String> details;

    public BitsoWithdrawal(JSONObject o) {
        withdrawalId = Helpers.getString(o, "wid");
        status = Helpers.getString(o, "status");
        withdrawalDate = Helpers.getZonedDatetime(o, "created_at");
        currency = Helpers.getString(o, "currency");
        method = Helpers.getString(o, "method");
        amount = Helpers.getBD(o, "amount");
        details = o.has("details") ? retrieveOperationDetails(o.getJSONObject("details")) : null;
    }

    public void addDetails(String key, String value){
        if(details != null){
            details.put(key, value);
        }
    }

    public void addDetails(HashMap<String, String> newDetails){
        details.putAll(newDetails);
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
}
