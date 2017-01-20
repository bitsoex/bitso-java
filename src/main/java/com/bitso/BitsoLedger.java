package com.bitso;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoLedger {
    BitsoOperation[] operations;

    public BitsoLedger(JSONObject o){
        JSONArray operationsJson = o.getJSONArray("payload");
        operations = retrieveOperations(operationsJson);
    }

    private BitsoOperation[] retrieveOperations(JSONArray array){
        int totalElements = array.length();
        BitsoOperation[] operations = new BitsoOperation[totalElements];
        for(int i=0; i<totalElements; i++){
            operations[i] =  new BitsoOperation(array.getJSONObject(i));
        }
        return operations;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
