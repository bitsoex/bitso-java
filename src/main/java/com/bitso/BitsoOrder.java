package com.bitso;

import org.json.JSONArray;
import org.json.JSONObject;
import com.bitso.exchange.Order;
import com.bitso.helpers.Helpers;

public class BitsoOrder{
    Order[] orders;

    public BitsoOrder(JSONObject o){
        JSONArray ordersJson = o.getJSONArray("payload");
        retrieveOrders(ordersJson);
    }

    private void retrieveOrders(JSONArray array){
        int totalElements = array.length();
        orders = new Order[totalElements];
        for(int i=0; i<totalElements; i++){
            orders[i] =  new Order(array.getJSONObject(i));
        }
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
