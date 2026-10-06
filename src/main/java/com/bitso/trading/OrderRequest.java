package com.bitso.trading;

import com.bitso.BitsoOrder;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/** A request to place an order.
 * Only one of the major (amount) or the minor (value) must be specified.
 */
@Value
@Builder
public class OrderRequest {
    /** The book where the order should be placed. */
    String book;
    /** The amount of the order, expressed in the book's major currency, or the major settlement currency. */
    BigDecimal amount;
    /** The value of the order, expressed in the book's minor currency, or the minor settlement currency. */
    BigDecimal value;
    /** Whether this is a buy or a sell order. */
    BitsoOrder.SIDE side;
    /** The order type, limit or market. */
    BitsoOrder.TYPE mode;
    /** The price of the order, expressed in the book's minor currency, or the minor settlement currency. Use null for market orders. */
    BigDecimal price;
    /** The time-in-force setting. Valid only for limit orders. */
    @Builder.Default
    BitsoOrder.TIME_IN_FORCE timeInForce = BitsoOrder.TIME_IN_FORCE.GOODTILLCANCELLED;
    /** The major settlement currency to use instead of the book major. */
    String majorSettle;
    /** The minor settlement currency to use instead of the book minor. */
    String minorSettle;
}
