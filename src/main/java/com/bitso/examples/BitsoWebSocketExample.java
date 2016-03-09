package com.bitso.examples;

import java.util.Observable;
import java.util.Observer;

import com.bitso.websockets.BitsoWebSocket;
import com.bitso.websockets.Channels;

public class BitsoWebSocketExample implements Observer {
    BitsoWebSocket ws;

    public BitsoWebSocketExample() {
        Channels[] subscribeTo = { Channels.TRADES, Channels.DIFF_ORDERS, Channels.ORDERS };
        ws = new BitsoWebSocket(subscribeTo);
        ws.addObserver(this);
        ws.connect();
    }

    @Override
    public void update(Observable o, Object arg) {
        String message = arg.toString();
        if (message.equals("disconnected")) {
            System.out.println("The websocket got disconnected, reconnecting.");
            ws.connect();
        }
        System.out.println("got a message: " + arg);
    }

    public static void main(String[] args) {
        BitsoWebSocketExample wsExample = new BitsoWebSocketExample();
    }
}
