package com.bitso.exchange;

import java.util.ArrayList;

public abstract class OrderBook {

    public long timestamp;
    public ArrayList<BookOrder> bids;
    public ArrayList<BookOrder> asks;

    public BookOrder getHighestBid() {
        if (bids == null || bids.size() == 0) {
            return null;
        }
        return bids.get(0);
    }

    public BookOrder getLowestAsk() {
        if (asks == null || asks.size() == 0) {
            return null;
        }
        return asks.get(0);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("====BIDS====");
        if (bids != null) {
            for (BookOrder bid : bids) {
                sb.append(bid.toString());
            }
        }
        sb.append("====ASKS====");
        if (asks != null) {
            for (BookOrder ask : asks) {
                sb.append(ask.toString());
            }
        }
        return sb.toString();
    }

}
