package com.bitso.websockets;

import org.json.JSONObject;

public class BitsoStreamOrders extends BitsoStreamUpdate{
    private JSONObject payload;
    
    public BitsoStreamOrders(JSONObject jsonObject) {
        super(jsonObject);
        payload = jsonObject.getJSONObject("payload");
    }

    public JSONObject getPayload() {
        return payload;
    }
    
    public boolean attributesNotNull(){
        if((bitsoStream == null) || (bitsoBook == null) || (payload == null)){
            return false;
        }
        return true;
    }
}
