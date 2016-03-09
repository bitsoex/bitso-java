package com.bitso.examples;

import java.util.Observable;
import java.util.Observer;

import com.bitso.websockets.BitsoWebSocket;
import com.bitso.websockets.Channels;

public class BitsoWebSocketExample implements Observer {

    public BitsoWebSocketExample() {
        Channels[] subscribeTo = { Channels.TRADES, Channels.DIFF_ORDERS, Channels.ORDERS };
        BitsoWebSocket ws = new BitsoWebSocket(subscribeTo);
        ws.addObserver(this);
        ws.connect();
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("got a message: " + arg);

    }

    public static void main(String[] args) {
        BitsoWebSocketExample wsExample = new BitsoWebSocketExample();
    }
}
