package com.bitso;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

/**
 * Represents an order in the Bitso system.
 */
@Slf4j
@Getter
@ToString
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

    private final String book;
    private final BigDecimal originalAmount;
    private BigDecimal unfilledAmount;
    private final BigDecimal originalValue;
    private final Date orderDate;
    private final Date updateDate;
    private final BigDecimal price;
    private final String oid;
    private final SIDE side;
    // open || partially filled || completed || cancelled || queuedis
    private final STATUS status;
    private final TYPE type;
    private TIME_IN_FORCE timeInForce;
    /** The major settlement currency, if one is used instead of the book major. */
    private final String majorSettle;
    /** The minor settlement currency, if one is used instead of the book minor. */
    private final String minorSettle;

    public BitsoOrder(JSONObject o) {
        book = Helpers.getString(o, "book");
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
        majorSettle = Helpers.getString(o, "settle_major");
        minorSettle = Helpers.getString(o, "settle_minor");
    }

    private BitsoOrder.SIDE retrieveSide(String side) {
        return BitsoOrder.SIDE.valueOf(side.toUpperCase());
    }

    private BitsoOrder.STATUS retrieveStatus(String status) {
        switch (status) {
            case "open":
                return STATUS.OPEN;
            case "partially filled":
                return STATUS.PARTIALLY_FILLED;
            case "completed":
                return STATUS.COMPLETED;
            case "cancelled":
                return STATUS.CANCELLED;
            case "queued":
                return STATUS.QUEUED;
        }

        log.error("{} is not a supported order status", status);
        return BitsoOrder.STATUS.UNKNOWN;
    }

    private BitsoOrder.TYPE retrieveType(String type) {
        return BitsoOrder.TYPE.valueOf(type.toUpperCase());
    }

    public void setUnfilledAmount(BigDecimal unfilledAmount) {
        this.unfilledAmount = unfilledAmount;
    }
}
