package com.bitso;

import org.json.JSONArray;
import org.json.JSONObject;
import com.bitso.exchange.Trade;
import com.bitso.helpers.Helpers;

public class BitsoTrade {
    Trade[] trades;
    
    public BitsoTrade(JSONObject o){
        JSONArray tradesJson = o.getJSONArray("payload");
        retrieveTrades(tradesJson);
    }
    
    private void retrieveTrades(JSONArray array){
        int totalElements = array.length();
        trades = new Trade[totalElements];
        for(int i=0; i<totalElements; i++){
            trades[i] =  new Trade(array.getJSONObject(i));
        }
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
