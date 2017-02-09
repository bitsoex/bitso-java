package com.bitso;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.net.ssl.SSLException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.bitso.exceptions.BitsoExceptionNotExpectedValue;
import com.bitso.helpers.Helpers;
import com.bitso.websockets.BitsoStreamDiffOrders;
import com.bitso.websockets.BitsoStreamOrders;
import com.bitso.websockets.BitsoStreamTrades;
import com.bitso.websockets.BitsoWebSocket;
import com.bitso.websockets.BitsoWebSocketObserver;
import com.bitso.websockets.Channels;

public class BitsoWebSocketTest {
    private final Channels[] channels = { Channels.TRADES, Channels.DIFF_ORDERS, Channels.ORDERS };
    BitsoWebSocket bitsoWebSocket;
    BitsoWebSocketObserver bitsoWebSocketObserver;
    
    @Before
    public void setUp() throws Exception{
        bitsoWebSocket = new BitsoWebSocket(channels);
        bitsoWebSocketObserver = new BitsoWebSocketObserver();
        bitsoWebSocket.addObserver(bitsoWebSocketObserver);
    }

    @Test
    public void testWebSocket() throws SSLException, URISyntaxException, InterruptedException {
        ArrayList<String> receivedMessages;
        int totalMessagesReceived = 0;
        String action = "";
        String response = "";

        bitsoWebSocket.connect();

        Thread.sleep(20000);

        bitsoWebSocket.close();

        receivedMessages = bitsoWebSocketObserver.getStreamUpdates();
        totalMessagesReceived = receivedMessages.size();

        // Check channel subscription
        for (int i = 0; i < 3; i++) {
            JSONObject jsonObject = new JSONObject(receivedMessages.get(i));
            action = Helpers.getString(jsonObject, "action");
            response = Helpers.getString(jsonObject, "response");
            assertEquals((action.equals("subscribe") && response.equals("ok")), true);
        }

        // Check stream update messages are correct
        for (int i = 3; i < totalMessagesReceived; i++) {
            JSONObject jsonObject = new JSONObject(receivedMessages.get(i));
            String type = Helpers.getString(jsonObject, "type");
            switch (type) {
                case "trades":
                    BitsoStreamTrades trades = new BitsoStreamTrades(jsonObject);
                    assertEquals((trades != null), true);
                    assertEquals(trades.attributesNotNull(), true);
                    break;
                case "diff-orders":
                    BitsoStreamDiffOrders diff = new BitsoStreamDiffOrders(jsonObject);
                    assertEquals((diff != null), true);
                    assertEquals(diff.attributesNotNull(), true);
                    break;
                case "orders":
                    BitsoStreamOrders orders = new BitsoStreamOrders(jsonObject);
                    assertEquals(orders != null, true);
                    assertEquals(orders.attributesNotNull(), true);
                    break;
                case "ka":
                    break;
                default:
                    String exceptionMessage = type + "is not a supported stream type";
                    throw new BitsoExceptionNotExpectedValue(exceptionMessage);
            }
        }
    }
}
