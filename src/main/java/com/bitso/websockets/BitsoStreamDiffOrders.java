package com.bitso.websockets;

import org.json.JSONArray;
import org.json.JSONObject;

public class BitsoStreamDiffOrders extends BitsoStreamUpdate{
    private JSONArray payload;
    
    public BitsoStreamDiffOrders(JSONObject jsonObject) {
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
