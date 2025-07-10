package com.bitso;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.bitso.exceptions.BitsoValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.bitso.exceptions.BitsoAPIException;
import com.bitso.exceptions.BitsoNullException;
import com.bitso.exceptions.BitsoPayloadException;
import com.bitso.exceptions.BitsoServerException;
import com.bitso.exchange.BookInfo;
import com.bitso.helpers.Helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class BitsoMockTest extends BitsoTest {
    private BookInfo[] mockAvailableBooks;
    private BitsoTicker[] mockTicker;
    private BitsoOrderBook mockOrderBook;
    private BitsoAccountStatus mockAccountStatus;
    private BitsoBalance mockBalance;
    private BitsoFee mockFee;
    private Map<String, String> mockBitsoBanks;
    private BitsoOperation[] mockLedgers;
    private BitsoOperation[] mockLedgersTrades;
    private BitsoOperation[] mockLedgersFees;
    private BitsoOperation[] mockLedgersFundings;
    private BitsoOperation[] mockLedgersWithdrawals;
    private BitsoFunding[] mockFundings;
    private BitsoTrade[] mockTrades;
    private Map<String, String> mockFundingDestination;

    private BitsoTransactions mockTransactions;
    BitsoWithdrawal[] mockWithdrawals;

    @BeforeEach
    public void setUp() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        mBitso = Mockito.mock(Bitso.class);
        setUpTestMocks();
        setUpMockitoActions();
    }

    private void setUpTestMocks() {
        try {
            setUpAvailableBooks(Helpers.getJSONFromFile("publicAvailableBooks.json"));
            setUpTicker(Helpers.getJSONFromFile("publicTicker.json"));
            setUpOrderBook(Helpers.getJSONFromFile("publicOrderBook.json"));
            setUpTransactions(Helpers.getJSONFromFile("publicTrades.json"));
            setUpAccountStatus(Helpers.getJSONFromFile("privateAccountStatus.json"));
            setUpAccountBalance(Helpers.getJSONFromFile("privateAccountBalance.json"));
            setUpFees(Helpers.getJSONFromFile("privateFees.json"));
            setUpLedgers();
            setUpWithdrawals(Helpers.getJSONFromFile("privateWithdrawals.json"));
            setUpFundings(Helpers.getJSONFromFile("privateFundings.json"));
            setUpTrades(Helpers.getJSONFromFile("privateUserTrades.json"));
            setUpFundingDestionation(
                    Helpers.getJSONFromFile("privateFundingDestination.json"));
            setUpBitsoBanks(Helpers.getJSONFromFile("privateBankCodes.json"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUpMockitoActions() throws JSONException, BitsoNullException, IOException,
            BitsoAPIException, BitsoPayloadException, BitsoServerException {
        Mockito.when(mBitso.getAvailableBooks()).thenReturn(mockAvailableBooks);
        Mockito.when(mBitso.getSignedAvailableBooks()).thenReturn(mockAvailableBooks);
        Mockito.when(mBitso.getTicker()).thenReturn(mockTicker);
        Mockito.when(mBitso.getSignedTicker()).thenReturn(mockTicker);
        Mockito.when(mBitso.getOrderBook("btc_mxn")).thenReturn(mockOrderBook);
        Mockito.when(mBitso.getOrderBook("eth_mxn")).thenReturn(mockOrderBook);
        Mockito.when(mBitso.getOrderBook("xrp_btc")).thenReturn(mockOrderBook);
        Mockito.when(mBitso.getOrderBook("xrp_mxn")).thenReturn(mockOrderBook);
        Mockito.when(mBitso.getOrderBook("eth_btc")).thenReturn(mockOrderBook);
        Mockito.when(mBitso.getOrderBook("bch_btc")).thenReturn(mockOrderBook);
        Mockito.when(mBitso.getTrades("btc_mxn")).thenReturn(mockTransactions);
        Mockito.when(mBitso.getTrades("eth_mxn")).thenReturn(mockTransactions);
        Mockito.when(mBitso.getTrades("xrp_btc")).thenReturn(mockTransactions);
        Mockito.when(mBitso.getTrades("xrp_mxn")).thenReturn(mockTransactions);
        Mockito.when(mBitso.getTrades("eth_btc")).thenReturn(mockTransactions);
        Mockito.when(mBitso.getTrades("bch_btc")).thenReturn(mockTransactions);
        Mockito.when(mBitso.getAccountStatus()).thenReturn(mockAccountStatus);
        Mockito.when(mBitso.getAccountBalance()).thenReturn(mockBalance);
        Mockito.when(mBitso.getFees()).thenReturn(mockFee);
        Mockito.when(mBitso.getLedger("")).thenReturn(mockLedgers);
        Mockito.when(mBitso.getLedger("trades")).thenReturn(mockLedgersTrades);
        Mockito.when(mBitso.getLedger("fees")).thenReturn(mockLedgersFees);
        Mockito.when(mBitso.getLedger("fundings")).thenReturn(mockLedgersFundings);
        Mockito.when(mBitso.getLedger("withdrawals")).thenReturn(mockLedgersWithdrawals);
        Mockito.when(mBitso.getWithdrawals(null)).thenReturn(mockWithdrawals);
        Mockito.when(mBitso.getFundings(null)).thenReturn(mockFundings);
        Mockito.when(mBitso.getUserTrades(null)).thenReturn(mockTrades);
        Mockito.when(mBitso.getOpenOrders(anyString())).thenReturn(new BitsoOrder[0]);
        BitsoOrder[] one = new BitsoOrder[1];
        JSONArray orders = Helpers.getJSONFromFile("privateOpenOrders.json").getJSONArray("payload");
        one[0] = new BitsoOrder(orders.getJSONObject(0));
        one[0].setUnfilledAmount(BigDecimal.ZERO);
        Mockito.when(mBitso.getOpenOrders("btc_mxn")).thenReturn(one);
        BitsoOrder[] lookup = new BitsoOrder[2];
        lookup[0] = one[0];
        lookup[1] = new BitsoOrder(orders.getJSONObject(1));
        lookup[1].setUnfilledAmount(BigDecimal.ZERO);
        Mockito.when(mBitso.lookupOrders(any(), any())).thenReturn(lookup);
        Mockito.when(mBitso.cancelAllOrders()).thenReturn(new String[0]);
        Mockito.when((mBitso.fundingDestination("fund_currency=btc"))).thenReturn(mockFundingDestination);
        Mockito.when((mBitso.fundingDestination("fund_currency=eth"))).thenReturn(mockFundingDestination);
        Mockito.when((mBitso.fundingDestination("fund_currency=mxn"))).thenReturn(mockFundingDestination);
        Mockito.when(mBitso.getBanks()).thenReturn(mockBitsoBanks);
        Mockito.when(mBitso.placeOrder(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn("genericOrder", generateOrderIds(10));
        Mockito.when(mBitso.placeLimitOrder(anyString(), any(), any(), any(), any(), any()))
                .thenReturn("limitOrder", generateOrderIds(15));
    }

    private final AtomicLong oidgen = new AtomicLong(12345);
    private String[] generateOrderIds(int count) {
        return Stream.generate(() -> "orderId" + oidgen.incrementAndGet())
                .limit(count).toArray(String[]::new);
    }

    private void setUpAvailableBooks(JSONObject o) {
        JSONArray arr = o.getJSONArray("payload");
        mockAvailableBooks = new BookInfo[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            mockAvailableBooks[i] = new BookInfo(arr.getJSONObject(i));
        }
    }

    private void setUpTicker(JSONObject o) {
        JSONArray array = o.getJSONArray("payload");
        int totalElements = array.length();
        mockTicker = new BitsoTicker[totalElements];

        for (int i = 0; i < totalElements; i++) {
            mockTicker[i] = new BitsoTicker(array.getJSONObject(i));
        }
    }

    private void setUpOrderBook(JSONObject o) {
        if (o.has("payload")) {
            mockOrderBook = new BitsoOrderBook(o.getJSONObject("payload"));
        }
    }

    private void setUpAccountStatus(JSONObject o) {
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            mockAccountStatus = new BitsoAccountStatus(payload);
        }
    }

    private void setUpAccountBalance(JSONObject o) {
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            mockBalance = new BitsoBalance(payload);
        }
    }

    private void setUpFees(JSONObject o) {
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            mockFee = new BitsoFee(payload);
        }
    }

    private void setUpLedgers() {
        String[] files = { "privateLedger.json", "privateLedgerTrades.json", "privateLedgerFees.json",
                "privateLedgerFundings.json", "privateLedgerWithdrawals.json" };

        JSONObject ledger = null;
        JSONObject ledgerTrades = null;
        JSONObject ledgerFees = null;
        JSONObject ledgerFunds = null;
        JSONObject ledgerWithdraws = null;

        try {
            ledger = Helpers.getJSONFromFile(files[0]);
            ledgerTrades = Helpers.getJSONFromFile(files[1]);
            ledgerFees = Helpers.getJSONFromFile(files[2]);
            ledgerFunds = Helpers.getJSONFromFile(files[3]);
            ledgerWithdraws = Helpers.getJSONFromFile(files[4]);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray payload = ledger.getJSONArray("payload");
        int totalElements = payload.length();

        mockLedgers = new BitsoOperation[totalElements];
        for (int i = 0; i < totalElements; i++) {
            mockLedgers[i] = new BitsoOperation(payload.getJSONObject(i));
        }

        payload = ledgerTrades.getJSONArray("payload");
        totalElements = payload.length();
        mockLedgersTrades = new BitsoOperation[totalElements];
        for (int i = 0; i < totalElements; i++) {
            mockLedgersTrades[i] = new BitsoOperation(payload.getJSONObject(i));
        }

        payload = ledgerFees.getJSONArray("payload");
        totalElements = payload.length();
        mockLedgersFees = new BitsoOperation[totalElements];
        for (int i = 0; i < totalElements; i++) {
            mockLedgersFees[i] = new BitsoOperation(payload.getJSONObject(i));
        }

        payload = ledgerFunds.getJSONArray("payload");
        totalElements = payload.length();
        mockLedgersFundings = new BitsoOperation[totalElements];
        for (int i = 0; i < totalElements; i++) {
            mockLedgersFundings[i] = new BitsoOperation(payload.getJSONObject(i));
        }

        payload = ledgerWithdraws.getJSONArray("payload");
        totalElements = payload.length();
        mockLedgersWithdrawals = new BitsoOperation[totalElements];
        for (int i = 0; i < totalElements; i++) {
            mockLedgersWithdrawals[i] = new BitsoOperation(payload.getJSONObject(i));
        }
    }

    private void setUpWithdrawals(JSONObject o) {
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            mockWithdrawals = new BitsoWithdrawal[totalElements];
            for (int i = 0; i < totalElements; i++) {
                mockWithdrawals[i] = new BitsoWithdrawal(payload.getJSONObject(i));
            }
        }
    }

    public void setUpFundings(JSONObject o) {
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            mockFundings = new BitsoFunding[totalElements];
            for (int i = 0; i < totalElements; i++) {
                mockFundings[i] = new BitsoFunding(payload.getJSONObject(i));
            }
        }
    }

    public void setUpTrades(JSONObject o) {
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            mockTrades = new BitsoTrade[totalElements];
            for (int i = 0; i < totalElements; i++) {
                mockTrades[i] = new BitsoTrade(payload.getJSONObject(i));
            }
        }
    }

    public void setUpFundingDestionation(JSONObject o) {
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            mockFundingDestination = new HashMap<String, String>();
            mockFundingDestination.put("account_identifier_name",
                    Helpers.getString(payload, "account_identifier_name"));
            mockFundingDestination.put("account_identifier",
                    Helpers.getString(payload, "account_identifier"));
        }
    }

    public void setUpTransactions(JSONObject o) {
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            mockTransactions = new BitsoTransactions(payload);
        }
    }

    private void setUpBitsoBanks(JSONObject o) {
        if (o.has("payload")) {
            mockBitsoBanks = new HashMap<String, String>();
            JSONArray payload = o.getJSONArray("payload");
            String currentBankCode = "";
            String currentBankName = "";
            JSONObject currentJSON = null;
            int totalElements = payload.length();
            for (int i = 0; i < totalElements; i++) {
                currentJSON = payload.getJSONObject(i);
                currentBankCode = Helpers.getString(currentJSON, "code");
                currentBankName = Helpers.getString(currentJSON, "name");
                mockBitsoBanks.put(currentBankCode, currentBankName);
            }

        }
    }

    @Test
    @Override
    public void testOrderBook() {
        try {
            BookInfo[] availableBooks = mBitso.getAvailableBooks();
            assertNotNull(availableBooks);
            for (BookInfo bookInfo : availableBooks) {
                BitsoOrderBook bitsoOrderBook = mBitso.getOrderBook(bookInfo.getBook());
                assertEquals(nullCheck(bitsoOrderBook, BitsoOrderBook.class), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Override
    public void testTrades() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        BookInfo[] availableBooks = mBitso.getAvailableBooks();
        assertNotNull(availableBooks);

        for (BookInfo bookInfo : availableBooks) {
            BitsoTransactions bitsoTransaction = mBitso.getTrades(bookInfo.getBook());
            assertTrue(nullCheck(bitsoTransaction, BitsoTransactions.class));
        }
    }

    @Test
    @Override
    public void testLedger() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        int totalElements = 0;

        BitsoOperation[] defaultLedger = mBitso.getLedger("");
        assertNotNull(defaultLedger);
        totalElements = defaultLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoOperation bitsoOperation : defaultLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
        }

        BitsoOperation[] tradesLedger = mBitso.getLedger("trades");
        assertNotNull(tradesLedger);
        totalElements = tradesLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoOperation bitsoOperation : tradesLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals("trade", bitsoOperation.getOperationDescription());
        }

        BitsoOperation[] feesLedger = mBitso.getLedger("fees");
        assertNotNull(feesLedger);
        totalElements = feesLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoOperation bitsoOperation : feesLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals("fee", bitsoOperation.getOperationDescription());
        }

        BitsoOperation[] fundingsLedger = mBitso.getLedger("fundings");
        assertNotNull(fundingsLedger);
        totalElements = fundingsLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoOperation bitsoOperation : fundingsLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals("funding", bitsoOperation.getOperationDescription());
        }

        BitsoOperation[] withdrawalsLedger = mBitso.getLedger("withdrawals");
        assertNotNull(withdrawalsLedger);
        totalElements = withdrawalsLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoOperation bitsoOperation : withdrawalsLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals("withdrawal", bitsoOperation.getOperationDescription());
        }
    }

    @Test
    @Override
    public void testWithdrawals() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        BitsoWithdrawal[] withdrawals = mBitso.getWithdrawals(null);
        assertNotNull(withdrawals);
        for (BitsoWithdrawal bitsoWithdrawal : withdrawals) {
            assertTrue(nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }
    }

    @Test
    @Override
    public void testFundings() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        BitsoFunding[] fundings = mBitso.getFundings(null);
        assertNotNull(fundings);
        for (BitsoFunding bitsoFunding : fundings) {
            assertTrue(nullCheck(bitsoFunding, BitsoFunding.class));
        }
    }

    @Test
    @Override
    public void testUserTrades() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        BitsoTrade[] trades = mBitso.getUserTrades(null);
        assertNotNull(trades);
        int totalElements = trades.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoTrade current : trades) {
            assertTrue(nullCheck(current, BitsoTrade.class));
        }
    }

    @Test
    public void testOrderTrades() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        int totalElements = 0;

        // TODO:
        // This should return a collection of 25 elements, not working limit default value
        BitsoTrade[] trades = mBitso.getUserTrades(null);
        assertNotNull(trades);
        totalElements = trades.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoTrade current : trades) {
            assertTrue(nullCheck(current, BitsoTrade.class));
        }
    }

    @Test
    @Override
    public void testTrading() throws JSONException, BitsoNullException, IOException, BitsoAPIException, BitsoPayloadException, BitsoServerException, InterruptedException, BitsoValidationException {
        //do nothing
    }
}
