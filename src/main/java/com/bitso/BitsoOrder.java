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

    public BitsoBook book;
    public BigDecimal originalAmount;
    public BigDecimal unfilledAmount;
    public BigDecimal originalValue;
    public Date orderDate;
    public Date updateDate;
    public BigDecimal price;
    public String oid;
    public SIDE side;
    public STATUS status;
    public TYPE type;

    public BitsoOrder(JSONObject o) {
        book = Helpers.getBook(Helpers.getString(o, "book"));
        originalAmount = Helpers.getBD(o, "original_amount");
        unfilledAmount = Helpers.getBD(o, "unfilled_amount");
        originalValue = Helpers.getBD(o, "original_value");
        orderDate = Helpers.getZonedDatetime(o, "created_at");
        updateDate = Helpers.getZonedDatetime(o, "updated_at");
        price = Helpers.getBD(o, "price");
        oid = Helpers.getString(o, "oid");
        side = getSide(Helpers.getString(o, "side"));
        status = getStatus(Helpers.getString(o, "status"));
        type = getType(Helpers.getString(o, "type"));
    }

    public BitsoOrder.SIDE getSide(String side) {
        if (side.equals("buy")) {
            return BitsoOrder.SIDE.BUY;
        }
        return BitsoOrder.SIDE.SELL;
    }

    public BitsoOrder.STATUS getStatus(String status) {
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

    public BitsoOrder.TYPE getType(String type) {
        switch (type) {
            case "limit":
                return BitsoOrder.TYPE.LIMIT;
            case "market":
                return BitsoOrder.TYPE.MARKET;
            default:
                String exceptionMessage = type + "is not a supported order type";
                throw new BitsoExceptionNotExpectedValue(exceptionMessage);
        }
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
