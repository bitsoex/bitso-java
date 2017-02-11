package com.bitso.websockets;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONObject;

import com.bitso.BitsoOrder;
import com.bitso.helpers.Helpers;

public class BitsoWebSocketPublicOrder{
    private Date mOrderDate;
    private BigDecimal mRate;
    private BitsoOrder.SIDE mSide;
    private BigDecimal mAmount;
    private BigDecimal mValue;
    private String mOrderId;
    
    public BitsoWebSocketPublicOrder(JSONObject jsonObject){
        mOrderDate = new java.util.Date(jsonObject.getLong("d")*1000);
        mRate = new BigDecimal(String.valueOf(jsonObject.getDouble("r")));
        mSide = (Helpers.getInt(jsonObject, "t") == 1) ?
                BitsoOrder.SIDE.SELL : BitsoOrder.SIDE.BUY;
        if(jsonObject.has("a") && jsonObject.has("v")){
            mAmount = new BigDecimal(String.valueOf(jsonObject.getDouble("a")));
            mValue = new BigDecimal(String.valueOf(jsonObject.getDouble("v")));
        }else{
            mAmount = new BigDecimal("0");
            mValue = new BigDecimal("0");
        }
        if(jsonObject.has("o")){
            mOrderId = Helpers.getString(jsonObject, "o");
        }
    }

    public Date getOrderDate() {
        return mOrderDate;
    }

    public BigDecimal getRate() {
        return mRate;
    }

    public BitsoOrder.SIDE getSide() {
        return mSide;
    }

    public BigDecimal getAmount() {
        return mAmount;
    }

    public BigDecimal getValue() {
        return mValue;
    }

    public String getOrderId() {
        return mOrderId;
    }
}