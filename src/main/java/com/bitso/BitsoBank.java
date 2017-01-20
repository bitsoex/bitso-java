package com.bitso;

import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoBank {
    public String code;
    public String name;
    
    public BitsoBank(JSONObject o){
        code = Helpers.getString(o, "code");
        name = Helpers.getString(o, "name");
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
