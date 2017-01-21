package com.bitso;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoTrade {
    public String book;
    public BigDecimal major;
    public ZonedDateTime tradeDate;
    public BigDecimal minor;
    public BigDecimal feesAmount;
    public String feesCurrency;
    public BigDecimal price;
    // TODO:
    // Check how long the tradeId would be, inr order
    // to save it in an int or long type
    public int tid;
    public String oid;
    public String side;

    public BitsoTrade(JSONObject o){
        book =  Helpers.getString(o, "book");
        major = Helpers.getBD(o, "major");
        tradeDate =  Helpers.getZonedDatetime(o, "created_at");
        minor =  Helpers.getBD(o, "minor");
        feesAmount = Helpers.getBD(o, "fees_amount");
        feesCurrency = Helpers.getString(o, "fees_currency");
        price = Helpers.getBD(o, "price");
        tid = Helpers.getInteger(o, "tid");
        oid = Helpers.getString(o, "oid");
        side = Helpers.getString(o, "side");
    }
    
    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
