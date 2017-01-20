package com.bitso;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoOrderS{
    BitsoOrder[] orders;

    public BitsoOrderS(JSONObject o){
        JSONArray ordersJson = o.getJSONArray("payload");
        retrieveOrders(ordersJson);
    }

    private void retrieveOrders(JSONArray array){
        int totalElements = array.length();
        orders = new BitsoOrder[totalElements];
        for(int i=0; i<totalElements; i++){
            orders[i] =  new BitsoOrder(array.getJSONObject(i));
        }
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
