package com.bitso.exchange;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class Funding {
    public String fundingId;
    public String status;
    public ZonedDateTime fundingDate;
    public String currency;
    public String method;
    public BigDecimal amount;
    public JSONObject details;

    public Funding(JSONObject o){
        fundingId = Helpers.getString(o, "fid");
        status = Helpers.getString(o, "status");
        fundingDate = Helpers.getZonedDatetime(o, "created_at");
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
