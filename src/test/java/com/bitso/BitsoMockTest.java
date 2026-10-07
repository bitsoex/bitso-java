package com.bitso;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.bitso.exceptions.BitsoAPIException;
import com.bitso.exchange.BookInfo;
import com.bitso.helpers.Helpers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

public class BitsoMockTest extends BitsoTest {
    private List<BookInfo> mockAvailableBooks;
    private List<BitsoTicker> mockTicker;
    private BitsoOrderBook mockOrderBook;
    private BitsoAccountStatus mockAccountStatus;
    private BitsoBalance mockBalance;
    private BitsoFee mockFee;
    private Map<String, String> mockBitsoBanks;
    private List<BitsoFunding> mockFundings;
    private BitsoTrade[] mockTrades;

    private BitsoTransactions mockTransactions;
    private List<BitsoWithdrawal> mockWithdrawals;

    @BeforeEach
    public void setUp() throws JSONException, IOException, BitsoAPIException {
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
            setUpWithdrawals(Helpers.getJSONFromFile("privateWithdrawals.json"));
            setUpFundings(Helpers.getJSONFromFile("privateFundings.json"));
            setUpTrades(Helpers.getJSONFromFile("privateUserTrades.json"));
            setUpBitsoBanks(Helpers.getJSONFromFile("privateBankCodes.json"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUpMockitoActions() throws JSONException, IOException,
            BitsoAPIException {
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
        Mockito.when(mBitso.getWithdrawals(null)).thenReturn(mockWithdrawals);
        Mockito.when(mBitso.getFundings(null)).thenReturn(mockFundings);
        Mockito.when(mBitso.getUserTrades(null)).thenReturn(mockTrades);
        Mockito.when(mBitso.getOpenOrders(anyString())).thenReturn(List.of());
        JSONArray orders = Helpers.getJSONFromFile("privateOpenOrders.json").getJSONArray("payload");
        var one = List.of(new BitsoOrder(orders.getJSONObject(0)));
        one.get(0).setUnfilledAmount(BigDecimal.ZERO);
        Mockito.when(mBitso.getOpenOrders("btc_mxn")).thenReturn(one);
        var lookup = List.of(one.get(0), new BitsoOrder(orders.getJSONObject(1)));
        lookup.get(1).setUnfilledAmount(BigDecimal.ZERO);
        var mxnbOrder = new JSONObject();
        mxnbOrder.put("book", "btc_mxn");
        mxnbOrder.put("original_amount", "0.001");
        mxnbOrder.put("unfilled_amount", "0.001");
        mxnbOrder.put("original_value", "1");
        mxnbOrder.put("created_at", "1791333670083");
        mxnbOrder.put("updated_at", "1791333676822");
        mxnbOrder.put("side", "buy");
        mxnbOrder.put("status", "open");
        mxnbOrder.put("type", "limit");
        mxnbOrder.put("settle_minor", "mxnb");
        Mockito.when(mBitso.lookupOrders(eq("mxnbOrder"))).thenReturn(List.of(new BitsoOrder(mxnbOrder)));
        Mockito.when(mBitso.lookupOrders(any(), any())).thenReturn(lookup);
        Mockito.when(mBitso.cancelAllOrders()).thenReturn(new String[0]);
        Mockito.when(mBitso.getBanks()).thenReturn(mockBitsoBanks);
        Mockito.when(mBitso.placeOrder(argThat(req -> req != null && "mxnb".equals(req.getSettleMinor()))))
                        .thenReturn("mxnbOrder");
        Mockito.when(mBitso.placeOrder(argThat(req -> req != null && req.getSettleMinor() == null)))
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
        mockAvailableBooks = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); i++) {
            mockAvailableBooks.add(new BookInfo(arr.getJSONObject(i)));
        }
    }

    private void setUpTicker(JSONObject o) {
        JSONArray array = o.getJSONArray("payload");
        int totalElements = array.length();
        mockTicker = new ArrayList<>(totalElements);

        for (int i = 0; i < totalElements; i++) {
            mockTicker.add(new BitsoTicker(array.getJSONObject(i)));
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

    private void setUpWithdrawals(JSONObject o) {
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            mockWithdrawals = new ArrayList<>(totalElements);
            for (int i = 0; i < totalElements; i++) {
                mockWithdrawals.add(new BitsoWithdrawal(payload.getJSONObject(i)));
            }
        }
    }

    public void setUpFundings(JSONObject o) {
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            mockFundings = new ArrayList<>(totalElements);
            for (int i = 0; i < totalElements; i++) {
                mockFundings.add(new BitsoFunding(payload.getJSONObject(i)));
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
            var availableBooks = mBitso.getAvailableBooks();
            assertNotNull(availableBooks);
            for (BookInfo bookInfo : availableBooks) {
                BitsoOrderBook bitsoOrderBook = mBitso.getOrderBook(bookInfo.getBook());
                assertTrue(nullCheck(bitsoOrderBook, BitsoOrderBook.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Override
    public void testTrades() throws JSONException, IOException, BitsoAPIException {
        var availableBooks = mBitso.getAvailableBooks();
        assertNotNull(availableBooks);

        for (BookInfo bookInfo : availableBooks) {
            BitsoTransactions bitsoTransaction = mBitso.getTrades(bookInfo.getBook());
            assertTrue(nullCheck(bitsoTransaction, BitsoTransactions.class));
        }
    }

    @Test
    @Override
    public void testWithdrawals() throws JSONException, IOException, BitsoAPIException {
        var withdrawals = mBitso.getWithdrawals(null);
        assertNotNull(withdrawals);
        for (BitsoWithdrawal bitsoWithdrawal : withdrawals) {
            assertTrue(nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }
    }

    @Test
    @Override
    public void testFundings() throws JSONException, IOException, BitsoAPIException {
        var fundings = mBitso.getFundings(null);
        assertNotNull(fundings);
        for (BitsoFunding bitsoFunding : fundings) {
            assertTrue(nullCheck(bitsoFunding, BitsoFunding.class));
        }
    }

    @Test
    @Override
    public void testUserTrades() throws JSONException, IOException, BitsoAPIException {
        BitsoTrade[] trades = mBitso.getUserTrades(null);
        assertNotNull(trades);
        int totalElements = trades.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoTrade current : trades) {
            assertTrue(nullCheck(current, BitsoTrade.class));
        }
    }

    @Test
    public void testOrderTrades() throws JSONException, IOException, BitsoAPIException {
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
    public void testTrading() throws JSONException, IOException, BitsoAPIException {
        //do nothing
    }
}
