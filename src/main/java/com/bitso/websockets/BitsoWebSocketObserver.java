package com.bitso.websockets;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class BitsoWebSocketObserver implements Observer{
    private ArrayList<String> mMessagesReceived;
    protected Boolean mWSConnected;

    public BitsoWebSocketObserver() {        
        this.mMessagesReceived = new ArrayList<String>();
        this.mWSConnected = Boolean.FALSE;
    }

    public void update(Observable o, Object arg) {
        // Update message
        if(arg instanceof String){
            String messageReceived = ((String) arg);
            System.out.println(messageReceived);
            mMessagesReceived.add(messageReceived);
        }
        
        // On connect/disconnect
        if(arg instanceof Boolean){
            mWSConnected = ((Boolean) arg);
            if(mWSConnected){
                System.out.println("Web socket is now connected");
            }else{
                System.out.println("Web socket is now disconnected");
            }
            
        }
    }
    
    public ArrayList<String> getMessagesReceived(){
        return mMessagesReceived;
    }
    
    public Boolean isWSConnected(){
        return mWSConnected;
    }
}