package com.bitso.exchange;

import java.math.BigDecimal;

public class BookOrder {
    public String id;
    public String book;
    public BigDecimal price;
    public BigDecimal amount;
    public TYPE type;
    public STATUS status;
    public String created;
    public String updated;
    public String dateTime;

    public static enum TYPE {
        BUY, SELL
    }

    public static enum STATUS {
        ACTIVE, PARTIALLY_FILLED, COMPLETE, CANCELLED
    }

    public BookOrder(BigDecimal price, BigDecimal amount) {
        this(price, amount, null);
    }

    public BookOrder(BigDecimal price, BigDecimal amount, TYPE type) {
        this.price = price;
        this.amount = amount;
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=====\nid=");
        sb.append(id);
        sb.append("\nbook=");
        sb.append(book);
        sb.append("\nprice=");
        sb.append(price);
        sb.append("\namount=");
        sb.append(amount);
        sb.append("\ntype=");
        sb.append(type);
        sb.append("\nstatus=");
        sb.append(status);
        sb.append("\ncreated=");
        sb.append(created);
        sb.append("\nupdated=");
        sb.append(updated);
        sb.append("\n=====");
        return sb.toString();
    }

}
