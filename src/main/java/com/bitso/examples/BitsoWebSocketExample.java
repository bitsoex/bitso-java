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

public class BitsoWebSocketExample extends BitsoWebSocketObserver{
    
    private OrderStructure mBids;
    private OrderStructure mAsks;
    
    private BitsoOrderBook mLiveOrderBook;
    private Bitso mBitso;
        
    public void update(Observable o, Object arg) {
        // Update message
        if(arg instanceof String){
            String messageReceived = ((String) arg);
            JSONObject jsonObject = new JSONObject(messageReceived);
            if(!jsonObject.has("action")){
                String type = Helpers.getString(jsonObject, "type");
                switch (type) {
                    case "trades":
                        BitsoStreamTrades trades = new BitsoStreamTrades(jsonObject);
                        System.out.println(trades);
                        break;
                    case "diff-orders":
                        BitsoStreamDiffOrders diff = new BitsoStreamDiffOrders(jsonObject);
                        System.out.println(diff);
                        mergeOrders(diff);
                        System.out.println("Best ask: " + mAsks.getMinPrice());
                        System.out.println("Best bid: " + mBids.getMaxPrice());
                        break;
                }
            }
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
    
    public void mergeOrders(BitsoStreamDiffOrders diff){
        OrderStructure orderStructure;
        BitsoWebSocketPublicOrder[] bids = diff.getPayload();
        for (BitsoWebSocketPublicOrder bitsoWebSocketPublicOrder : bids) {
            if(bitsoWebSocketPublicOrder.getSide().compareTo(BitsoOrder.SIDE.BUY) == 0){
                orderStructure = mBids;
            }else{
                orderStructure = mAsks;
            }
            
            BigDecimal currentPrice = bitsoWebSocketPublicOrder.getRate();
            BigDecimal currentAmount = bitsoWebSocketPublicOrder.getAmount();
            
            if(currentAmount.compareTo(new BigDecimal("0")) == 0){
                orderStructure.removePrice(currentPrice);
            }else{
                if(orderStructure.priceExists(currentPrice)){
                    orderStructure.updatePrice(currentPrice, currentAmount);
                }else{
                    orderStructure.insertPrice(currentPrice, currentAmount);
                }
            }
        }
    }
    
    public void getInitialOrderBook(){
        // Public functions in API, no key or secret needed
        mBitso = new Bitso("", "", 0, Boolean.TRUE, Boolean.TRUE);
        
        mLiveOrderBook = mBitso.getOrderBook(BitsoBook.BTC_MXN);
        
        mBids = new OrderStructure();
        mAsks = new OrderStructure();
        
        for (PulicOrder publicOrder : mLiveOrderBook.asks) {
            mAsks.insertPrice(publicOrder.mPrice, publicOrder.mAmount);
        }
        
        for (PulicOrder publicOrder : mLiveOrderBook.bids) {
            mBids.insertPrice(publicOrder.mPrice, publicOrder.mAmount);
        }
        
        mAsks.updateMaxMin();
        mBids.updateMaxMin();
        
        System.out.println("Best ask: " + mAsks.getMinPrice());
        System.out.println("Best bid: " + mBids.getMaxPrice());
    }
    
    public class OrderStructure{
        private HashMap<BigDecimal, BigDecimal> mPriceMap;
        private BigDecimal mMinPrice;
        private BigDecimal mMaxPrice;

        public OrderStructure(){
            mPriceMap = new HashMap<BigDecimal, BigDecimal>();
            mMinPrice = new BigDecimal(0);
            mMaxPrice = new BigDecimal(0);
        }
        
        public void insertPrice(BigDecimal price, BigDecimal amount){
            mPriceMap.put(price, amount);
            
            if(mMinPrice.compareTo(price) > 0){
                mMinPrice = price;
            }
            
            if(mMaxPrice.compareTo(price) < 0){
                mMaxPrice = price;
            }
        }
        
        public void updatePrice(BigDecimal price, BigDecimal amount){
            mPriceMap.put(price, amount);
            updateMaxMin();
        }
        
        public void removePrice(BigDecimal price){
            mPriceMap.remove(price);
            
            if((mMinPrice.compareTo(price) == 0) || (mMaxPrice.compareTo(price) == 0)){
                updateMaxMin();
            }
        }
        
        public boolean priceExists(BigDecimal price){
           return mPriceMap.containsKey(price);
        }
        
        public void updateMaxMin(){
            ArrayList<BigDecimal> publicOrders = getSortedOrders();
            mMinPrice = publicOrders.get(0);
            mMaxPrice = publicOrders.get(publicOrders.size() - 1);
        }
        
        public ArrayList<BigDecimal> getSortedOrders(){
            ArrayList<BigDecimal> prices = new ArrayList<BigDecimal>();
            
            Iterator<Map.Entry<BigDecimal, BigDecimal>> it = mPriceMap.entrySet().iterator();
            
            while(it.hasNext()){
                Map.Entry<BigDecimal, BigDecimal> entry = it.next(); 
                prices.add(entry.getValue());
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

    public static void main(String args[]) throws SSLException, URISyntaxException, InterruptedException{
        final BitsoChannels[] bitsoChannels = { BitsoChannels.TRADES,
                BitsoChannels.DIFF_ORDERS};
        
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