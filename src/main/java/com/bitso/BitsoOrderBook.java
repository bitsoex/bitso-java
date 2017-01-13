package com.bitso;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exchange.BookOrder;
import com.bitso.exchange.OrderBook;
import com.bitso.helpers.Helpers;

public class BitsoOrderBook extends OrderBook {
    public long sequence;
    public ZonedDateTime updatedAt;

    public BitsoOrderBook(JSONObject obj) {
        if (!obj.has("payload")) {
            System.err.println("No payload: " + obj);
            Helpers.printStackTrace();
        }
        obj = obj.getJSONObject("payload");
        if (obj.has("updated_at")) {
            updatedAt = ZonedDateTime.parse(obj.getString("updated_at"));
        } else {
            System.err.println("No timestamp: " + obj);
            Helpers.printStackTrace();
        }

        if (!obj.has("sequence")) {
            System.err.println("No sequence: " + obj);
            Helpers.printStackTrace();
        }
        sequence = Long.parseLong(obj.getString("sequence"));

        if (obj.has("bids")) {
            JSONArray bidsJSON = obj.getJSONArray("bids");
            bids = new ArrayList<BookOrder>(bidsJSON.length());

            for (int i = 0; i < bidsJSON.length(); i++) {
                JSONObject bid = bidsJSON.getJSONObject(i);
                BigDecimal price = new BigDecimal(bid.getString("price"));
                BigDecimal amount = new BigDecimal(bid.getString("amount"));
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
                JSONObject ask = asksJSON.getJSONObject(i);
                BigDecimal price = new BigDecimal(ask.getString("price"));
                BigDecimal amount = new BigDecimal(ask.getString("amount"));
                asks.add(new BookOrder(price, amount, BookOrder.TYPE.SELL));
            }
        } else {
            System.err.println("No asks: " + obj);
            Helpers.printStackTrace();
        }
    }
}
