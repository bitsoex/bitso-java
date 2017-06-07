package com.bitso;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONObject;

import com.bitso.exceptions.BitsoExceptionNotExpectedValue;
import com.bitso.helpers.Helpers;

public class BitsoOrder {
    public static enum SIDE {
        BUY, SELL;

        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public static enum STATUS {
        OPEN, PARTIALLY_FILLED, COMPLETED, CANCELLED;

        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public static enum TYPE {
        MARKET, LIMIT;

        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private BitsoBook book;
    private BigDecimal originalAmount;
    private BigDecimal unfilledAmount;
    private BigDecimal originalValue;
    private Date orderDate;
    private Date updateDate;
    private BigDecimal price;
    private String oid;
    private SIDE side;
    private STATUS status;
    private TYPE type;

    public BitsoOrder(JSONObject o) {
        book = Helpers.getBook(Helpers.getString(o, "book"));
        originalAmount = Helpers.getBD(o, "original_amount");
        unfilledAmount = Helpers.getBD(o, "unfilled_amount");
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
        switch (status) {
            case "open":
                return BitsoOrder.STATUS.OPEN;
            case "partially filled":
                return BitsoOrder.STATUS.PARTIALLY_FILLED;
            case "completed":
                return BitsoOrder.STATUS.COMPLETED;
            case "cancelled":
                return BitsoOrder.STATUS.CANCELLED;
            default:
                String exceptionMessage = status + "is not a supported order status";
                throw new BitsoExceptionNotExpectedValue(exceptionMessage);
        }
    }

    private BitsoOrder.TYPE retrieveType(String type) {
        return BitsoOrder.TYPE.valueOf(type.toUpperCase());
    }

    public BitsoBook getBook() {
        return book;
    }

    public void setBook(BitsoBook book) {
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
