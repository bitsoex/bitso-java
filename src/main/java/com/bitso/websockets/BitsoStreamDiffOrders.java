package com.bitso.websockets;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoStreamDiffOrders extends BitsoStreamUpdate{
    private BitsoWebSocketPublicOrder[] mPayload;
    private int mSequenceNumber;
    
    public BitsoStreamDiffOrders(JSONObject jsonObject) {
        super(jsonObject);
        mSequenceNumber = Helpers.getInt(jsonObject, "sequence");
        processPayload(jsonObject.getJSONArray("payload"));
    }

    public BitsoWebSocketPublicOrder[] getPayload() {
        return mPayload;
    }
    
    public int getSequenceNumber(){
        return mSequenceNumber;
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

    @Override
    public String toString() {
        return "New Order: \n" + Arrays.toString(mPayload);
    }
}
