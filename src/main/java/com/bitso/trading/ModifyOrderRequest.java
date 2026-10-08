package com.bitso.trading;

import lombok.Value;

import java.math.BigDecimal;

/** A request to modify an existing order.
 * The parameters that can be modified are: the major or minor amount (only one of those two, not both),
 * the price, the stop rate, and whether to cancel the order if the modification fails.
 */
@Value
public class ModifyOrderRequest {

    BigDecimal major;
    BigDecimal minor;
    BigDecimal price;
    BigDecimal stopRate;
    boolean cancelOnFail;

    /** Convenience method to only modify the major amount. */
    public static ModifyOrderRequest withNewMajor(BigDecimal newMajor) {
        return new ModifyOrderRequest(newMajor, null, null, null, false);
    }
    /** Convenience method to only modify the minor amount. */
    public static ModifyOrderRequest withNewMinor(BigDecimal newMinor) {
        return new ModifyOrderRequest(null, newMinor, null, null, false);
    }
    /** Convenience method to only modify the price. */
    public static ModifyOrderRequest withNewPrice(BigDecimal newPrice) {
        return new ModifyOrderRequest(null, null, newPrice, null, false);
    }
    /** Convenience method to only modify the major amount along with the price. */
    public static ModifyOrderRequest withNewMajorAndPrice(BigDecimal newMajor, BigDecimal newPrice) {
        return new ModifyOrderRequest(newMajor, null, newPrice, null, false);
    }
    /** Convenience method to only modify the minor amount along with the price. */
    public static ModifyOrderRequest withNewMinorAndPrice(BigDecimal newMinor, BigDecimal newPrice) {
        return new ModifyOrderRequest(null, newMinor, newPrice, null, false);
    }
    /** Convenience method to only modify the stop rate. */
    public static ModifyOrderRequest withNewStopRate(BigDecimal newStopRate) {
        return new ModifyOrderRequest(null, null, null, newStopRate, false);
    }

    /** Throw an exception if the parameters are wrong:
     * <ul>
     *     <li>Major and minor cannot both be set.</li>
     *     <li>At least one of the numeric values must be set.</li>
     * </ul>
     */
    public void validate() {
        if (major != null && minor != null) {
            throw new IllegalArgumentException("major and minor cannot both be set");
        }
        if (major == null && minor == null && price == null && stopRate == null) {
            throw new IllegalArgumentException("at least one of major, minor, price, or stop rate must be set");
        }
    }
}
