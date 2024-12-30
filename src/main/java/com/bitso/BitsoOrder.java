package com.bitso;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONObject;

import com.bitso.helpers.Helpers;

/**
 * Represents an order in the Bitso system.
 */
public class BitsoOrder {

    public enum SIDE {
        BUY, SELL;

        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public enum TYPE {
        MARKET, LIMIT;

        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public enum STATUS {
        OPEN, PARTIALLY_FILLED, QUEUED, COMPLETED, CANCELLED, UNKNOWN
    }

    /** The time-in-force attribute for limit orders. */
    public enum TIME_IN_FORCE {
        /** Leave the order in the book until it's completed, or cancelled by the user. */
        GOODTILLCANCELLED,
        /** The order must be completed when it's processed, or canceled without any matches. */
        FILLORKILL,
        /** If the order is not completed during processing, cancel whatever is left, but keep the matches. */
        IMMEDIATEORCANCEL,
        /** If the order matches during processing, cancel it instead. */
        POSTONLY
    }

    private String book;
    private BigDecimal originalAmount;
    private BigDecimal unfilledAmount;
    private BigDecimal originalValue;
    private Date orderDate;
    private Date updateDate;
    private BigDecimal price;
    private String oid;
    private SIDE side;
    // open || partially filled || completed || cancelled || queuedis
    private STATUS status;
    private TYPE type;
    private TIME_IN_FORCE timeInForce;

    public BitsoOrder(JSONObject o) {
        book = Helpers.getString(o, "book");
        originalAmount = Helpers.getBD(o, "original_amount");
        //unfilledAmount = Helpers.getBD(o, "unfilled_amount");
        originalValue = Helpers.getBD(o, "original_value");
        orderDate = Helpers.getZonedDatetime(o, "created_at");
        updateDate = Helpers.getZonedDatetime(o, "updated_at");
        price = Helpers.getBD(o, "price");
        oid = Helpers.getString(o, "oid");
        side = retrieveSide(Helpers.getString(o, "side"));
        status = retrieveStatus(Helpers.getString(o, "status"));
        type = retrieveType(Helpers.getString(o, "type"));
    }

    private BitsoOrder.SIDE retrieveSide(String side) {
        return BitsoOrder.SIDE.valueOf(side.toUpperCase());
    }

    private BitsoOrder.STATUS retrieveStatus(String status) {
        if (status.equals("open")) return BitsoOrder.STATUS.OPEN;
        if (status.equals("partially filled")) return BitsoOrder.STATUS.PARTIALLY_FILLED;
        if (status.equals("completed")) return BitsoOrder.STATUS.COMPLETED;
        if (status.equals("cancelled")) return BitsoOrder.STATUS.CANCELLED;
        if (status.equals("queued")) return BitsoOrder.STATUS.QUEUED;

        System.err.println(status + " is not a supported order status");
        return BitsoOrder.STATUS.UNKNOWN;
    }

    private BitsoOrder.TYPE retrieveType(String type) {
        return BitsoOrder.TYPE.valueOf(type.toUpperCase());
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getUnfilledAmount() {
        return unfilledAmount;
    }

    public void setUnfilledAmount(BigDecimal unfilledAmount) {
        this.unfilledAmount = unfilledAmount;
    }

    public BigDecimal getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(BigDecimal originalValue) {
        this.originalValue = originalValue;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public SIDE getSide() {
        return side;
    }

    public void setSide(SIDE side) {
        this.side = side;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this, BitsoOrder.class);
    }
}
