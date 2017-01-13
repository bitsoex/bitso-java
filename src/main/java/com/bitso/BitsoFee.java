package com.bitso;

import org.json.JSONArray;
import org.json.JSONObject;
import com.bitso.exchange.Fee;
import com.bitso.helpers.Helpers;

public class BitsoFee {
    
    Fee fees[];
    
    public BitsoFee(JSONObject obj) {
        JSONObject payload = obj.getJSONObject("payload");
        JSONArray jsonFees = payload.getJSONArray("fees");
        fees = retrieveFees(jsonFees);
    }
    
    private Fee[] retrieveFees(JSONArray array){
        int totalElements = array.length();
        Fee fees[] = new Fee[totalElements];
        for(int i=0; i<totalElements; i++){
            fees[i] = new Fee(array.getJSONObject(i));
        }
        return fees;
    }
    
    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
