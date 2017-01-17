package com.bitso.exchange;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class Trade {    
    public String book;
    public BigDecimal major;
    public ZonedDateTime tradeDate;
    public BigDecimal minor;
    public BigDecimal feesAmount;
    public String feesCurrency;
    public BigDecimal price;
  //TODO: API returns an int, String is expected
    public int tradeId;
    public String oid;
    public String side;

    public Trade(JSONObject o){
        book =  Helpers.getString(o, "book");
        major = Helpers.getBD(o, "major");
        tradeDate =  Helpers.getZonedDatetime(Helpers.getString(o, "created_at"));
        minor =  Helpers.getBD(o, "minor");
        feesAmount = Helpers.getBD(o, "fees_amount");
        feesCurrency = Helpers.getString(o, "fees_currency");
        price = Helpers.getBD(o, "price");
        tradeId = Helpers.getInteger(o, "tid");
        oid = Helpers.getString(o, "oid");
        side = Helpers.getString(o, "side");
    }
    
    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
