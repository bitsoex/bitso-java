package com.bitso.exchange;

import java.math.BigDecimal;

import org.json.JSONObject;

import com.bitso.BitsoOperation;
import com.bitso.helpers.Helpers;

public class BookInfo {

    private String mBook;
    private BigDecimal mMinAmount;
    private BigDecimal mMaxAmount;
    private BigDecimal mMinPrice;
    private BigDecimal mMaxPrice;
    private BigDecimal mMinValue;
    private BigDecimal mMaxValue;

    public BookInfo(JSONObject o) {
        mMinAmount = Helpers.getBD(o, "minimum_amount");
        mMaxAmount = Helpers.getBD(o, "maximum_amount");
        mMinPrice = Helpers.getBD(o, "minimum_price");
        mMaxPrice = Helpers.getBD(o, "maximum_price");
        mMinValue = Helpers.getBD(o, "minimum_value");
        mMaxValue = Helpers.getBD(o, "maximum_value");
        mBook = Helpers.getString(o, "book");
    }

    public String getBook() {
        return mBook;
    }

    public void setBook(String mBook) {
        this.mBook = mBook;
    }

    public BigDecimal getMinAmount() {
        return mMinAmount;
    }

    public void setMinAmount(BigDecimal mMinAmount) {
        this.mMinAmount = mMinAmount;
    }

    public BigDecimal gemMaxAmount() {
        return mMaxAmount;
    }

    public void setMaxAmount(BigDecimal mMaxAmount) {
        this.mMaxAmount = mMaxAmount;
    }

    public BigDecimal getMinPrice() {
        return mMinPrice;
    }

    public void setMinPrice(BigDecimal mMinPrice) {
        this.mMinPrice = mMinPrice;
    }

    public BigDecimal getMaxPrice() {
        return mMaxPrice;
    }

    public void setMaxPrice(BigDecimal mMaxPrice) {
        this.mMaxPrice = mMaxPrice;
    }

    public BigDecimal getMinValue() {
        return mMinValue;
    }

    public void setMinValue(BigDecimal mMinValue) {
        this.mMinValue = mMinValue;
    }

    public BigDecimal getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(BigDecimal mMaxValue) {
        this.mMaxValue = mMaxValue;
    }

    public String toString() {
        return Helpers.fieldPrinter(this, BookInfo.class);
    }
}
