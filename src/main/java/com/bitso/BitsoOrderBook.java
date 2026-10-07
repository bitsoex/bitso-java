package com.bitso;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Value;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

/** The info for a trading order book.
 */
@Value
public class BitsoOrderBook {
    Date orderDate;
    int sequence;
    List<PublicOrder> asks;
    List<PublicOrder> bids;

    public BitsoOrderBook(JSONObject o) {
        this.orderDate = Helpers.getZonedDatetime(o, "updated_at");
        this.sequence = Helpers.getInt(o, "sequence");
        // Getting asks
        if (o.has("asks")) {
            JSONArray asksArray = o.getJSONArray("asks");
            int totalAsks = asksArray.length();
            var mAsks = new PublicOrder[totalAsks];
            for (int i = 0; i < totalAsks; i++) {
                mAsks[i] = new PublicOrder(asksArray.getJSONObject(i));
            }
            asks = List.of(mAsks);
        } else {
            asks = List.of();
        }

        // Getting bids
        if (o.has("bids")) {
            JSONArray bidsArray = o.getJSONArray("bids");
            int totalBids = bidsArray.length();
            var mBids = new PublicOrder[totalBids];
            for (int i = 0; i < totalBids; i++) {
                mBids[i] = new PublicOrder(bidsArray.getJSONObject(i));
            }
            bids = List.of(mBids);
        } else {
            bids = List.of();
        }
    }

    /** The data for an order in the book. */
    @Value
    public static class PublicOrder implements Comparable<PublicOrder> {
        String book;
        BigDecimal price;
        BigDecimal amount;
        String orderId;

        public PublicOrder(JSONObject o) {
            book = Helpers.getString(o, "book");
            price = Helpers.getBD(o, "price");
            amount = Helpers.getBD(o, "amount");
            if (o.has("oid")) {
                orderId = Helpers.getString(o, "oid");
            } else {
                orderId = "";
            }
        }

        public int compareTo(PublicOrder o) {
            return price.compareTo(o.price);
        }
    }
}
