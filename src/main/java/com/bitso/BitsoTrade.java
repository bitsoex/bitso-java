package com.bitso;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoTrade {
    // TODO:
    // Check how long the tradeId would be, in order
    // to save it in an int or long type
    private int mTid;
    private String mOid;
    private String mSide;
    private String mMinorCurrency;
    private String mMajorCurrency;
    private String mBook;
    private String mFeesCurrency;
    private BigDecimal mFeesAmount;
    private BigDecimal mPrice;
    private BigDecimal mMajor;
    private BigDecimal mMinor;
    private Date mTradeDate;

    public BitsoTrade() {
        mMajor = BigDecimal.ZERO;
        mMinor = BigDecimal.ZERO;
    }

    public BitsoTrade(JSONObject o) {
        this.mBook = Helpers.getString(o, "book");
        this.mMajor = Helpers.getBD(o, "major");
        this.mTradeDate = Helpers.getZonedDatetime(o, "created_at");
        this.mMinor = Helpers.getBD(o, "minor");
        this.mFeesAmount = Helpers.getBD(o, "fees_amount");
        this.mFeesCurrency = Helpers.getString(o, "fees_currency");
        this.mPrice = Helpers.getBD(o, "price");
        this.mTid = Helpers.getInteger(o, "tid");
        this.mOid = Helpers.getString(o, "oid");
        this.mSide = Helpers.getString(o, "side");
        this.mMinorCurrency = Helpers.getString(o, "minor_currency");
        this.mMajorCurrency = Helpers.getString(o, "major_currency");
    }

    public int getTid() {
        return mTid;
    }

    public void setTid(int mTid) {
        this.mTid = mTid;
    }

    public String getOid() {
        return mOid;
    }

    public void setOid(String mOid) {
        this.mOid = mOid;
    }

    public String getSide() {
        return mSide;
    }

    public void setSide(String mSide) {
        this.mSide = mSide;
    }

    public String getMinorCurrency() {
        return mMinorCurrency;
    }

    public void setMinorCurrency(String mMinorCurrency) {
        this.mMinorCurrency = mMinorCurrency;
    }

    public String getMajorCurrency() {
        return mMajorCurrency;
    }

    public void setMajorCurrency(String mMajorCurrency) {
        this.mMajorCurrency = mMajorCurrency;
    }

    public String getBook() {
        return mBook;
    }

    public void setBook(String mBook) {
        this.mBook = mBook;
    }

    public String getFeesCurrency() {
        return mFeesCurrency;
    }

    public void setFeesCurrency(String mFeesCurrency) {
        this.mFeesCurrency = mFeesCurrency;
    }

    public BigDecimal getFeesAmount() {
        return mFeesAmount;
    }

    public void setFeesAmount(BigDecimal mFeesAmount) {
        this.mFeesAmount = mFeesAmount;
    }

    public BigDecimal getPrice() {
        return mPrice;
    }

    public void setPrice(BigDecimal mPrice) {
        this.mPrice = mPrice;
    }

    public BigDecimal getMajor() {
        return mMajor;
    }

    public void setMajor(BigDecimal mMajor) {
        this.mMajor = mMajor;
    }

    public BigDecimal getMinor() {
        return mMinor;
    }

    public void setMinor(BigDecimal mMinor) {
        this.mMinor = mMinor;
    }

    public Date getTradeDate() {
        return mTradeDate;
    }

    public void setTradeDate(Date mTradeDate) {
        this.mTradeDate = mTradeDate;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this, BitsoTrade.class);
    }
}
