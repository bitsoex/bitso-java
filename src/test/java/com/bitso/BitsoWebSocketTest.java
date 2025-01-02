package com.bitso;

import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.net.ssl.SSLException;

import org.json.JSONObject;

import com.bitso.exceptions.BitsoExceptionNotExpectedValue;
import com.bitso.helpers.Helpers;
import com.bitso.websockets.BitsoStreamDiffOrders;
import com.bitso.websockets.BitsoStreamOrders;
import com.bitso.websockets.BitsoStreamTrades;
import com.bitso.websockets.BitsoWebSocket;
import com.bitso.websockets.BitsoWebSocketObserver;
import com.bitso.websockets.BitsoChannels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BitsoWebSocketTest {
    private final BitsoChannels[] bitsoChannels = { BitsoChannels.TRADES, BitsoChannels.DIFF_ORDERS,
            BitsoChannels.ORDERS };
    private BitsoWebSocket bitsoWebSocket;
    private BitsoWebSocketObserver bitsoWebSocketObserver;

    @BeforeEach
    public void setUp() throws Exception {
        bitsoWebSocket = new BitsoWebSocket();
        bitsoWebSocketObserver = new BitsoWebSocketObserver();
        bitsoWebSocket.addObserver(bitsoWebSocketObserver);
    }

    @Test
    public void testWebSocket() throws SSLException, URISyntaxException, InterruptedException {
        ArrayList<String> receivedMessages;
        int totalMessagesReceived = 0;
        String action = "";
        String response = "";

        bitsoWebSocket.openConnection();
        for (BitsoChannels bitsoChannel : bitsoChannels) {
            bitsoWebSocket.subscribeBitsoChannel(bitsoChannel.toString());
        }

        Thread.sleep(20000);

        bitsoWebSocket.closeConnection();

        receivedMessages = bitsoWebSocketObserver.getMessagesReceived();
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
            BitsoChannels channel = BitsoChannels.getBitsoChannel(type);

            assertEquals(true, (channel != null));

            switch (channel) {
                case TRADES:
                    BitsoStreamTrades trades = new BitsoStreamTrades(jsonObject);
                    assertEquals((trades != null), true);
                    assertEquals(trades.attributesNotNull(), true);
                    break;
                case DIFF_ORDERS:
                    BitsoStreamDiffOrders diff = new BitsoStreamDiffOrders(jsonObject);
                    assertEquals((diff != null), true);
                    assertEquals(diff.attributesNotNull(), true);
                    break;
                case ORDERS:
                    BitsoStreamOrders orders = new BitsoStreamOrders(jsonObject);
                    assertEquals(orders != null, true);
                    assertEquals(orders.attributesNotNull(), true);
                    break;
                case KA:
                    break;
                default:
                    String exceptionMessage = type + "is not a supported stream type";
                    throw new BitsoExceptionNotExpectedValue(exceptionMessage);
            }
        }
    }
}
