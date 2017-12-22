package com.bitso.examples;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;

import javax.net.ssl.SSLException;

import org.json.JSONException;
import org.json.JSONObject;
import com.bitso.Bitso;
import com.bitso.BitsoOrder;
import com.bitso.BitsoOrderBook;
import com.bitso.BitsoOrderBook.PublicOrder;
import com.bitso.exceptions.BitsoAPIException;
import com.bitso.exceptions.BitsoPayloadException;
import com.bitso.exceptions.BitsoServerException;
import com.bitso.helpers.Helpers;
import com.bitso.websockets.BitsoChannels;
import com.bitso.websockets.BitsoStreamDiffOrders;
import com.bitso.websockets.BitsoWebSocket;
import com.bitso.websockets.BitsoWebSocketObserver;
import com.bitso.websockets.BitsoWebSocketPublicOrder;

public class BitsoWebSocketExample extends BitsoWebSocketObserver {
    private final String BTC_MXN_BOOK = "btc_mxn";

    private Operation mBids;
    private Operation mAsks;
    private BitsoOrderBook mLiveOrderBook;
    private Bitso mBitso;
    private int mCurrentSequenceNumber;
    private int mExpectedNewSequenceNumber;
    private boolean mCorrectSequenceNumber;
    private boolean mOrderBookObtained;

    public BitsoWebSocketExample() {
        mBids = new Operation();
        mAsks = new Operation();
        mCurrentSequenceNumber = 0;

        mExpectedNewSequenceNumber = 0;
        mCorrectSequenceNumber = Boolean.FALSE;
        mOrderBookObtained = Boolean.FALSE;
    }

    public void update(Observable o, Object arg) {
        // Channel message
        if (arg instanceof String) {
            String messageReceived = ((String) arg);
            JSONObject jsonObject = new JSONObject(messageReceived);

            // Discard response message of channel subscription or keep alive
            if (jsonObject.has("action") || (Helpers.getString(jsonObject, "type").equals("ka"))) {
                return;
            }

            BitsoStreamDiffOrders bitsoStreamDiffOrders = new BitsoStreamDiffOrders(jsonObject);

            if (mOrderBookObtained) {
                mExpectedNewSequenceNumber = mCurrentSequenceNumber + 1;
                mCorrectSequenceNumber = (bitsoStreamDiffOrders
                        .getSequenceNumber() == mExpectedNewSequenceNumber);
                if (mCorrectSequenceNumber) {
                    mergeOrders(bitsoStreamDiffOrders);
                    mCurrentSequenceNumber++;
                    printUpdate(bitsoStreamDiffOrders);
                } else {
                    getInitialOrderBook();
                }
            } else {
                mergeOrders(bitsoStreamDiffOrders);
                printUpdate(bitsoStreamDiffOrders);
            }
        }

        // On connect/disconnect
        if (arg instanceof Boolean) {
            mWSConnected = ((Boolean) arg);
            if (mWSConnected) {
                System.out.println("Web socket is now connected");
            } else {
                System.out.println("Web socket is now disconnected");
            }

        }
    }

    public void printUpdate(BitsoStreamDiffOrders diff) {
        System.out.println(diff);
        System.out.println("Best ask: " + mAsks.getMinPrice());
        System.out.println("Best bid: " + mBids.getMaxPrice());
    }

    public void mergeOrders(BitsoStreamDiffOrders bitsoStreamDiffOrders) {
        int streamSequenceNumber = bitsoStreamDiffOrders.getSequenceNumber();

        Operation streamUpdateOperation;

        BitsoWebSocketPublicOrder[] orders = bitsoStreamDiffOrders.getPayload();

        for (BitsoWebSocketPublicOrder bitsoWebSocketPublicOrder : orders) {
            if (bitsoWebSocketPublicOrder.getSide() == BitsoOrder.SIDE.BUY) {
                streamUpdateOperation = mBids;
            } else {
                streamUpdateOperation = mAsks;
            }

            BigDecimal currentPrice = bitsoWebSocketPublicOrder.getRate();
            BigDecimal currentAmount = bitsoWebSocketPublicOrder.getAmount();
            String currentOrderId = bitsoWebSocketPublicOrder.getOrderId();

            if (currentAmount.compareTo(new BigDecimal("0")) == 0) {
                streamUpdateOperation.removeOrder(currentOrderId);
                return;
            }

            OrderUpdate orderUpdate = new OrderUpdate(currentOrderId, currentPrice, currentAmount,
                    streamSequenceNumber);
            streamUpdateOperation.manageOrder(currentOrderId, orderUpdate);
        }
    }

    public void getInitialOrderBook() {
        // Public functions in API, no key or secret needed
        if (mBitso == null) {
            mBitso = new Bitso("", "", 0, Boolean.TRUE, Boolean.TRUE);
        }

        try {
            mLiveOrderBook = mBitso.getOrderBook(BTC_MXN_BOOK, Boolean.FALSE);

            mCurrentSequenceNumber = mLiveOrderBook.getSequence();
            mOrderBookObtained = Boolean.TRUE;

            for (PublicOrder publicOrder : mLiveOrderBook.getAsks()) {
                mAsks.manageOrder(publicOrder.getOrderId(), new OrderUpdate(publicOrder.getOrderId(),
                        publicOrder.getPrice(), publicOrder.getAmount(), mCurrentSequenceNumber));
            }

            for (PublicOrder publicOrder : mLiveOrderBook.getBids()) {
                mBids.manageOrder(publicOrder.getOrderId(), new OrderUpdate(publicOrder.getOrderId(),
                        publicOrder.getPrice(), publicOrder.getAmount(), mCurrentSequenceNumber));
            }

            mAsks.updateMaxMin(mCurrentSequenceNumber);
            mBids.updateMaxMin(mCurrentSequenceNumber);

            System.out.println("Best ask: " + mAsks.getMinPrice());
            System.out.println("Best bid: " + mBids.getMaxPrice());
        } catch (BitsoAPIException e) {
            e.printStackTrace();
        } catch (BitsoPayloadException e) {
            e.printStackTrace();
        } catch (BitsoServerException e) {
            e.printStackTrace();
        }
    }

    public class Operation {
        private HashMap<String, OrderUpdate> mOrderUpdatesMap;
        private BigDecimal mMinPrice;
        private BigDecimal mMaxPrice;

        public Operation() {
            mOrderUpdatesMap = new HashMap<String, OrderUpdate>();
            mMinPrice = new BigDecimal(0);
            mMaxPrice = new BigDecimal(0);
        }

        public void manageOrder(String orderId, OrderUpdate orderUpdate) {
            mOrderUpdatesMap.put(orderId, orderUpdate);
            updateMaxMin();
        }

        public void removeOrder(String orderId) {
            if (mOrderUpdatesMap.containsKey(orderId)) {
                BigDecimal price = mOrderUpdatesMap.get(orderId).getPrice();

                mOrderUpdatesMap.remove(orderId);

                if ((mMinPrice.compareTo(price) == 0) || (mMaxPrice.compareTo(price) == 0)) {
                    updateMaxMin();
                }
            }
        }

        public boolean orderExists(String orderId) {
            return mOrderUpdatesMap.containsKey(orderId);
        }

        public void updateMaxMin() {
            ArrayList<BigDecimal> publicOrders = getSortedOrders();
            if (publicOrders.size() > 0) {
                mMinPrice = publicOrders.get(0);
                mMaxPrice = publicOrders.get(publicOrders.size() - 1);
            }
        }

        public void updateMaxMin(int sequence) {
            ArrayList<BigDecimal> publicOrders = getSortedOrders(sequence);
            if (publicOrders.size() > 0) {
                mMinPrice = publicOrders.get(0);
                mMaxPrice = publicOrders.get(publicOrders.size() - 1);
            }
        }

        public ArrayList<BigDecimal> getSortedOrders() {
            ArrayList<BigDecimal> prices = new ArrayList<BigDecimal>();

            Iterator<Map.Entry<String, OrderUpdate>> it = mOrderUpdatesMap.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, OrderUpdate> entry = it.next();
                prices.add(entry.getValue().getPrice());
            }

            Collections.sort(prices);

            return prices;
        }

        public ArrayList<BigDecimal> getSortedOrders(int sequence) {
            ArrayList<BigDecimal> prices = new ArrayList<BigDecimal>();
            ArrayList<String> removableOrders = new ArrayList<String>();

            Iterator<Map.Entry<String, OrderUpdate>> it = mOrderUpdatesMap.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, OrderUpdate> entry = it.next();
                if (entry.getValue().getSequence() > sequence) {
                    prices.add(entry.getValue().getPrice());
                } else {
                    removableOrders.add(entry.getKey());
                }
            }

            // Clean map of orders with older sequence number
            for (String orderId : removableOrders) {
                mOrderUpdatesMap.remove(orderId);
            }

            Collections.sort(prices);

            return prices;
        }

        public BigDecimal getMinPrice() {
            return mMinPrice;
        }

        public BigDecimal getMaxPrice() {
            return mMaxPrice;
        }
    }

    public class OrderUpdate {
        private String mOrderId;
        private BigDecimal mPrice;
        private BigDecimal mAmount;
        private int mSequence;

        public OrderUpdate(String orderId, BigDecimal price, BigDecimal amount, int sequence) {
            mOrderId = orderId;
            mPrice = price;
            mAmount = amount;
            mSequence = sequence;
        }

        public OrderUpdate(String orderId, BigDecimal price, BigDecimal amount) {
            mOrderId = orderId;
            mPrice = price;
            mAmount = amount;
            mSequence = -1;
        }

        public int getSequence() {
            return mSequence;
        }

        public BigDecimal getPrice() {
            return mPrice;
        }

        public BigDecimal getAmount() {
            return mAmount;
        }

        public String getOrderId() {
            return mOrderId;
        }
    }

    public static void main(String args[]) throws SSLException, URISyntaxException, InterruptedException {
        final BitsoChannels[] bitsoChannels = { BitsoChannels.DIFF_ORDERS };

        BitsoWebSocket bitsoWebSocket = new BitsoWebSocket();
        BitsoWebSocketExample bitsoWebSocketExample = new BitsoWebSocketExample();

        bitsoWebSocket.addObserver(bitsoWebSocketExample);

        bitsoWebSocket.openConnection();

        for (BitsoChannels bitsoChannel : bitsoChannels) {
            bitsoWebSocket.subscribeBitsoChannel(bitsoChannel.toString());
        }

        bitsoWebSocketExample.getInitialOrderBook();

        Thread.sleep(50000);

        bitsoWebSocket.closeConnection();
    }
}
