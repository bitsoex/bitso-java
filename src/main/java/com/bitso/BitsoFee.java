package com.bitso;

import com.bitso.helpers.Helpers;

import lombok.Value;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** A container for trading and withdrawal fees. */
@Value
public class BitsoFee {
    /** A map from books to the trading fees for that book.
     * A book can have several fee entries, for different volume levels
     */
    Map<String, List<Fee>> tradeFees;
    /** A map of currencies with their withdrawal fees. */
    Map<String, BigDecimal> withdrawalFees;

    public BitsoFee(JSONObject o) {
        tradeFees = processTradeFees(o);
        withdrawalFees = processWithdrawalFees(o);
    }

    private Map<String, List<Fee>> processTradeFees(JSONObject o) {
        var mTradeFees = new HashMap<String, List<Fee>>();
        JSONArray jsonFees = o.getJSONArray("fees");
        int totalElements = jsonFees.length();
        for (int i = 0; i < totalElements; i++) {
            JSONObject fee = jsonFees.getJSONObject(i);
            String book = Helpers.getString(fee, "book");
            Fee currentFee = new Fee(book,
                    Helpers.getBD(fee, "current_volume"),
                    Helpers.getBD(fee, "taker_fee_decimal"),
                    Helpers.getBD(fee, "taker_fee_percent"),
                    Helpers.getBD(fee, "maker_fee_decimal"),
                    Helpers.getBD(fee, "maker_fee_percent")
            );
            mTradeFees.computeIfAbsent(book, k -> new ArrayList<>(2)).add(currentFee);
        }
        return Map.copyOf(mTradeFees);
    }

    private Map<String, BigDecimal> processWithdrawalFees(JSONObject o) {
        var mWithdrawalFees = new HashMap<String, BigDecimal>();
        JSONObject withdrawalFees = o.getJSONObject("withdrawal_fees");
        Iterator<String> it = withdrawalFees.keys();
        while (it.hasNext()) {
            String key = it.next();
            mWithdrawalFees.put(key, withdrawalFees.getBigDecimal(key));
        }
        return Map.copyOf(mWithdrawalFees);
    }

    /** An entry for trading fees. */
    @Value
    public static class Fee {
        /** The order book for which these fees apply. */
        String mBook;
        /** The trading volume that must be achieved to get these fees. */
        BigDecimal currentVolume;
        /** The taker fee, applied when an order trades during matching. */
        BigDecimal takerFeeDecimal;
        /** The taker fee as a percentage. */
        BigDecimal takerFeePercent;
        /** The maker fee, applied when an order trades after it's been added to the book. */
        BigDecimal makerFeeDecimal;
        /** The maker fee as a percentage. */
        BigDecimal makerFeePercent;
    }
}
