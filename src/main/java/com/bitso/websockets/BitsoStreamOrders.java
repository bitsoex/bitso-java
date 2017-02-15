package com.bitso.websockets;

import org.json.JSONArray;
import org.json.JSONObject;

public class BitsoStreamOrders extends BitsoStreamUpdate{
    protected BitsoWebSocketPublicOrder[] mBids;
    protected BitsoWebSocketPublicOrder[] mAsks;
    
    public BitsoStreamOrders(JSONObject jsonObject) {
        super(jsonObject);
        processPayload(jsonObject.getJSONObject("payload"));
    }
    
    public BitsoWebSocketPublicOrder[] getmBids() {
        return mBids;
    }

    public BitsoWebSocketPublicOrder[] getmAsks() {
        return mAsks;
    }

    private void processPayload(JSONObject jsonObject){
        // Process Bids
        JSONArray jsonArrayBids = jsonObject.getJSONArray("bids");
        int totalBids = jsonArrayBids.length();
        mBids = new BitsoWebSocketPublicOrder[totalBids];
        processBids(jsonArrayBids, totalBids);
        
        // Process Asks
        JSONArray jsonArrayAsks = jsonObject.getJSONArray("asks");
        int totalAsks = jsonArrayAsks.length();
        mAsks = new BitsoWebSocketPublicOrder[totalAsks];
        processAsks(jsonArrayAsks, totalAsks);
    }
    
    private void processBids(JSONArray jsonArray, int totalElements){
        for(int i=0; i<totalElements; i++){
            mBids[i] = new BitsoWebSocketPublicOrder(jsonArray.getJSONObject(i));
        }
    }
    
    private void processAsks(JSONArray jsonArray, int totalElements){
        for(int i=0; i<totalElements; i++){
            mAsks[i] = new BitsoWebSocketPublicOrder(jsonArray.getJSONObject(i));
        }
    }
    
    public boolean attributesNotNull(){
        if((bitsoChannel == null) || (bitsoBook == null) || !payloadNotNull()){
            return false;
        }
        return true;
    }
    
    protected boolean payloadNotNull(){
        for (BitsoWebSocketPublicOrder diffPayload : mBids) {
            if((diffPayload.getOrderDate() == null) ||
                    (diffPayload.getRate() == null) ||
                    (diffPayload.getSide() ==  null) ||
                    (diffPayload.getAmount() ==  null) ||
                    (diffPayload.getValue() ==  null)){
                return false;
            }
        }
        
        for (BitsoWebSocketPublicOrder diffPayload : mAsks) {
            if((diffPayload.getOrderDate() == null) ||
                    (diffPayload.getRate() == null) ||
                    (diffPayload.getSide() ==  null) ||
                    (diffPayload.getAmount() ==  null) ||
                    (diffPayload.getValue() ==  null)){
                return false;
            }
        }
        return true;
    }
}
