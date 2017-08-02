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
        bitsoChannel = getStream(Helpers.getString(jsonObject, "type"));
        bitsoBook = Helpers.getString(jsonObject, "book");
    }
    
    protected BitsoChannels getStream(String stream) {
        switch (stream) {
            case "diff-orders":
                return BitsoChannels.DIFF_ORDERS;
            case "orders":
                return BitsoChannels.ORDERS;
            case "trades":
                return BitsoChannels.TRADES;
            default:
                String exceptionMessage = stream + "is not a supported stream";
                throw new BitsoExceptionNotExpectedValue(exceptionMessage);
        }
    }
}
