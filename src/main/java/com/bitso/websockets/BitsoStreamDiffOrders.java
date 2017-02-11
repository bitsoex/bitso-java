package com.bitso.websockets;

import org.json.JSONArray;
import org.json.JSONObject;

public class BitsoStreamDiffOrders extends BitsoStreamUpdate{
protected BitsoWebSocketPublicOrder[] mPayload;
    
    public BitsoStreamDiffOrders(JSONObject jsonObject) {
        super(jsonObject);
        processPayload(jsonObject.getJSONArray("payload"));
    }

    public BitsoWebSocketPublicOrder[] getPayload() {
        return mPayload;
    }
    
    private void processPayload(JSONArray jsonArray){
        int totalElements = jsonArray.length();
        mPayload = new BitsoWebSocketPublicOrder[totalElements];
        for(int i=0; i<totalElements; i++){
            mPayload[i] = new BitsoWebSocketPublicOrder(jsonArray.getJSONObject(i));
        }
    }
    
    public boolean attributesNotNull(){
        if((bitsoChannel == null) || (bitsoBook == null) || !payloadNotNull()){
            return false;
        }
        return true;
    }

    protected boolean payloadNotNull(){
        for (BitsoWebSocketPublicOrder diffPayload : mPayload) {
            if((diffPayload.getOrderDate() == null) ||
                    (diffPayload.getRate() == null) ||
                    (diffPayload.getSide() ==  null) ||
                    (diffPayload.getAmount() ==  null) ||
                    (diffPayload.getValue() ==  null) ||
                    (diffPayload.getOrderId() ==  null)){
                return false;
            }
        }
        return true;
    }
}
