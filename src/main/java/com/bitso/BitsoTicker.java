package com.bitso;

import org.json.JSONObject;

import com.bitso.exchange.Ticker;
import com.bitso.helpers.Helpers;

public class BitsoTicker extends Ticker {

    private String book;

    public BitsoTicker(JSONObject o) {
        mLast = Helpers.getBD(o, "last");
        mHigh = Helpers.getBD(o, "high");
        mLow = Helpers.getBD(o, "low");
        mVwap = Helpers.getBD(o, "vwap");
        mVolume = Helpers.getBD(o, "volume");
        mBid = Helpers.getBD(o, "bid");
        mAsk = Helpers.getBD(o, "ask");
        mCreatedAt = Helpers.getZonedDatetime(o, "created_at");
        book = Helpers.getString(o, "book");
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String toString() {
        return Helpers.fieldPrinter(this, BitsoTicker.class) + super.toString();
    }
}
