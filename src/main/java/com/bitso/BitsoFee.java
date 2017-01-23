package com.bitso;

import java.math.BigDecimal;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoFee {
    public String book;
    public BigDecimal feeDecimal;
    public BigDecimal feePercent;
    
    public BitsoFee(String book, BigDecimal feeDecimal, BigDecimal feePercent) {
        this.book = book;
        this.feeDecimal = feeDecimal;
        this.feePercent = feePercent;
    }
    
    public BitsoFee(JSONObject o){
        this.book = Helpers.getString(o, "book");
        this.feeDecimal = Helpers.getBD(o, "fee_decimal");
        this.feePercent =  Helpers.getBD(o, "fee_percent");
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}