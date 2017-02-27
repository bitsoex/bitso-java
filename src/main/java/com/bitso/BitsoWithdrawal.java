package com.bitso;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoWithdrawal {
    public String withdrawalId;
    public String status;
    public Date withdrawalDate;
    public String currency;
    public String method;
    public BigDecimal amount;
    public JSONObject details;

    public BitsoWithdrawal(JSONObject o){
        withdrawalId = Helpers.getString(o, "wid");
        status = Helpers.getString(o, "status");
        withdrawalDate = Helpers.getZonedDatetime(o, "created_at");
        currency = Helpers.getString(o, "currency");
        method = Helpers.getString(o, "method");
        amount = Helpers.getBD(o, "amount");
        details = o.getJSONObject("details");
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
