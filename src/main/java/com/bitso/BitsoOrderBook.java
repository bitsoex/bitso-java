package com.bitso;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exchange.BookOrder;
import com.bitso.exchange.OrderBook;

public class BitsoOrderBook extends OrderBook {

    public BitsoOrderBook(String orderBookJSON) {
        JSONObject obj = new JSONObject(orderBookJSON);

        timestamp = obj.getLong("timestamp");

        JSONArray bidsJSON = obj.getJSONArray("bids");
        bids = new ArrayList<BookOrder>(bidsJSON.length());

        for (int i = 0; i < bidsJSON.length(); i++) {
            JSONArray bid = bidsJSON.getJSONArray(i);
            BigDecimal price = new BigDecimal(bid.getString(0));
            BigDecimal amount = new BigDecimal(bid.getString(1));
            bids.add(new BookOrder(price, amount, BookOrder.TYPE.BUY));
        }

        JSONArray asksJSON = obj.getJSONArray("asks");
        asks = new ArrayList<BookOrder>(asksJSON.length());

        for (int i = 0; i < asksJSON.length(); i++) {
            JSONArray ask = asksJSON.getJSONArray(i);
            BigDecimal price = new BigDecimal(ask.getString(0));
            BigDecimal amount = new BigDecimal(ask.getString(1));
            asks.add(new BookOrder(price, amount, BookOrder.TYPE.SELL));
        }
    }
}
