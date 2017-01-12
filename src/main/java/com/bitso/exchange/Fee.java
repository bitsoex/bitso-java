package com.bitso.exchange;

import java.math.BigDecimal;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class Fee {
    String book;
    BigDecimal feeDecimal;
    BigDecimal feePercent;
    
    public Fee(String book, BigDecimal feeDecimal, BigDecimal feePercent) {
        this.book = book;
        this.feeDecimal = feeDecimal;
        this.feePercent = feePercent;
    }
    
    public Fee(JSONObject o){
        this.book = Helpers.getString(o, "book");
        this.feeDecimal = Helpers.getBD(o, "fee_decimal");
        this.feePercent =  Helpers.getBD(o, "fee_percent");
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}