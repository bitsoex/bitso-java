package com.bitso.websockets;

import java.math.BigDecimal;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoStreamTrades extends BitsoStreamUpdate{
    private TradePayload[] payload;
    
    public BitsoStreamTrades(JSONObject jsonObject) {
        super(jsonObject);
        processPayload(jsonObject.getJSONArray("payload"));
    }

    public TradePayload[] getPayload() {
        return payload;
    }
    
    private void processPayload(JSONArray jsonArray){
        int totalElements = jsonArray.length();
        payload = new TradePayload[totalElements];
        for(int i=0; i<totalElements; i++){
            payload[i] = new TradePayload(jsonArray.getJSONObject(i));
        }
    }
    
    public boolean attributesNotNull(){
        if((bitsoChannel == null) || (bitsoBook == null) || !payloadNotNull()){
            return false;
        }
        return true;
    }
    
    private boolean payloadNotNull(){
        for (TradePayload tradePayload : payload) {
            if((tradePayload.mTradeId == -1) ||
                    (tradePayload.mAmount == null) ||
                    (tradePayload.mRate ==  null) ||
                    (tradePayload.mValue ==  null)){
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "New Trade: \n" + Arrays.toString(payload);
    }

    public class TradePayload{
        private int mTradeId;
        private BigDecimal mAmount;
        private BigDecimal mRate;
        private BigDecimal mValue;
        
        public TradePayload(JSONObject o){
            mTradeId = Helpers.getInt(o, "i");
            mAmount = new BigDecimal(String.valueOf(o.getDouble("a")));
            mRate = new BigDecimal(String.valueOf(o.getDouble("r")));
            mValue = new BigDecimal(String.valueOf(o.getDouble("v")));
        }

        public int getTradeId() {
            return mTradeId;
        }

        public BigDecimal getAmount() {
            return mAmount;
        }

        public BigDecimal getRate() {
            return mRate;
        }

        public BigDecimal getValue() {
            return mValue;
        }

        @Override
        public String toString() {
            return "Amount:" + mAmount + ", Rate:" + mRate
                    + ", Value=" + mValue;
        }
    }
}
