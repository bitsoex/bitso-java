package com.bitso.websockets;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONObject;

public class BitsoWebSocketObserver implements Observer{
    
    private ArrayList<String> mStreamUpdates;

    public BitsoWebSocketObserver() {
        mStreamUpdates = new ArrayList<String>();
    }
    
    @Override
    public void update(Observable o, Object arg) {
        String response =  (String) arg;
        System.out.println(response);
        mStreamUpdates.add(response);
    }

    public ArrayList<String> getStreamUpdates() {
        return mStreamUpdates;
    }
}