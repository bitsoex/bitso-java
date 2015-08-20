package com.bitso;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

public class BitsoFeeStructureTest {

    @Test
    public void testBrackets() {
        // 1% (0-1)
        assertTrue(new BigDecimal("1").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("0"))) == 0);
        assertTrue(new BigDecimal("1").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("0.5"))) == 0);
        assertTrue(new BigDecimal("1").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("1"))) == 0);

        // 0.95% (1-2.5)
        assertTrue(new BigDecimal("0.95").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("1.00000001"))) == 0);
        assertTrue(new BigDecimal("0.95").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("2"))) == 0);
        assertTrue(new BigDecimal("0.95").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("2.5"))) == 0);

        // 0.90% (2.5-4)
        assertTrue(new BigDecimal("0.9").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("2.50000001"))) == 0);
        assertTrue(new BigDecimal("0.9").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("4"))) == 0);

        // 0.85% (4-6.5)
        assertTrue(new BigDecimal("0.85").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("4.00000001"))) == 0);
        assertTrue(new BigDecimal("0.85").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("6.5"))) == 0);

        // 0.80% (6.5-9)
        assertTrue(new BigDecimal("0.8").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("6.50000001"))) == 0);
        assertTrue(new BigDecimal("0.8").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("9"))) == 0);

        // 0.75% (9-12)
        assertTrue(new BigDecimal("0.75").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("9.00000001"))) == 0);
        assertTrue(new BigDecimal("0.75").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("12"))) == 0);

        // 0.70% (12-18)
        assertTrue(new BigDecimal("0.7").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("12.00000001"))) == 0);
        assertTrue(new BigDecimal("0.7").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("18"))) == 0);

        // 0.65% (18-25)
        assertTrue(new BigDecimal("0.65").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("18.00000001"))) == 0);
        assertTrue(new BigDecimal("0.65").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("25"))) == 0);

        // 0.60% (25-32)
        assertTrue(new BigDecimal("0.6").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("25.00000001"))) == 0);
        assertTrue(new BigDecimal("0.6").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("32"))) == 0);

        // 0.55% (32-40)
        assertTrue(new BigDecimal("0.55").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("32.00000001"))) == 0);
        assertTrue(new BigDecimal("0.55").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("40"))) == 0);

        // 0.50% (40-55)
        assertTrue(new BigDecimal("0.5").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("40.00000001"))) == 0);
        assertTrue(new BigDecimal("0.5").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("55"))) == 0);

        // 0.45% (55-75)
        assertTrue(new BigDecimal("0.45").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("55.00000001"))) == 0);
        assertTrue(new BigDecimal("0.45").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("75"))) == 0);

        // 0.40% (75-100)
        assertTrue(new BigDecimal("0.4").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("75.00000001"))) == 0);
        assertTrue(new BigDecimal("0.4").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("100"))) == 0);

        // 0.35% (100-125)
        assertTrue(new BigDecimal("0.35").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("100.00000001"))) == 0);
        assertTrue(new BigDecimal("0.35").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("125"))) == 0);

        // 0.30% (125-160)
        assertTrue(new BigDecimal("0.3").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("125.00000001"))) == 0);
        assertTrue(new BigDecimal("0.3").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("160"))) == 0);

        // 0.25% (160-200)
        assertTrue(new BigDecimal("0.25").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("160.00000001"))) == 0);
        assertTrue(new BigDecimal("0.25").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("200"))) == 0);

        // 0.20% (200-250)
        assertTrue(new BigDecimal("0.2").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("200.00000001"))) == 0);
        assertTrue(new BigDecimal("0.2").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("250"))) == 0);

        // 0.15% (250-320)
        assertTrue(new BigDecimal("0.15").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("250.00000001"))) == 0);
        assertTrue(new BigDecimal("0.15").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("320"))) == 0);

        // 0.10% (320+)
        assertTrue(new BigDecimal("0.1").compareTo(BitsoFeeStructure.getFeeInPercentage(new BigDecimal("320.00000001"))) == 0);
    }
}
