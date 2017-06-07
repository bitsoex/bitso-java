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
    private PulicOrder[] mAsks;
    private PulicOrder[] mBids;

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
            mAsks = new PulicOrder[totalAsks];
            for (int i = 0; i < totalAsks; i++) {
                mAsks[i] = new PulicOrder(asksArray.getJSONObject(i));
            }
        }

        // Getting bids
        if (o.has("bids")) {
            JSONArray bidsArray = o.getJSONArray("bids");
            int totalBids = bidsArray.length();
            mBids = new PulicOrder[totalBids];
            for (int i = 0; i < totalBids; i++) {
                mBids[i] = new PulicOrder(bidsArray.getJSONObject(i));
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

    public PulicOrder[] getAsks() {
        return mAsks;
    }

    public void setAsks(PulicOrder[] mAsks) {
        this.mAsks = mAsks;
    }

    public PulicOrder[] getBids() {
        return mBids;
    }

    public void setBids(PulicOrder[] mBids) {
        this.mBids = mBids;
    }

    public String toString() {
        return Helpers.fieldPrinter(this, BitsoOrderBook.class);
    }

    public class PulicOrder implements Comparable<PulicOrder> {
        private BitsoBook mBook;
        private BigDecimal mPrice;
        private BigDecimal mAmount;
        private String mOrderId;

        public PulicOrder(JSONObject o) {
            mBook = Helpers.getBook(Helpers.getString(o, "book"));
            mPrice = Helpers.getBD(o, "price");
            mAmount = Helpers.getBD(o, "amount");
            if (o.has("oid")) {
                mOrderId = Helpers.getString(o, "oid");
            } else {
                mOrderId = "";
            }
        }

        public BitsoBook getBook() {
            return mBook;
        }

        public void setBook(BitsoBook mBook) {
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
            return Helpers.fieldPrinter(this, BitsoOrderBook.PulicOrder.class);
        }

        @Override
        public int compareTo(PulicOrder o) {
            return mPrice.compareTo(o.mPrice);
        }

        public class Comparators {
            public Comparator<PulicOrder> PRICE = new Comparator<PulicOrder>() {
                @Override
                public int compare(PulicOrder o1, PulicOrder o2) {
                    return o1.mPrice.compareTo(o2.mPrice);
                }
            };
        }
    }
}
