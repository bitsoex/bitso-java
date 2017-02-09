package com.bitso.websockets;

import org.json.JSONObject;

import com.bitso.BitsoBook;
import com.bitso.exceptions.BitsoExceptionNotExpectedValue;
import com.bitso.helpers.Helpers;

public class BitsoStreamUpdate {
    protected BitsoStreams bitsoStream;
    protected BitsoBook bitsoBook;

    public BitsoStreams getBitsoStream() {
        return bitsoStream;
    }
        
    public BitsoBook getBitsoBook() {
        return bitsoBook;
    }
    
    public BitsoStreamUpdate(JSONObject jsonObject){
        bitsoStream = getStream(Helpers.getString(jsonObject, "type"));
        bitsoBook = Helpers.getBook(Helpers.getString(jsonObject, "book"));
    }
    
    protected BitsoStreams getStream(String stream) {
        switch (stream) {
            case "diff-orders":
                return BitsoStreams.DIFF_ORDERS;
            case "orders":
                return BitsoStreams.ORDERS;
            case "trades":
                return BitsoStreams.TRADES;
            default:
                String exceptionMessage = stream + "is not a supported stream";
                throw new BitsoExceptionNotExpectedValue(exceptionMessage);
        }
    }
}
