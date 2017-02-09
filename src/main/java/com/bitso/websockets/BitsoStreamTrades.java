package com.bitso.websockets;

import org.json.JSONArray;
import org.json.JSONObject;

public class BitsoStreamTrades extends BitsoStreamUpdate{
    private JSONArray payload;
    
    public BitsoStreamTrades(JSONObject jsonObject) {
        super(jsonObject);
        payload = jsonObject.getJSONArray("payload");
    }

    public JSONArray getPayload() {
        return payload;
    }
    
    public boolean attributesNotNull(){
        if((bitsoStream == null) || (bitsoBook == null) || (payload == null)){
            return false;
        }
        return true;
    }
}
