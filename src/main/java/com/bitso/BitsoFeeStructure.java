package com.bitso;

import java.math.BigDecimal;

public class BitsoFeeStructure {

    // Numbers from https://bitso.com/fees
    // Accessed July 28, 2015
    public static BigDecimal getFeeInPercentage(BigDecimal thirtyDayVolume) {
        if (thirtyDayVolume.compareTo(new BigDecimal("1")) <= 0) {
            return new BigDecimal("1");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("2.5")) <= 0) {
            return new BigDecimal("0.95");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("4")) <= 0) {
            return new BigDecimal("0.9");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("6.5")) <= 0) {
            return new BigDecimal("0.85");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("9")) <= 0) {
            return new BigDecimal("0.8");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("12")) <= 0) {
            return new BigDecimal("0.75");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("18")) <= 0) {
            return new BigDecimal("0.7");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("25")) <= 0) {
            return new BigDecimal("0.65");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("32")) <= 0) {
            return new BigDecimal("0.60");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("40")) <= 0) {
            return new BigDecimal("0.55");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("55")) <= 0) {
            return new BigDecimal("0.5");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("75")) <= 0) {
            return new BigDecimal("0.45");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("100")) <= 0) {
            return new BigDecimal("0.4");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("125")) <= 0) {
            return new BigDecimal("0.35");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("160")) <= 0) {
            return new BigDecimal("0.3");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("200")) <= 0) {
            return new BigDecimal("0.25");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("250")) <= 0) {
            return new BigDecimal("0.2");
        } else if (thirtyDayVolume.compareTo(new BigDecimal("320")) <= 0) {
            return new BigDecimal("0.15");
        } else {
            return new BigDecimal("0.10");
        }
    }

    public static BigDecimal getFeeInDecimals(BigDecimal thirtyDayVolume) {
        return getFeeInPercentage(thirtyDayVolume).divide(new BigDecimal("100"), 4,
                BigDecimal.ROUND_UNNECESSARY);
    }

    public static BigDecimal lowestThirtyDayVolume(BigDecimal fee) {
        if (fee.compareTo(new BigDecimal("1.00")) == 0) {
            return new BigDecimal("0");
        } else if (fee.compareTo(new BigDecimal("0.95")) == 0) {
            return new BigDecimal("1");
        } else if (fee.compareTo(new BigDecimal("0.90")) == 0) {
            return new BigDecimal("2.5");
        } else if (fee.compareTo(new BigDecimal("0.85")) == 0) {
            return new BigDecimal("4");
        } else if (fee.compareTo(new BigDecimal("0.80")) == 0) {
            return new BigDecimal("6.5");
        } else if (fee.compareTo(new BigDecimal("0.75")) == 0) {
            return new BigDecimal("9");
        } else if (fee.compareTo(new BigDecimal("0.70")) == 0) {
            return new BigDecimal("12");
        } else if (fee.compareTo(new BigDecimal("0.65")) == 0) {
            return new BigDecimal("18");
        } else if (fee.compareTo(new BigDecimal("0.60")) == 0) {
            return new BigDecimal("25");
        } else if (fee.compareTo(new BigDecimal("0.55")) == 0) {
            return new BigDecimal("32");
        } else if (fee.compareTo(new BigDecimal("0.50")) == 0) {
            return new BigDecimal("40");
        } else if (fee.compareTo(new BigDecimal("0.45")) == 0) {
            return new BigDecimal("55");
        } else if (fee.compareTo(new BigDecimal("0.40")) == 0) {
            return new BigDecimal("75");
        } else if (fee.compareTo(new BigDecimal("0.35")) == 0) {
            return new BigDecimal("100");
        } else if (fee.compareTo(new BigDecimal("0.30")) == 0) {
            return new BigDecimal("125");
        } else if (fee.compareTo(new BigDecimal("0.25")) == 0) {
            return new BigDecimal("160");
        } else if (fee.compareTo(new BigDecimal("0.20")) == 0) {
            return new BigDecimal("200");
        } else if (fee.compareTo(new BigDecimal("0.15")) == 0) {
            return new BigDecimal("250");
        } else if (fee.compareTo(new BigDecimal("0.10")) == 0) {
            return new BigDecimal("320");
        } else if (fee.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        throw new IllegalStateException("Could not find fee bracket");
    }

}
