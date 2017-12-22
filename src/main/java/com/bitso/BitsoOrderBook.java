package com.bitso;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoOrderBook {
    private Date mOrderDate;
    private int mSequence;
    private PublicOrder[] mAsks;
    private PublicOrder[] mBids;

    public BitsoOrderBook(JSONObject o) {
        this.mOrderDate = Helpers.getZonedDatetime(o, "updated_at");
        this.mSequence = Helpers.getInt(o, "sequence");
        processOrders(o);
    }

    private void processOrders(JSONObject o) {
        // Getting asks
        if (o.has("asks")) {
            JSONArray asksArray = o.getJSONArray("asks");
            int totalAsks = asksArray.length();
            mAsks = new PublicOrder[totalAsks];
            for (int i = 0; i < totalAsks; i++) {
                mAsks[i] = new PublicOrder(asksArray.getJSONObject(i));
            }
        }

        // Getting bids
        if (o.has("bids")) {
            JSONArray bidsArray = o.getJSONArray("bids");
            int totalBids = bidsArray.length();
            mBids = new PublicOrder[totalBids];
            for (int i = 0; i < totalBids; i++) {
                mBids[i] = new PublicOrder(bidsArray.getJSONObject(i));
            }
        }

    }

    public Date getOrderDate() {
        return mOrderDate;
    }

    public void setOrderDate(Date mOrderDate) {
        this.mOrderDate = mOrderDate;
    }

    public int getSequence() {
        return mSequence;
    }

    public void setSequence(int mSequence) {
        this.mSequence = mSequence;
    }

    public PublicOrder[] getAsks() {
        return mAsks;
    }

    public void setAsks(PublicOrder[] mAsks) {
        this.mAsks = mAsks;
    }

    public PublicOrder[] getBids() {
        return mBids;
    }

    public void setBids(PublicOrder[] mBids) {
        this.mBids = mBids;
    }

    public String toString() {
        return Helpers.fieldPrinter(this, BitsoOrderBook.class);
    }

    public class PublicOrder implements Comparable<PublicOrder> {
        private String mBook;
        private BigDecimal mPrice;
        private BigDecimal mAmount;
        private String mOrderId;

        public PublicOrder(JSONObject o) {
            mBook = Helpers.getString(o, "book");
            mPrice = Helpers.getBD(o, "price");
            mAmount = Helpers.getBD(o, "amount");
            if (o.has("oid")) {
                mOrderId = Helpers.getString(o, "oid");
            } else {
                mOrderId = "";
            }
        }

        public String getBook() {
            return mBook;
        }

        public void setBook(String mBook) {
            this.mBook = mBook;
        }

        public BigDecimal getPrice() {
            return mPrice;
        }

        public void setPrice(BigDecimal mPrice) {
            this.mPrice = mPrice;
        }

        public BigDecimal getAmount() {
            return mAmount;
        }

        public void setAmount(BigDecimal mAmount) {
            this.mAmount = mAmount;
        }

        public String getOrderId() {
            return mOrderId;
        }

        public void setOrderId(String mOrderId) {
            this.mOrderId = mOrderId;
        }

        public String toString() {
            return Helpers.fieldPrinter(this, PublicOrder.class);
        }

        public int compareTo(PublicOrder o) {
            return mPrice.compareTo(o.mPrice);
        }

        public class Comparators {
            public Comparator<PublicOrder> PRICE = new Comparator<PublicOrder>() {
                public int compare(PublicOrder o1, PublicOrder o2) {
                    return o1.mPrice.compareTo(o2.mPrice);
                }
            };
        }
    }
}
