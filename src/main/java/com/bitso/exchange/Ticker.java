package com.bitso.exchange;

import java.math.BigDecimal;
import java.util.Date;

import com.bitso.helpers.Helpers;

public class Ticker {

    protected BigDecimal mLast;
    protected BigDecimal mHigh;
    protected BigDecimal mLow;
    protected BigDecimal mVwap;
    protected BigDecimal mVolume;
    protected BigDecimal mBid;
    protected BigDecimal mAsk;
    protected Date mCreatedAt;

    public BigDecimal getLast() {
        return mLast;
    }

    public void setLast(BigDecimal mLast) {
        this.mLast = mLast;
    }

    public BigDecimal getHigh() {
        return mHigh;
    }

    public void setHigh(BigDecimal mHigh) {
        this.mHigh = mHigh;
    }

    public BigDecimal getLow() {
        return mLow;
    }

    public void setLow(BigDecimal mLow) {
        this.mLow = mLow;
    }

    public BigDecimal getVwap() {
        return mVwap;
    }

    public void setVwap(BigDecimal mVwap) {
        this.mVwap = mVwap;
    }

    public BigDecimal getVolume() {
        return mVolume;
    }

    public void setVolume(BigDecimal mVolume) {
        this.mVolume = mVolume;
    }

    public BigDecimal getBid() {
        return mBid;
    }

    public void setBid(BigDecimal mBid) {
        this.mBid = mBid;
    }

    public BigDecimal getAsk() {
        return mAsk;
    }

    public void setAsk(BigDecimal mAsk) {
        this.mAsk = mAsk;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(Date mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    public String toString() {
        return Helpers.fieldPrinter(this, Ticker.class);
    }
}
