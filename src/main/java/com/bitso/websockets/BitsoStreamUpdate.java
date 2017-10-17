package com.bitso.websockets;

import org.json.JSONObject;

import com.bitso.exceptions.BitsoExceptionNotExpectedValue;
import com.bitso.helpers.Helpers;

public class BitsoStreamUpdate {
    protected BitsoChannels bitsoChannel;
    protected String bitsoBook;

    public BitsoChannels getBitsoChannel() {
        return bitsoChannel;
    }
        
    public String getBitsoBook() {
        return bitsoBook;
    }
    
    public BitsoStreamUpdate(JSONObject jsonObject){
        bitsoChannel = BitsoChannels.getBitsoChannel(Helpers.getString(jsonObject, "type"));
        bitsoBook = Helpers.getString(jsonObject, "book");
    }
}
