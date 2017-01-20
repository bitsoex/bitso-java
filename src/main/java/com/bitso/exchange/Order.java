package com.bitso.exchange;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.json.JSONObject;

import com.bitso.BitsoBook;
import com.bitso.exceptions.NotExpectedValue;
import com.bitso.helpers.Helpers;

public class Order {
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
    public ZonedDateTime orderDate;
    public ZonedDateTime updateDate;
    public BigDecimal price;
    public String oid;
    public SIDE side;
    public STATUS status;
    public TYPE type;

    public Order(JSONObject o) {
        book = getBook(Helpers.getString(o, "book"));
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

    private BitsoBook getBook(String book) {
        switch (book) {
            case "btc_mxn":
                return BitsoBook.BTC_MXN;
            case "eth_mxn":
                return BitsoBook.ETH_MXN;
            default:
                String exceptionMessage = book + "is not a supported book";
                throw new NotExpectedValue(exceptionMessage);
        }
    }

    private Order.SIDE getSide(String side) {
        if (side.equals("buy")) {
            return Order.SIDE.BUY;
        }
        return Order.SIDE.SELL;
    }

    private Order.STATUS getStatus(String status) {
        switch (status) {
            case "open":
                return Order.STATUS.OPEN;
            case "partially filled":
                return Order.STATUS.PARTIALLY_FILLED;
            case "completed":
                return Order.STATUS.COMPLETED;
            case "cancelled":
                return Order.STATUS.CANCELLED;
            default:
                String exceptionMessage = status + "is not a supported order status";
                throw new NotExpectedValue(exceptionMessage);
        }
    }

    private Order.TYPE getType(String type) {
        switch (type) {
            case "limit":
                return Order.TYPE.LIMIT;
            case "market":
                return Order.TYPE.MARKET;
            default:
                String exceptionMessage = type + "is not a supported order type";
                throw new NotExpectedValue(exceptionMessage);
        }
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
