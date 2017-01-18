package com.bitso.exchange;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class Withdrawal {
    public String withdrawalId;
    public String status;
    public ZonedDateTime withdrawalDate;
    public String currency;
    public String method;
    public BigDecimal amount;
    public JSONObject details;

    public Withdrawal(JSONObject o){
        withdrawalId = Helpers.getString(o, "wid");
        status = Helpers.getString(o, "status");
        withdrawalDate = Helpers.getZonedDatetime(o, "created_at");
        currency = Helpers.getString(o, "currency");
        method = Helpers.getString(o, "method");
        amount = Helpers.getBD(o, "amount");

        // TODO:
        // Expected always a JSONObject, sometimes
        // JSONArray is obtained
        details = Helpers.expectJSONObject(o, "details", this);
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
