package com.bitso;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exchange.BookOrder;
import com.bitso.exchange.OrderBook;
import com.bitso.helpers.Helpers;

public class BitsoOrderBook extends OrderBook {

    public BitsoOrderBook(JSONObject obj) {

        if (obj.has("timestamp")) {
            timestamp = obj.getLong("timestamp");
        } else {
            System.err.println("No timestamp: " + obj);
            Helpers.printStackTrace();
        }

        if (obj.has("bids")) {
            JSONArray bidsJSON = obj.getJSONArray("bids");
            bids = new ArrayList<BookOrder>(bidsJSON.length());

            for (int i = 0; i < bidsJSON.length(); i++) {
                JSONArray bid = bidsJSON.getJSONArray(i);
                BigDecimal price = new BigDecimal(bid.getString(0));
                BigDecimal amount = new BigDecimal(bid.getString(1));
                bids.add(new BookOrder(price, amount, BookOrder.TYPE.BUY));
            }
        } else {
            System.err.println("No bids: " + obj);
            Helpers.printStackTrace();
        }

        if (obj.has("asks")) {
            JSONArray asksJSON = obj.getJSONArray("asks");
            asks = new ArrayList<BookOrder>(asksJSON.length());

            for (int i = 0; i < asksJSON.length(); i++) {
                JSONArray ask = asksJSON.getJSONArray(i);
                BigDecimal price = new BigDecimal(ask.getString(0));
                BigDecimal amount = new BigDecimal(ask.getString(1));
                asks.add(new BookOrder(price, amount, BookOrder.TYPE.SELL));
            }
        } else {
            System.err.println("No asks: " + obj);
            Helpers.printStackTrace();
        }
    }
}
