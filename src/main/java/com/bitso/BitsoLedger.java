package com.bitso;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exchange.Operation;
import com.bitso.helpers.Helpers;

public class BitsoLedger {
    Operation[] operations;

    public BitsoLedger(JSONObject o){
        JSONArray operationsJson = o.getJSONArray("payload");
        operations = retrieveOperations(operationsJson);
    }

    private Operation[] retrieveOperations(JSONArray array){
        int totalElements = array.length();
        Operation[] operations = new Operation[totalElements];
        for(int i=0; i<totalElements; i++){
            operations[i] =  new Operation(array.getJSONObject(i));
        }
        return operations;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
