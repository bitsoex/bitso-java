package com.bitso.examples;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;

import javax.net.ssl.SSLException;
import org.json.JSONObject;
import com.bitso.Bitso;
import com.bitso.BitsoBook;
import com.bitso.BitsoOrder;
import com.bitso.BitsoOrderBook;
import com.bitso.BitsoOrderBook.PulicOrder;
import com.bitso.helpers.Helpers;
import com.bitso.websockets.BitsoChannels;
import com.bitso.websockets.BitsoStreamDiffOrders;
import com.bitso.websockets.BitsoStreamTrades;
import com.bitso.websockets.BitsoWebSocket;
import com.bitso.websockets.BitsoWebSocketObserver;
import com.bitso.websockets.BitsoWebSocketPublicOrder;

public class BitsoWebSocketExample extends BitsoWebSocketObserver {

    private SocketUpdates mBids;
    private SocketUpdates mAsks;
    private BitsoOrderBook mLiveOrderBook;
    private Bitso mBitso;
    private int mCurrentSequenceNumber;
    private int mExpectedNewSequenceNumber;
    private boolean mCorrectSequenceNumber;
    private boolean mOrderBookObtained;

    public BitsoWebSocketExample() {
        mBids = new SocketUpdates();
        mAsks = new SocketUpdates();
        mCurrentSequenceNumber = 0;
        
        mExpectedNewSequenceNumber = 0;
        mCorrectSequenceNumber = Boolean.FALSE;
        mOrderBookObtained = Boolean.FALSE;
    }

    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            String messageReceived = ((String) arg);
            JSONObject jsonObject = new JSONObject(messageReceived);

            if (jsonObject.has("action")) {
                return;
            }

            String type = Helpers.getString(jsonObject, "type");
            switch (type) {
                case "trades":
                    BitsoStreamTrades trades = new BitsoStreamTrades(jsonObject);
                    System.out.println(trades);
                    break;
                case "diff-orders":
                    BitsoStreamDiffOrders diff = new BitsoStreamDiffOrders(jsonObject);
                    if(mOrderBookObtained){
                        mExpectedNewSequenceNumber = mCurrentSequenceNumber + 1;
                        mCorrectSequenceNumber = (diff.getSequenceNumber() == mExpectedNewSequenceNumber);
                        if(mCorrectSequenceNumber){
                            mergeOrders(diff);
                            mCurrentSequenceNumber++;
                            printUpdate(diff);
                        }else{
                            getInitialOrderBook();
                        }
                    }else{
                        mergeOrders(diff);
                        printUpdate(diff);
                    }
                    break;
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

    public void printUpdate(BitsoStreamDiffOrders diff){
        System.out.println(diff);
        System.out.println("Best ask: " + mAsks.getMinPrice());
        System.out.println("Best bid: " + mBids.getMaxPrice());
    }
    
    public void mergeOrders(BitsoStreamDiffOrders diff) {
        int diffSequence = diff.getSequenceNumber();

        SocketUpdates socketUpdates;

        BitsoWebSocketPublicOrder[] orders = diff.getPayload();

        for (BitsoWebSocketPublicOrder bitsoWebSocketPublicOrder : orders) {
            if (bitsoWebSocketPublicOrder.getSide() == BitsoOrder.SIDE.BUY) {
                socketUpdates = mBids;
            } else {
                socketUpdates = mAsks;
            }

            BigDecimal currentPrice = bitsoWebSocketPublicOrder.getRate();
            BigDecimal currentAmount = bitsoWebSocketPublicOrder.getAmount();

            if (currentAmount.compareTo(new BigDecimal("0")) == 0) {
                socketUpdates.removePrice(currentPrice);
                return;
            }

            SocketUpdatesOperation operation = new SocketUpdatesOperation(currentPrice, currentAmount,
                    diffSequence);

            if (socketUpdates.priceExists(currentPrice)) {
                socketUpdates.updatePrice(currentPrice, operation);
            } else {
                socketUpdates.insertPrice(currentPrice, operation);
            }
        }
    }

    public void getInitialOrderBook() {
        // Public functions in API, no key or secret needed
        if (mBitso == null) {
            mBitso = new Bitso("", "", 0, Boolean.TRUE, Boolean.TRUE);
        }

        mLiveOrderBook = mBitso.getOrderBook(BitsoBook.BTC_MXN);
        mCurrentSequenceNumber = mLiveOrderBook.sequence;
        mOrderBookObtained = Boolean.TRUE;

        for (PulicOrder publicOrder : mLiveOrderBook.asks) {
            mAsks.insertPrice(publicOrder.mPrice,
                    new SocketUpdatesOperation(publicOrder.mPrice, publicOrder.mAmount));
        }

        for (PulicOrder publicOrder : mLiveOrderBook.bids) {
            mBids.insertPrice(publicOrder.mPrice,
                    new SocketUpdatesOperation(publicOrder.mPrice, publicOrder.mAmount));
        }

        mAsks.updateMaxMin(mCurrentSequenceNumber);
        mBids.updateMaxMin(mCurrentSequenceNumber);

        System.out.println("Best ask: " + mAsks.getMinPrice());
        System.out.println("Best bid: " + mBids.getMaxPrice());
    }

    public class SocketUpdates {
        private HashMap<BigDecimal, SocketUpdatesOperation> mPriceMap;
        private BigDecimal mMinPrice;
        private BigDecimal mMaxPrice;

        public SocketUpdates() {
            mPriceMap = new HashMap<BigDecimal, SocketUpdatesOperation>();
            mMinPrice = new BigDecimal(0);
            mMaxPrice = new BigDecimal(0);
        }

        public void insertPrice(BigDecimal price, SocketUpdatesOperation operation) {
            mPriceMap.put(price, operation);
            updateMaxMin();
        }

        public void updatePrice(BigDecimal price, SocketUpdatesOperation operation) {
            mPriceMap.put(price, operation);
            updateMaxMin();
        }

        public void removePrice(BigDecimal price) {
            mPriceMap.remove(price);

            if ((mMinPrice.compareTo(price) == 0) || (mMaxPrice.compareTo(price) == 0)) {
                updateMaxMin();
            }
        }

        public boolean priceExists(BigDecimal price) {
            return mPriceMap.containsKey(price);
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

            Iterator<Map.Entry<BigDecimal, SocketUpdatesOperation>> it = mPriceMap.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<BigDecimal, SocketUpdatesOperation> entry = it.next();
                prices.add(entry.getKey());
            }

            Collections.sort(prices);

            return prices;
        }

        public ArrayList<BigDecimal> getSortedOrders(int sequence) {
            ArrayList<BigDecimal> prices = new ArrayList<BigDecimal>();
            ArrayList<BigDecimal> olderPrices = new ArrayList<BigDecimal>();

            Iterator<Map.Entry<BigDecimal, SocketUpdatesOperation>> it = mPriceMap.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<BigDecimal, SocketUpdatesOperation> entry = it.next();
                if (entry.getValue().getSequence() > sequence) {
                    prices.add(entry.getKey());
                } else {
                    olderPrices.add(entry.getKey());
                }
            }

            // Clean map of orders with older sequence number
            for (BigDecimal price : olderPrices) {
                mPriceMap.remove(price);
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

    public class SocketUpdatesOperation {
        private BigDecimal mPrice;
        private BigDecimal mAmount;
        private int mSequence;

        public SocketUpdatesOperation(BigDecimal price, BigDecimal amount, int sequence) {
            mPrice = price;
            mAmount = amount;
            mSequence = sequence;
        }

        public SocketUpdatesOperation(BigDecimal price, BigDecimal amount) {
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
    }

    public static void main(String args[]) throws SSLException, URISyntaxException, InterruptedException {
        final BitsoChannels[] bitsoChannels = { BitsoChannels.TRADES, BitsoChannels.DIFF_ORDERS };

        BitsoWebSocket bitsoWebSocket = new BitsoWebSocket();
        BitsoWebSocketExample bitsoWebSocketExample = new BitsoWebSocketExample();

        bitsoWebSocket.addObserver(bitsoWebSocketExample);

        bitsoWebSocket.openConnection();

        for (BitsoChannels bitsoChannel : bitsoChannels) {
            bitsoWebSocket.subscribeBitsoChannel(bitsoChannel.toString());
        }

        bitsoWebSocketExample.getInitialOrderBook();

        Thread.sleep(50_000);

        bitsoWebSocket.closeConnection();
    }
}
