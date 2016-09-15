package com.bitso;

import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.bitso.exchange.Entry;
import com.bitso.utils.Util;

public class BitsoLedgerEntryTest {
    @InjectMocks
    private List<JSONObject> jsonList;
    List<BitsoLedgerEntry> ledgerEntries;
    @InjectMocks
    public final static String BITSO_LEDGER_ENTRY_FULL_NEW_ACCOUNT = "/Users/Gerardo/Documents/bitso/libraries/bitsoExJava/bitso-java/src/test/java/com/bitso/bitsoLedgerEntryTest.json";

    @Mock
    JSONObject json;

    @Before
    public void setUp() {
        jsonList = Util.readFileToJson(BITSO_LEDGER_ENTRY_FULL_NEW_ACCOUNT);
        if (jsonList != null && jsonList.size() > 0) {
            ledgerEntries = new ArrayList<BitsoLedgerEntry>();
            for (JSONObject obj : jsonList) {
                ledgerEntries.add(new BitsoLedgerEntry(obj));
            }
        }
    }

    /**
     * Method to validate if the BitsoLedgerEntry classes and their entries are not null. respectively.
     */
    @Test
    public void testBitsoLedgerEntryList() {
        for (BitsoLedgerEntry ledgerEntry : ledgerEntries) {
            if (ledgerEntry == null) {
                fail("BitsoLedgerEntry shouldn´t be null.");
            }
            if (ledgerEntry.getEntries() == null) {
                fail("BitsoLedgerEntry shouldn´t be null.");
            }
        }
    }

    /**
     * Method to validate the corresponding BitsoLedgerEntry class and its entries if null or not and how many
     * entries it has and its values. Tests methods: {@link com.bitso.BitsoLedgerEntry#getEntries()}
     * {@link com.bitso.exchange.Entry#getBalanceUpdates()} {@link com.bitso.exchange.Entry#getCreatedAt()}
     * {@link com.bitso.exchange.Entry#getDetailsMap()} {@link com.bitso.exchange.Entry#getOperation()}
     */
    @Test
    public void testBitsoLedgerEntries() {
        int i = 0;
        for (BitsoLedgerEntry ledgerEntry : ledgerEntries) {
            Assert.assertNotNull(ledgerEntry.getEntries());
            switch (i) {
                case 0:
                    // Tests the BitsoLedgerEntry with a new account, no funds, no transactions.
                    Assert.assertEquals(0, ledgerEntry.getEntries().size());
                    break;
                case 1:
                    // Tests the BitsoLedgerEntry with a new account with founds but no transactions, contains
                    // one entry.
                    Assert.assertEquals(1, ledgerEntry.getEntries().size());
                    for (Entry entry : ledgerEntry.getEntries()) {
                        Assert.assertNotNull(entry.getEntryId());
                        Assert.assertNotNull(entry.getBalanceUpdates());
                        Assert.assertEquals(1, entry.getBalanceUpdates().size());
                        Assert.assertEquals(2, entry.getDetailsMap().size());
                        Assert.assertNotNull(entry.getCreatedAt());
                        Assert.assertNotNull(entry.getDetailsMap());
                        Assert.assertNotNull(entry.getOperation());
                        Assert.assertEquals("82159a2848f2a15cb292038545bb3c4d", entry.getEntryId());
                        Assert.assertEquals(1, entry.getBalanceUpdates().size());
                        Assert.assertEquals(new BigDecimal(10), entry.getBalanceUpdates().get(0).getAmount());
                        Assert.assertEquals("btc", entry.getBalanceUpdates().get(0).getCurrency());
                        Assert.assertEquals(Month.SEPTEMBER, entry.getCreatedAt().getMonth());
                        Assert.assertEquals(DayOfWeek.TUESDAY, entry.getCreatedAt().getDayOfWeek());
                        Assert.assertEquals(2016, entry.getCreatedAt().getYear());
                        Assert.assertEquals("-05:00", entry.getCreatedAt().getZone().getId());
                        Assert.assertEquals("adm", entry.getDetailsMap().get("method"));
                        Assert.assertEquals("Admin Adjustment", entry.getDetailsMap().get("method_name"));
                        Assert.assertEquals("funding", entry.getOperation());
                    }
                    break;
                case 2:
                    // Tests the BitsoLedgerEntry with a new account with funds and with transactions,
                    // contains three entries
                    int k = i - 2;
                    Assert.assertEquals(3, ledgerEntry.getEntries().size());
                    for (Entry entry : ledgerEntry.getEntries()) {
                        switch (k) {
                            case 0:
                                Assert.assertNotNull(entry.getEntryId());
                                Assert.assertNotNull(entry.getBalanceUpdates());
                                Assert.assertEquals(1, entry.getBalanceUpdates().size());
                                Assert.assertEquals(0, entry.getDetailsMap().size());
                                Assert.assertNotNull(entry.getCreatedAt());
                                Assert.assertNotNull(entry.getDetailsMap());
                                Assert.assertNotNull(entry.getOperation());
                                Assert.assertEquals("01c45f5c58b1333c0ebbf9b4f3edc252", entry.getEntryId());
                                Assert.assertEquals(Month.SEPTEMBER, entry.getCreatedAt().getMonth());
                                Assert.assertEquals(2016, entry.getCreatedAt().getYear());
                                Assert.assertEquals("-05:00", entry.getCreatedAt().getZone().getId());
                                Assert.assertEquals(-31,
                                        entry.getBalanceUpdates().get(k).getAmount().intValue());
                                Assert.assertEquals("mxn", entry.getBalanceUpdates().get(k).getCurrency());
                                Assert.assertNull(entry.getDetailsMap().get("method"));
                                Assert.assertNull(entry.getDetailsMap().get("method_name"));
                                Assert.assertEquals("fee", entry.getOperation());
                                break;
                            case 1:
                                Assert.assertNotNull(entry.getEntryId());
                                Assert.assertNotNull(entry.getBalanceUpdates());
                                Assert.assertEquals(2, entry.getBalanceUpdates().size());
                                Assert.assertEquals(2, entry.getDetailsMap().size());
                                Assert.assertNotNull(entry.getCreatedAt());
                                Assert.assertNotNull(entry.getDetailsMap());
                                Assert.assertNotNull(entry.getOperation());
                                Assert.assertEquals(Month.SEPTEMBER, entry.getCreatedAt().getMonth());
                                Assert.assertEquals(DayOfWeek.MONDAY, entry.getCreatedAt().getDayOfWeek());
                                Assert.assertEquals(2016, entry.getCreatedAt().getYear());
                                Assert.assertEquals("-05:00", entry.getCreatedAt().getZone().getId());
                                Assert.assertEquals(3096,
                                        entry.getBalanceUpdates().get(k - 1).getAmount().intValue());
                                Assert.assertEquals("mxn",
                                        entry.getBalanceUpdates().get(k - 1).getCurrency());
                                Assert.assertEquals(-1,
                                        entry.getBalanceUpdates().get(k).getAmount().intValue());
                                Assert.assertEquals("btc", entry.getBalanceUpdates().get(k).getCurrency());
                                Assert.assertEquals(
                                        "0ghc3Xo1ral7mn06tim1c5OlsdmvRsO4oasj2h3lfoR2cj62M3Xqhqm350n3nVsg",
                                        entry.getDetailsMap().get("oid"));
                                Assert.assertEquals(936, entry.getDetailsMap().get("tid"));
                                Assert.assertEquals("trade", entry.getOperation());
                                break;
                            case 2:
                                Assert.assertNotNull(entry.getEntryId());
                                Assert.assertNotNull(entry.getBalanceUpdates());
                                Assert.assertEquals(1, entry.getBalanceUpdates().size());

                                Assert.assertNotNull(entry.getCreatedAt());
                                Assert.assertNotNull(entry.getDetailsMap());
                                Assert.assertNotNull(entry.getOperation());
                                Assert.assertEquals(Month.SEPTEMBER, entry.getCreatedAt().getMonth());
                                Assert.assertEquals(DayOfWeek.TUESDAY, entry.getCreatedAt().getDayOfWeek());
                                Assert.assertEquals(2016, entry.getCreatedAt().getYear());
                                Assert.assertEquals("-05:00", entry.getCreatedAt().getZone().getId());
                                Assert.assertEquals(new BigDecimal(10),
                                        entry.getBalanceUpdates().get(k - 2).getAmount());
                                Assert.assertEquals("btc",
                                        entry.getBalanceUpdates().get(k - 2).getCurrency());
                                Assert.assertEquals("adm", entry.getDetailsMap().get("method"));
                                Assert.assertEquals("Admin Adjustment",
                                        entry.getDetailsMap().get("method_name"));
                                Assert.assertEquals("funding", entry.getOperation());
                                break;
                        }
                        k++;
                    }
                    break;
            }
            i++;
        }
    }
}
