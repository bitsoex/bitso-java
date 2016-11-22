package com.bitso.exchange;

import java.math.BigDecimal;

import com.bitso.BitsoBook;
import com.bitso.helpers.Helpers;

public class BookOrder implements Comparable<BookOrder> {
    // Body parameters: book
    // {
    // "success": true,
    // "payload": {
    // "asks": [{
    // "book": "btc_mxn",
    // "price": "5632.24",
    // "amount": "1.34491802",
    // "created_at": "2016-04-08T17:52:31.000+00:00",
    // "updated_at": null
    // }],
    // "bids": [{
    // "book": "btc_mxn",
    // "price": "6123.55",
    // "amount": "1.12560000",
    // "created_at": "2016-04-08T17:52:31.000+00:00",
    // "updated_at": null
    // }],
    // "created_at": "2016-04-08T17:52:31.000+00:00"
    // }
    // }

    public String id;
    public BitsoBook book;
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
    public String createdAt;
    public String updatedAt;
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
