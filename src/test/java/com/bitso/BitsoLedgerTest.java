package com.bitso;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bitso.exchange.Entry;
import com.bitso.utils.Util;

@RunWith(MockitoJUnitRunner.class)
public class BitsoLedgerTest {
    private List<JSONObject> jsonList;
    private List<BitsoLedger> ledgerList;
    public final static String BITSO_LEDGER_TEST_FILE = "extras/bitsoLedgerTest.json";
    @Mock
    BitsoLedger mockLedgerList;
    @Mock
    List<Entry> mockEntries;

    @Before
    public void setUp() {
        jsonList = Util.readFileToJson(BITSO_LEDGER_TEST_FILE);
        if (jsonList != null && jsonList.size() > 0) {
            ledgerList = new ArrayList<BitsoLedger>();
            for (JSONObject obj : jsonList) {
                ledgerList.add(new BitsoLedger(obj));
            }
        }
        mockLedgerList = mock(BitsoLedger.class);
    }

    /**
     * Method to validate if the BitsoLedger classes created from the test file and their entries are not null
     * respectively.
     */
    @Test
    public void testBitsoLedgerList() {
        for (BitsoLedger ledger : ledgerList) {
            if (ledger == null) {
                fail("BitsoLedger class shouldn´t be null.");
            }
            if (ledger.getEntries() == null) {
                fail("The list of entries of the BitsoLedger class shouldn´t be null.");
            }
        }
    }

    /**
     * Test that verifies the execution of {@link com.bitso.BitsoLedger#getEntries()}.
     */
    @Test
    public void testVerifyGetEntries() {
        mockLedgerList.getEntries();
        verify(mockLedgerList).getEntries();
        when(mockLedgerList.getEntries()).thenReturn(ledgerList.get(0).getEntries());
    }

    /**
     * Validates the methods and fields of: {@link com.bitso.BitsoLedger#getEntries()}
     * {@link com.bitso.exchange.Entry#entryId}, {@link com.bitso.exchange.Entry#balances},
     * {@link com.bitso.exchange.Entry#createdAt}, {@link com.bitso.exchange.Entry#detailsMap} and
     * {@link com.bitso.exchange.Entry#operation}.
     */
    @Test
    public void testBitsoLedgerEntriesValues() {
        int i = 0;
        for (BitsoLedger ledger : ledgerList) {
            Assert.assertNotNull(ledger.getEntries());
            switch (i) {
                case 0:
                    // Tests the BitsoLedger with a new account, no funds, no transactions.
                    Assert.assertEquals(0, ledger.getEntries().size());
                    when(mockEntries.size()).thenReturn(ledger.getEntries().size());
                    when(mockEntries.get(0)).thenThrow(new IndexOutOfBoundsException());
                    break;
                case 1:
                    // Tests the BitsoLedger with a new account with founds but no transactions, contains
                    // one entry.
                    Assert.assertEquals(1, ledger.getEntries().size());
                    when(mockEntries.size()).thenReturn(ledger.getEntries().size());
                    when(mockEntries.get(1)).thenThrow(new IndexOutOfBoundsException());
                    for (Entry entry : ledger.getEntries()) {
                        Assert.assertNotNull(entry.entryId);
                        Assert.assertNotNull(entry.balances);
                        Assert.assertEquals(1, entry.balances.size());
                        Assert.assertEquals(2, entry.detailsMap.size());
                        Assert.assertNotNull(entry.createdAt);
                        Assert.assertNotNull(entry.detailsMap);
                        Assert.assertNotNull(entry.operation);
                        Assert.assertEquals("82159a2848f2a15cb292038545bb3c4d", entry.entryId);
                        Assert.assertEquals(1, entry.balances.size());
                        Assert.assertEquals(new BigDecimal(10), entry.balances.get(0).getAmount());
                        Assert.assertEquals("btc", entry.balances.get(0).getCurrency());
                        Assert.assertEquals(Month.SEPTEMBER, entry.createdAt.getMonth());
                        Assert.assertEquals(DayOfWeek.TUESDAY, entry.createdAt.getDayOfWeek());
                        Assert.assertEquals(2016, entry.createdAt.getYear());
                        Assert.assertEquals("-05:00", entry.createdAt.getZone().getId());
                        Assert.assertEquals("adm", entry.detailsMap.get("method"));
                        Assert.assertEquals("Admin Adjustment", entry.detailsMap.get("method_name"));
                        Assert.assertEquals("funding", entry.operation);
                    }
                    break;
                case 2:
                    // Tests the BitsoLedgerEntry with a new account with funds and with transactions,
                    // contains three entries
                    int k = i - 2;
                    Assert.assertEquals(3, ledger.getEntries().size());
                    when(mockEntries.size()).thenReturn(ledger.getEntries().size());
                    when(mockEntries.get(3)).thenThrow(new IndexOutOfBoundsException());
                    for (Entry entry : ledger.getEntries()) {
                        switch (k) {
                            case 0:
                                Assert.assertNotNull(entry.entryId);
                                Assert.assertNotNull(entry.balances);
                                Assert.assertEquals(1, entry.balances.size());
                                Assert.assertEquals(0, entry.detailsMap.size());
                                Assert.assertNotNull(entry.createdAt);
                                Assert.assertNotNull(entry.detailsMap);
                                Assert.assertNotNull(entry.operation);
                                Assert.assertEquals("01c45f5c58b1333c0ebbf9b4f3edc252", entry.entryId);
                                Assert.assertEquals(Month.SEPTEMBER, entry.createdAt.getMonth());
                                Assert.assertEquals(2016, entry.createdAt.getYear());
                                Assert.assertEquals("-05:00", entry.createdAt.getZone().getId());
                                Assert.assertEquals(-31, entry.balances.get(k).getAmount().intValue());
                                Assert.assertEquals("mxn", entry.balances.get(k).getCurrency());
                                Assert.assertNull(entry.detailsMap.get("method"));
                                Assert.assertNull(entry.detailsMap.get("method_name"));
                                Assert.assertEquals("fee", entry.operation);
                                break;
                            case 1:
                                Assert.assertNotNull(entry.entryId);
                                Assert.assertNotNull(entry.balances);
                                Assert.assertEquals(2, entry.balances.size());
                                Assert.assertEquals(2, entry.detailsMap.size());
                                Assert.assertNotNull(entry.createdAt);
                                Assert.assertNotNull(entry.detailsMap);
                                Assert.assertNotNull(entry.operation);
                                Assert.assertEquals(Month.SEPTEMBER, entry.createdAt.getMonth());
                                Assert.assertEquals(DayOfWeek.MONDAY, entry.createdAt.getDayOfWeek());
                                Assert.assertEquals(2016, entry.createdAt.getYear());
                                Assert.assertEquals("-05:00", entry.createdAt.getZone().getId());
                                Assert.assertEquals(3096, entry.balances.get(k - 1).getAmount().intValue());
                                Assert.assertEquals("mxn", entry.balances.get(k - 1).getCurrency());
                                Assert.assertEquals(-1, entry.balances.get(k).getAmount().intValue());
                                Assert.assertEquals("btc", entry.balances.get(k).getCurrency());
                                Assert.assertEquals(
                                        "0ghc3Xo1ral7mn06tim1c5OlsdmvRsO4oasj2h3lfoR2cj62M3Xqhqm350n3nVsg",
                                        entry.detailsMap.get("oid"));
                                Assert.assertEquals(936, entry.detailsMap.get("tid"));
                                Assert.assertEquals("trade", entry.operation);
                                break;
                            case 2:
                                Assert.assertNotNull(entry.entryId);
                                Assert.assertNotNull(entry.balances);
                                Assert.assertEquals(1, entry.balances.size());

                                Assert.assertNotNull(entry.createdAt);
                                Assert.assertNotNull(entry.detailsMap);
                                Assert.assertNotNull(entry.operation);
                                Assert.assertEquals(Month.SEPTEMBER, entry.createdAt.getMonth());
                                Assert.assertEquals(DayOfWeek.TUESDAY, entry.createdAt.getDayOfWeek());
                                Assert.assertEquals(2016, entry.createdAt.getYear());
                                Assert.assertEquals("-05:00", entry.createdAt.getZone().getId());
                                Assert.assertEquals(new BigDecimal(10),
                                        entry.balances.get(k - 2).getAmount());
                                Assert.assertEquals("btc", entry.balances.get(k - 2).getCurrency());
                                Assert.assertEquals("adm", entry.detailsMap.get("method"));
                                Assert.assertEquals("Admin Adjustment", entry.detailsMap.get("method_name"));
                                Assert.assertEquals("funding", entry.operation);
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
