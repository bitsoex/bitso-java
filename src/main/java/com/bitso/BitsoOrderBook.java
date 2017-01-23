package com.bitso;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoOrderBook {
    public ZonedDateTime orderDate;
    public String sequence;
    public PulicOrder[] asks;
    public PulicOrder[] bids;

    public BitsoOrderBook(JSONObject o){
        orderDate = Helpers.getZonedDatetime(o, "updated_at");
        sequence = Helpers.getString(o, "sequence");
        processOrders(o);
    }
    
    private void processOrders(JSONObject o) {
        // Getting asks
        if (o.has("asks")) {
            JSONArray asksArray = o.getJSONArray("asks");
            int totalAsks = asksArray.length();
            asks = new PulicOrder[totalAsks];
            for (int i = 0; i < totalAsks; i++) {
                asks[i] = new PulicOrder(asksArray.getJSONObject(i));
            }
        }

        // Getting bids
        if (o.has("bids")) {
            JSONArray bidsArray = o.getJSONArray("bids");
            int totalBids = bidsArray.length();
            bids = new PulicOrder[totalBids];
            for (int i = 0; i < totalBids; i++) {
                bids[i] = new PulicOrder(bidsArray.getJSONObject(i));
            }
        }

    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
    
    public class PulicOrder{
        public BitsoBook book;
        public BigDecimal price;
        public BigDecimal amount;
        
        public PulicOrder(JSONObject o){
            book = BitsoOrder.getBook(Helpers.getString(o, "book"));
            price = Helpers.getBD(o, "price");
            amount = Helpers.getBD(o, "amount");
        }

        @Override
        public String toString() {
            return Helpers.fieldPrinter(this);
        }
    }
}
