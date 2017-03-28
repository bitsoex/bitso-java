package com.bitso;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoTrade {
    protected String book;
    protected BigDecimal major;
    protected Date tradeDate;
    protected BigDecimal minor;
    protected BigDecimal feesAmount;
    protected String feesCurrency;
    protected BigDecimal price;
    // TODO:
    // Check how long the tradeId would be, in order
    // to save it in an int or long type
    protected int tid;
    protected String oid;
    protected String side;

    public BitsoTrade(JSONObject o) {
        book = Helpers.getString(o, "book");
        major = Helpers.getBD(o, "major");
        tradeDate = Helpers.getZonedDatetime(o, "created_at");
        minor = Helpers.getBD(o, "minor");
        feesAmount = Helpers.getBD(o, "fees_amount");
        feesCurrency = Helpers.getString(o, "fees_currency");
        price = Helpers.getBD(o, "price");
        tid = Helpers.getInteger(o, "tid");
        oid = Helpers.getString(o, "oid");
        side = Helpers.getString(o, "side");
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public BigDecimal getMajor() {
        return major;
    }

    public void setMajor(BigDecimal major) {
        this.major = major;
    }

    public Date getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(Date tradeDate) {
        this.tradeDate = tradeDate;
    }

    public BigDecimal getMinor() {
        return minor;
    }

    public void setMinor(BigDecimal minor) {
        this.minor = minor;
    }

    public BigDecimal getFeesAmount() {
        return feesAmount;
    }

    public void setFeesAmount(BigDecimal feesAmount) {
        this.feesAmount = feesAmount;
    }

    public String getFeesCurrency() {
        return feesCurrency;
    }

    public void setFeesCurrency(String feesCurrency) {
        this.feesCurrency = feesCurrency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
