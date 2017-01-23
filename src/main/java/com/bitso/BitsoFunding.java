package com.bitso;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoFunding {
    public String fundingId;
    public String status;
    public ZonedDateTime fundingDate;
    public String currency;
    public String method;
    public BigDecimal amount;
    public JSONObject details;

    public BitsoFunding(JSONObject o){
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
}
