package com.bitso;

import org.json.JSONArray;
import org.json.JSONObject;
import com.bitso.exchange.Funding;
import com.bitso.helpers.Helpers;

public class BitsoFunding {
    Funding[] fundings;

    public BitsoFunding(JSONObject o) {
        JSONArray fundingJson = o.getJSONArray("payload");
        fundings = retrieveFundings(fundingJson);
    }

    private Funding[] retrieveFundings(JSONArray array){
        int totalElements = array.length();
        Funding[] fundings = new Funding[totalElements];
        for(int i=0; i<totalElements; i++){
            fundings[i] =  new Funding(array.getJSONObject(i));
        }
        return fundings;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
