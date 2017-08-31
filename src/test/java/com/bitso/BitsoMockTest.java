package com.bitso;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.mockito.Mockito;

import com.bitso.exceptions.BitsoAPIException;
import com.bitso.exchange.BookInfo;
import com.bitso.helpers.Helpers;

public class BitsoMockTest extends BitsoTest {
    private BookInfo[] mockAvailableBooks;
    private BitsoTicker[] mockTicker;
    private BitsoOrderBook mockOrderBook;
    private BitsoAccountStatus mockAccountStatus;
    private BitsoBalance mockBalance;
    private BitsoFee mockFee;
    private BitsoOrder[] mockOpenOrders;
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

    @Before
    public void setUp() throws Exception {
        mBitso = Mockito.mock(Bitso.class);
        setUpTestMocks();
        setUpMockitoActions();
    }

    private void setUpTestMocks() {
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
        setUpFundingDestionation(Helpers.getJSONFromFile("privateFundingDestination.json"));
        setUpBitsoBanks(Helpers.getJSONFromFile("privateBankCodes.json"));
    }

    private void setUpMockitoActions() throws BitsoAPIException {
        Mockito.when(mBitso.getAvailableBooks()).thenReturn(mockAvailableBooks);
        Mockito.when(mBitso.getTicker()).thenReturn(mockTicker);
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
        Mockito.when(mBitso.getOpenOrders("btc_mxn")).thenReturn(mockOpenOrders);
        Mockito.when(mBitso.getOpenOrders("eth_mxn")).thenReturn(mockOpenOrders);
        Mockito.when(mBitso.getOpenOrders("xrp_btc")).thenReturn(mockOpenOrders);
        Mockito.when(mBitso.getOpenOrders("xrp_mxn")).thenReturn(mockOpenOrders);
        Mockito.when(mBitso.getOpenOrders("eth_btc")).thenReturn(mockOpenOrders);
        Mockito.when(mBitso.getOpenOrders("bch_btc")).thenReturn(mockOpenOrders);
        Mockito.when((mBitso.fundingDestination("fund_currency=btc"))).thenReturn(mockFundingDestination);
        Mockito.when((mBitso.fundingDestination("fund_currency=eth"))).thenReturn(mockFundingDestination);
        Mockito.when((mBitso.fundingDestination("fund_currency=mxn"))).thenReturn(mockFundingDestination);
        Mockito.when(mBitso.getBanks()).thenReturn(mockBitsoBanks);
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

        JSONObject ledger = Helpers.getJSONFromFile(files[0]);
        JSONObject ledgerTrades = Helpers.getJSONFromFile(files[1]);
        JSONObject ledgerFees = Helpers.getJSONFromFile(files[2]);
        JSONObject ledgerFunds = Helpers.getJSONFromFile(files[3]);
        JSONObject ledgerWithdraws = Helpers.getJSONFromFile(files[4]);

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

    @Override
    public void testOrderBook() throws BitsoAPIException {
        BookInfo[] availableBooks = mBitso.getAvailableBooks();
        assertEquals(availableBooks != null, true);
        for (BookInfo bookInfo : availableBooks) {
            BitsoOrderBook bitsoOrderBook = mBitso.getOrderBook(bookInfo.getBook());
            assertEquals(nullCheck(bitsoOrderBook, BitsoOrderBook.class), true);
        }
    }

    @Override
    public void testTrades() throws InterruptedException, BitsoAPIException {
        BookInfo[] availableBooks = mBitso.getAvailableBooks();
        assertEquals(availableBooks != null, true);

        for (BookInfo bookInfo : availableBooks) {
            BitsoTransactions bitsoTransaction = mBitso.getTrades(bookInfo.getBook());
            assertEquals(nullCheck(bitsoTransaction, BitsoTransactions.class), true);
        }
    }

    @Override
    public void testLedger() throws InterruptedException, BitsoAPIException {
        int totalElements = 0;

        BitsoOperation[] defaultLedger = mBitso.getLedger("");
        assertEquals(defaultLedger != null, true);
        totalElements = defaultLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoOperation bitsoOperation : defaultLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
        }

        BitsoOperation[] tradesLedger = mBitso.getLedger("trades");
        assertEquals(tradesLedger != null, true);
        totalElements = tradesLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoOperation bitsoOperation : tradesLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals(bitsoOperation.getOperationDescription(), "trade");
        }

        BitsoOperation[] feesLedger = mBitso.getLedger("fees");
        assertEquals(feesLedger != null, true);
        totalElements = feesLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoOperation bitsoOperation : feesLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals(bitsoOperation.getOperationDescription(), "fee");
        }

        BitsoOperation[] fundingsLedger = mBitso.getLedger("fundings");
        assertEquals(fundingsLedger != null, true);
        totalElements = fundingsLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoOperation bitsoOperation : fundingsLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals(bitsoOperation.getOperationDescription(), "funding");
        }

        BitsoOperation[] withdrawalsLedger = mBitso.getLedger("withdrawals");
        assertEquals(withdrawalsLedger != null, true);
        totalElements = withdrawalsLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoOperation bitsoOperation : withdrawalsLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals(bitsoOperation.getOperationDescription(), "withdrawal");
        }
    }

    @Override
    public void testWithdrawals() throws InterruptedException, BitsoAPIException {
        BitsoWithdrawal[] withdrawals = mBitso.getWithdrawals(null);
        assertEquals(withdrawals != null, true);
        for (BitsoWithdrawal bitsoWithdrawal : withdrawals) {
            assertEquals(true, nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }
    }

    @Override
    public void tesFundings() throws InterruptedException, BitsoAPIException {
        BitsoFunding[] fundings = mBitso.getFundings(null);
        assertEquals(fundings != null, true);
        for (BitsoFunding bitsoFunding : fundings) {
            assertEquals(true, nullCheck(bitsoFunding, BitsoFunding.class));
        }
    }

    @Override
    public void testUserTrades() throws InterruptedException, BitsoAPIException {
        BitsoTrade[] trades = mBitso.getUserTrades(null);
        assertEquals(trades != null, true);
        int totalElements = trades.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoTrade current : trades) {
            assertEquals(true, nullCheck(current, BitsoTrade.class));
        }
    }

    @Override
    public void testTrading() {
        System.out.println("This test is overriden");
    }
}
