package com.bitso.exchange;

import java.math.BigDecimal;

import com.bitso.helpers.Helpers;

public class BookOrder implements Comparable<BookOrder> {
    public String id;
    public String book;
    public BigDecimal price;
    /**
     * Used to indicate the amount of major currency
     *
     * @deprecated use {@link #major} instead.
     */
    @Deprecated
    public BigDecimal amount;
    public BigDecimal major;
    public BigDecimal minor;
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

    public BookOrder(BigDecimal price, BigDecimal major) {
        this(price, major, null);
    }

    public BookOrder(BigDecimal price, BigDecimal major, TYPE type) {
        this.price = price;
        this.amount = major;
        this.major = major;
        this.type = type;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }

    @Override
    public int compareTo(BookOrder o) {
        if (o.type != this.type) {
            throw new IllegalStateException("Cannot compare two BookOrders of different types!");
        }
        if (this.type == TYPE.BUY) {
            return o.price.compareTo(this.price);
        } else if (this.type == TYPE.SELL) {
            return this.price.compareTo(o.price);
        }
        throw new IllegalStateException("Cannot compare BookOrders without type!");
    }

}
