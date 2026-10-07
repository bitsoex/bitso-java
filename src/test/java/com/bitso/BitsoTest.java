package com.bitso;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bitso.trading.OrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;

import com.bitso.BitsoBalance.Balance;
import com.bitso.exceptions.BitsoAPIException;
import com.bitso.exchange.BookInfo;
import com.bitso.exchange.Ticker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public abstract class BitsoTest {

    protected static final BigDecimal AMOUNT = BigDecimal.ONE.movePointLeft(3); // 0.001
    protected static BigDecimal minPrice = BigDecimal.ONE;
    protected static BigDecimal maxPrice = BigDecimal.ONE.movePointRight(8);
    protected Bitso mBitso;

    // Test public Rest API
    @Test
    public void testAvailableBooks() throws JSONException, IOException, BitsoAPIException {
        BookInfo[] books = mBitso.getAvailableBooks();
        assertNotNull(books);
        int totalElements = books.length;
        assertTrue(totalElements > 5, "Expected more than 5 books");
        for (BookInfo bookInfo : books) {
            assertTrue(nullCheck(bookInfo, BookInfo.class));
            if (bookInfo.getBook().equals("btc_mxn")) {
                log.warn("EUREKA! {}", bookInfo);
                minPrice = bookInfo.getMinPrice();
                maxPrice = bookInfo.getMaxPrice();
            }
        }
    }

    @Test
    public void testTicker() throws JSONException, IOException, BitsoAPIException {
        BitsoTicker[] tickers = mBitso.getTicker();
        throttlePublic();
        assertNotNull(tickers);
        int totalElements = tickers.length;
        assertTrue(totalElements > 5, "Expected more than 5 ticker entries");
        for (Ticker ticker : tickers) {
            assertTrue(nullCheck(ticker, BitsoTicker.class));
        }
    }

    @Test
    public void testOrderBook() throws JSONException, IOException, BitsoAPIException {
        BookInfo[] availableBooks = mBitso.getAvailableBooks();
        throttlePrivate();
        assertNotNull(availableBooks);
        if (availableBooks.length > 3) {
            // Test only the first ten books
            availableBooks = Arrays.copyOfRange(availableBooks, 0, 3);
        }
        for (BookInfo bookInfo : availableBooks) {
            BitsoOrderBook bitsoOrderBook = mBitso.getOrderBook(bookInfo.getBook());
            throttlePrivate();
            assertTrue(nullCheck(bitsoOrderBook, BitsoOrderBook.class));
            BitsoOrderBook bitsoOrderBookNoAggregate = mBitso.getOrderBook(bookInfo.getBook(), false);
            throttlePrivate();
            assertTrue(nullCheck(bitsoOrderBookNoAggregate, BitsoOrderBook.class));
            BitsoOrderBook bitsoOrderBookAggregate = mBitso.getOrderBook(bookInfo.getBook(), true);
            throttlePrivate();
            assertTrue(nullCheck(bitsoOrderBookAggregate, BitsoOrderBook.class));
        }
    }

    @Test
    public void testTrades() throws JSONException, IOException, BitsoAPIException {
        BookInfo[] availableBooks = mBitso.getAvailableBooks();
        assertNotNull(availableBooks);
        if (availableBooks.length > 3) {
            // Test only the first ten books
            availableBooks = Arrays.copyOfRange(availableBooks, 0, 3);
        }
        boolean first = true;
        for (BookInfo bookInfo : availableBooks) {
            int totalElements = 0;
            BitsoTransactions.Transaction[] innerTransactions;

            BitsoTransactions bitsoTransaction = mBitso.getTrades(bookInfo.getBook());
            assertTrue(nullCheck(bitsoTransaction, BitsoTransactions.class));

            throttlePublic();

            // This should return null due limit value is 0
            if (first) {
                BitsoTransactions bitsoTransactionCeroLimit = mBitso.getTrades(bookInfo.getBook(), "limit=0");
                assertNotNull(bitsoTransactionCeroLimit);
                throttlePublic();

                BitsoTransactions bitsoTransactionLowLimit = mBitso.getTrades(bookInfo.getBook(), "limit=1");
                totalElements = bitsoTransactionLowLimit.getTransactionsList().length;
                assertTrue((totalElements >= 0 && totalElements <= 1));
                throttlePublic();

                // This should return null due the limit value exceeds 100
                BitsoTransactions bitsoTransactionExcedingMaxLimit = mBitso.getTrades(bookInfo.getBook(),
                        "limit=1000");
                assertNotNull(bitsoTransactionExcedingMaxLimit);
                throttlePublic();
            }

            BitsoTransactions bitsoTransactionMaxLimit = mBitso.getTrades(bookInfo.getBook(), "limit=100");
            totalElements = bitsoTransactionMaxLimit.getTransactionsList().length;
            assertTrue((totalElements >= 0 && totalElements <= 100));

            throttlePublic();

            BitsoTransactions bitsoTransactionSortAsc = mBitso.getTrades(bookInfo.getBook(), "sort=asc");
            innerTransactions = bitsoTransaction.getTransactionsList();
            totalElements = innerTransactions.length;
            assertNotNull(bitsoTransactionSortAsc);
            assertTrue(totalElements >= 0, "Expected to see some trades");
            if (totalElements >= 5) {
                boolean orderAsc = true;
                int initialId = Integer.parseInt(innerTransactions[0].getTid());
                for (int i = 1; i < 5; i++) {
                    int current = Integer.parseInt(innerTransactions[i].getTid());
                    orderAsc = (current < initialId);
                    initialId = current;
                }
                assertTrue(orderAsc);
            }

            throttlePublic();

            // TODO:
            // This should return a correct DESC order and is not doing it
            BitsoTransactions bitsoTransactionSortDesc = mBitso.getTrades(bookInfo.getBook(), "sort=desc");
            innerTransactions = bitsoTransaction.getTransactionsList();
            totalElements = innerTransactions.length;
            assertNotNull(bitsoTransactionSortDesc);
            assertTrue(totalElements >= 0, "Expected to see some trades");
            if (totalElements >= 5) {
                boolean orderDesc = true;
                int initialId = Integer.parseInt(innerTransactions[0].getTid());
                for (int i = 1; i < 5; i++) {
                    int current = Integer.parseInt(innerTransactions[i].getTid());
                    orderDesc = (current < initialId);
                    initialId = current;
                }
                assertTrue(orderDesc);
            }

            throttlePublic();

            BitsoTransactions bitsoTransactionSortLimit = mBitso.getTrades(bookInfo.getBook(), "sort=asc",
                    "limit=15");
            totalElements = bitsoTransactionSortLimit.getTransactionsList().length;
            assertNotNull(bitsoTransactionSortLimit);
            assertTrue((totalElements >= 0 && totalElements <= 15));
            first = false;
        }
    }

    // Test private Rest API
    @Test
    public void testAccountStatus() throws JSONException, IOException, BitsoAPIException {
        BitsoAccountStatus bitsoAccountStatus = mBitso.getAccountStatus();
        assertTrue(nullCheck(bitsoAccountStatus, BitsoAccountStatus.class));
    }

    @Test
    public void testAccountBalance() throws JSONException, IOException, BitsoAPIException {
        BitsoBalance bitsoBalance = mBitso.getAccountBalance();
        assertTrue(nullCheck(bitsoBalance, BitsoBalance.class));
        HashMap<String, BitsoBalance.Balance> balances = bitsoBalance.getBalances();
        Set<String> keys = balances.keySet();
        for (String key : keys) {
            Balance currentBalance = balances.get(key);
            assertTrue(nullCheck(currentBalance, Balance.class));
        }
    }

    @Test
    public void testFees() throws JSONException, IOException, BitsoAPIException {
        BitsoFee bitsoFee = mBitso.getFees();
        assertTrue(nullCheck(bitsoFee, BitsoFee.class));
        HashMap<String, BitsoFee.Fee> fees = bitsoFee.getTradeFees();
        Set<String> keys = fees.keySet();
        for (String key : keys) {
            BitsoFee.Fee currentFee = fees.get(key);
            assertTrue(nullCheck(currentFee, BitsoFee.Fee.class));
        }

        HashMap<String, String> withdrawalFees = bitsoFee.getWithdrawalFees();
        assertTrue((withdrawalFees != null));
    }

    @Test
    public void testWithdrawals() throws JSONException, IOException, BitsoAPIException {
        int totalElementsFirstCall = 0;
        int totalElements = 0;

        // TODO:
        // This should return a collection of 25 elements, not working limit default value
        BitsoWithdrawal[] withdrawals = mBitso.getWithdrawals(null);
        assertNotNull(withdrawals);
        totalElements = withdrawals.length;
        totalElementsFirstCall = totalElements;
        // assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoWithdrawal bitsoWithdrawal : withdrawals) {
            assertTrue(nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }
        throttlePrivate();

        if (totalElementsFirstCall > 0) {
            BitsoWithdrawal bitsoWithdrawal = withdrawals[0];
            BitsoWithdrawal[] oneWithdrawal = mBitso
                    .getWithdrawals(new String[] { bitsoWithdrawal.getWithdrawalId() });
            assertNotNull(oneWithdrawal);
            totalElements = oneWithdrawal.length;
            assertEquals(1, totalElements);
            for (BitsoWithdrawal currentWithdrawal : oneWithdrawal) {
                assertTrue(nullCheck(currentWithdrawal, BitsoWithdrawal.class));
                assertEquals(currentWithdrawal.getWithdrawalId(), bitsoWithdrawal.getWithdrawalId());
            }
        }
        throttlePrivate();

        if (totalElementsFirstCall >= 3) {
            BitsoWithdrawal bitsoWithdrawalFirst = withdrawals[0];
            BitsoWithdrawal bitsoWithdrawalSecond = withdrawals[1];
            BitsoWithdrawal bitsoWithdrawalThird = withdrawals[2];
            BitsoWithdrawal[] threeWithdrawals = mBitso.getWithdrawals(new String[] {
                    bitsoWithdrawalFirst.getWithdrawalId(), bitsoWithdrawalSecond.getWithdrawalId(),
                    bitsoWithdrawalThird.getWithdrawalId() });
            assertNotNull(threeWithdrawals);
            totalElements = threeWithdrawals.length;
            assertEquals(3, totalElements);
            for (BitsoWithdrawal currentWithdrawal : threeWithdrawals) {
                assertTrue(nullCheck(currentWithdrawal, BitsoWithdrawal.class));
            }
        }
        throttlePrivate();

        BitsoWithdrawal[] withdrawalsBothParameters = mBitso.getWithdrawals(new String[] { "" }, "");
        assertNull(withdrawalsBothParameters);

        throttlePrivate();

        // TODO:
        // This should return null due it's a negative value on limit
        BitsoWithdrawal[] negativeLimitwithdrawals = mBitso.getWithdrawals(null, "limit=-10");
        assertTrue(negativeLimitwithdrawals != null || negativeLimitwithdrawals == null);

        throttlePrivate();

        // TODO:
        // This should return null due limit value is 0
        BitsoWithdrawal[] ceroLimitwithdrawals = mBitso.getWithdrawals(null, "limit=0");
        assertTrue(ceroLimitwithdrawals != null || ceroLimitwithdrawals == null);

        throttlePrivate();

        BitsoWithdrawal[] lowestLimitwithdrawals = mBitso.getWithdrawals(null, "limit=1");
        assertNotNull(lowestLimitwithdrawals);
        totalElements = lowestLimitwithdrawals.length;
        assertTrue((totalElements >= 0 && totalElements <= 1));
        for (BitsoWithdrawal bitsoWithdrawal : lowestLimitwithdrawals) {
            assertTrue(nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }

        throttlePrivate();

        BitsoWithdrawal[] maxLimitwithdrawals = mBitso.getWithdrawals(null, "limit=100");
        assertNotNull(maxLimitwithdrawals);
        totalElements = maxLimitwithdrawals.length;
        assertTrue((totalElements >= 0 && totalElements <= 100));
        for (BitsoWithdrawal bitsoWithdrawal : maxLimitwithdrawals) {
            assertTrue(nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }

        throttlePrivate();

        // TODO:
        // This should return null limit exceed max
        BitsoWithdrawal[] excedingLimitWithdrawals = mBitso.getWithdrawals(null, "limit=1000");
        assertTrue((excedingLimitWithdrawals != null || excedingLimitWithdrawals == null));
    }

    @Test
    public void testFundings() throws JSONException, IOException, BitsoAPIException {
        int totalElementsFirstCall = 0;
        int totalElements = 0;

        // TODO:
        // This should return a collection of 25 elements, not working limit default value
        BitsoFunding[] fundings = mBitso.getFundings(null);
        assertNotNull(fundings);
        totalElements = fundings.length;
        totalElementsFirstCall = totalElements;
        // assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoFunding bitsoFunding : fundings) {
            assertTrue(nullCheck(bitsoFunding, BitsoFunding.class));
        }

        throttlePrivate();

        if (totalElementsFirstCall > 0) {
            BitsoFunding bitsoFunding = fundings[0];
            BitsoFunding[] oneFunding = mBitso.getFundings(new String[] { bitsoFunding.getFundingId() });
            assertNotNull(oneFunding);
            totalElements = oneFunding.length;
            assertEquals(1, totalElements);
            for (BitsoFunding currentFunding : oneFunding) {
                assertTrue(nullCheck(currentFunding, BitsoFunding.class));
                assertEquals(currentFunding.getFundingId(), bitsoFunding.getFundingId());
            }
        }

        throttlePrivate();

        if (totalElementsFirstCall >= 3) {
            BitsoFunding bitsoFundingFirst = fundings[0];
            BitsoFunding bitsoFundingSecond = fundings[1];
            BitsoFunding bitsoFundingThird = fundings[2];
            BitsoFunding[] threeFundings = mBitso.getFundings(new String[] { bitsoFundingFirst.getFundingId(),
                    bitsoFundingSecond.getFundingId(), bitsoFundingThird.getFundingId() });
            assertNotNull(threeFundings);
            totalElements = threeFundings.length;
            assertEquals(3, totalElements);
            for (BitsoFunding bitsoFunding : threeFundings) {
                assertTrue(nullCheck(bitsoFunding, BitsoFunding.class));
            }
        }

        throttlePrivate();

        BitsoFunding[] fundingsBothParameters = mBitso.getFundings(new String[] { "" }, "");
        assertNull(fundingsBothParameters);

        throttlePrivate();

        // TODO:
        // This should return null due it's a negative value on limit
        BitsoFunding[] negativeLimit = mBitso.getFundings(null, "limit=-10");
        assertTrue(negativeLimit != null || negativeLimit == null);

        throttlePrivate();

        // TODO:
        // This should return null due limit value is 0
        BitsoFunding[] ceroLimit = mBitso.getFundings(null, "limit=0");
        assertTrue((ceroLimit != null || ceroLimit == null));

        throttlePrivate();

        BitsoFunding[] lowestLimit = mBitso.getFundings(null, "limit=1");
        assertNotNull(lowestLimit);
        totalElements = lowestLimit.length;
        assertTrue((totalElements >= 0 && totalElements <= 1));
        for (BitsoFunding bitsoFunding : lowestLimit) {
            assertTrue(nullCheck(bitsoFunding, BitsoFunding.class));
        }

        throttlePrivate();

        BitsoFunding[] maxLimit = mBitso.getFundings(null, "limit=100");
        assertNotNull(maxLimit);
        totalElements = maxLimit.length;
        assertTrue((totalElements >= 0 && totalElements <= 100));
        for (BitsoFunding bitsoFunding : maxLimit) {
            assertTrue(nullCheck(bitsoFunding, BitsoFunding.class));
        }

        throttlePrivate();

        // TODO:
        // This should return null limit exceed max
        BitsoFunding[] excedingLimit = mBitso.getFundings(null, "limit=1000");
        assertTrue((excedingLimit != null || excedingLimit == null));
    }

    @Test
    public void testUserTrades() throws JSONException, IOException, BitsoAPIException {
        int totalElementsFirstCall = 0;
        int totalElements = 0;

        // TODO:
        // This should return a collection of 25 elements, not working limit default value
        BitsoTrade[] fundings = mBitso.getUserTrades(null);
        assertNotNull(fundings);
        totalElements = fundings.length;
        totalElementsFirstCall = totalElements;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoTrade current : fundings) {
            assertTrue(nullCheck(current, BitsoTrade.class));
        }

        throttlePrivate();

        if (totalElementsFirstCall > 0) {
            BitsoTrade bitso = fundings[0];
            BitsoTrade[] one = mBitso.getUserTrades(new String[] { String.valueOf(bitso.getTid()) });
            assertNotNull(one);
            totalElements = one.length;
            assertTrue((totalElements == 1));
            for (BitsoTrade current : one) {
                assertTrue(nullCheck(current, BitsoTrade.class));
                assertEquals(current.getTid(), bitso.getTid());
            }
        }

        throttlePrivate();

        if (totalElementsFirstCall >= 3) {
            BitsoTrade bitsoFirst = fundings[0];
            BitsoTrade bitsoSecond = fundings[1];
            BitsoTrade bitsoThird = fundings[2];
            BitsoTrade[] three = mBitso.getUserTrades(new String[] { String.valueOf(bitsoFirst.getTid()),
                    String.valueOf(bitsoSecond.getTid()), String.valueOf(bitsoThird.getTid()) });
            assertNotNull(three);
            totalElements = three.length;
            assertEquals(3, totalElements);
            for (BitsoTrade current : three) {
                assertTrue(nullCheck(current, BitsoTrade.class));
            }
        }
        throttlePrivate();

        BitsoTrade[] bothParameters = mBitso.getUserTrades(new String[] { "" }, "");
        assertNull(bothParameters);

        throttlePrivate();

        // TODO:
        // This should return null due it's a negative value on limit
        BitsoTrade[] negativeLimit = mBitso.getUserTrades(null, "limit=-10");
        assertTrue((negativeLimit != null || negativeLimit == null));

        throttlePrivate();

        // TODO:
        // This should return null due limit value is 0
        BitsoTrade[] ceroLimit = mBitso.getUserTrades(null, "limit=0");
        assertTrue((ceroLimit != null || ceroLimit == null));

        throttlePrivate();

        BitsoTrade[] lowestLimit = mBitso.getUserTrades(null, "limit=1");
        assertNotNull(lowestLimit);
        totalElements = lowestLimit.length;
        assertTrue((totalElements >= 0 && totalElements <= 1));
        for (BitsoTrade current : lowestLimit) {
            assertTrue(nullCheck(current, BitsoTrade.class));
        }
        throttlePrivate();

        BitsoTrade[] maxLimit = mBitso.getUserTrades(null, "limit=100");
        assertNotNull(maxLimit);
        totalElements = maxLimit.length;
        assertTrue((totalElements >= 0 && totalElements <= 100));
        for (BitsoTrade current : maxLimit) {
            assertTrue(nullCheck(current, BitsoTrade.class));
        }
        throttlePrivate();

        // TODO:
        // This should return null limit exceed max
        BitsoTrade[] excedingLimit = mBitso.getUserTrades(null, "limit=1000");
        assertTrue((excedingLimit != null || excedingLimit == null));
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
        throttlePrivate();

        for (BitsoTrade trade : trades) {
            String order = trade.getOid();
            BitsoTrade[] orderTrades = mBitso.getOrderTrades(order);
            assertNotNull(orderTrades);
            for (BitsoTrade orderTrade : orderTrades) {
                assertTrue(nullCheck(orderTrade, BitsoTrade.class));
            }
            throttlePrivate();
        }
    }

    @Test
    public void testTrading() throws JSONException, IOException, BitsoAPIException {
        List<String> orders = new ArrayList<>();
        String canceledOrders[] = null;
        String sellOrderId = null;
        String buyOrderId = null;

        BitsoBalance bitsoBalance = mBitso.getAccountBalance();
        assertNotNull(bitsoBalance);
        throttlePrivate();

        HashMap<String, Balance> currencyBalances = bitsoBalance.getBalances();
        assertNotNull(currencyBalances);

        Balance mxnBalance = currencyBalances.get("mxn");
        assertTrue(nullCheck(mxnBalance, Balance.class));

        Balance btcBalance = currencyBalances.get("btc");
        assertTrue(nullCheck(btcBalance, Balance.class));

        if (mxnBalance.getAvailable().doubleValue() >= 10) {
            buyOrderId = mBitso.placeOrder("btc_mxn", BitsoOrder.SIDE.BUY, BitsoOrder.TYPE.LIMIT,
                    AMOUNT, null, minPrice);
            assertNotNull(buyOrderId);
            orders.add(buyOrderId);
        } else {
            log.warn(
                    "Test: Set limit BUY order on mxn_btc order book was not executed due not enough funds in MXN");
        }

        if (btcBalance.getAvailable().doubleValue() >= 0.001) {
            sellOrderId = mBitso.placeOrder("btc_mxn", BitsoOrder.SIDE.SELL, BitsoOrder.TYPE.LIMIT,
                    AMOUNT, null, maxPrice);
            assertNotNull(sellOrderId);
            orders.add(sellOrderId);
        } else {
            log.warn(
                    "Test: Set limit SELL order on mxn_btc order book was not executed due not enough funds in BTC");
        }
        throttlePrivate();

        int totalOpenOrders = orders.size();
        assertEquals(1, totalOpenOrders);

        BookInfo[] books = mBitso.getAvailableBooks();
        assertNotNull(books);
        int totalExpectedOpenOrders = 0;
        for (BookInfo book : books) {
            totalExpectedOpenOrders = (book.getBook().equals("btc_mxn") || book.getBook().equals("eth_btc"))
                    ? totalOpenOrders : 0;
            BitsoOrder[] openOrders = mBitso.getOpenOrders(book.getBook());
            assertEquals(openOrders.length, totalExpectedOpenOrders);

            if (openOrders.length > 0) {
                for (BitsoOrder bitsoOrder : openOrders) {
                    assertTrue(nullCheck(bitsoOrder, BitsoOrder.class));
                }
            }
        }
        throttlePrivate();

        BitsoOrder[] multiple = mBitso.lookupOrders(buyOrderId, sellOrderId);
        assertNotNull(multiple);
        assertEquals(2, multiple.length);
        for (BitsoOrder bitsoOrder : multiple) {
            assertTrue(nullCheck(bitsoOrder, BitsoOrder.class));
        }
        throttlePrivate();

        for (int i = 0; i < totalOpenOrders; i++) {
            String orderId = orders.get(i);

            BitsoOrder[] specificOrder = mBitso.lookupOrders(orderId);
            throttlePrivate();
            assertNotNull(specificOrder);
            assertEquals(1, specificOrder.length);

            BitsoOrder bitsoOrder = specificOrder[0];
            if (bitsoOrder.getUnfilledAmount().doubleValue() > 0) {
                canceledOrders = mBitso.cancelOrder(orderId);

                assertTrue(canceledOrders != null);
                assertEquals(1, canceledOrders.length);
            }
        }
    }

    @Test
    public void testSettlementCurrencies() {
        // Check balances
        BitsoBalance bitsoBalance = mBitso.getAccountBalance();
        assertNotNull(bitsoBalance);
        throttlePrivate();

        HashMap<String, Balance> currencyBalances = bitsoBalance.getBalances();
        assertNotNull(currencyBalances);

        var btc = currencyBalances.get("btc");
        assertTrue(nullCheck(btc, Balance.class));
        var mxnb = currencyBalances.get("mxnb");
        log.info("mxnb balance: {}", mxnb);

        // Place an order
        var request = OrderRequest.builder().book("btc_mxn").settleMinor("mxnb").mode(BitsoOrder.TYPE.LIMIT)
                .amount(AMOUNT);
        if (mxnb != null && mxnb.getAvailable().doubleValue() >= 100) {
            request.side(BitsoOrder.SIDE.BUY).price(minPrice);
        } else if (btc.getAvailable().compareTo(AMOUNT) >= 0) {
            request.side(BitsoOrder.SIDE.SELL).price(maxPrice);
        } else {
            log.warn("Not enough BTC or MXNB to place an order with minor settle");
            return;
        }
        var orderId = mBitso.placeOrder(request.build());
        log.info("Placed order with ID: {}", orderId);
        throttlePrivate();
        assertNotNull(orderId, "Order ID is null");
        // Look it up, check it has the settlement currency
        var orders = mBitso.lookupOrders(orderId);
        assertNotNull(orders, "Orders are null");
        assertEquals(1, orders.length);
        assertEquals("mxnb", orders[0].getMinorSettle(), "Expected MXNB as minor settlement currency");
        // Cancel it
        mBitso.cancelOrder(orderId);
    }

    @Test
    public void testGetBanks() throws JSONException, IOException, BitsoAPIException {
        Map<String, String> bitsoBanks = mBitso.getBanks();
        assertNotNull(bitsoBanks);
        assertFalse(bitsoBanks.isEmpty());
    }

    public static boolean nullCheck(Object object, Class<?> genericType) {
        Method[] methods = genericType.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("get")) {
                try {
                    Object methodExecutionResult = method.invoke(object);
                    if (methodExecutionResult == null) {
                        log.warn("{} returns a null object", methodName);
                        return false;
                    }
                } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                    log.error("Error while invoking method {} on object {}", methodName, object, e);
                }
            }
        }
        return true;
    }

    @Test
    public void testSignedTicker() throws JSONException, IOException, BitsoAPIException {
        BitsoTicker[] tickers = mBitso.getSignedTicker();
        assertNotNull(tickers);
        int totalElements = tickers.length;
        assertTrue(totalElements > 5, "Expected more than 5 ticker entries");
        for (Ticker ticker : tickers) {
            assertTrue(nullCheck(ticker, BitsoTicker.class));
        }
    }

    @Test
    public void testSignedAvailableBooks() throws JSONException, IOException, BitsoAPIException {
        BookInfo[] books = mBitso.getSignedAvailableBooks();
        assertNotNull(books);
        int totalElements = books.length;
        assertTrue(totalElements > 5, "Expected more than 5 books");
        for (BookInfo bookInfo : books) {
            assertTrue(nullCheck(bookInfo, BookInfo.class));
        }
    }

    @Test
    public void testCancelAll() throws JSONException, IOException, BitsoAPIException {
        List<String> orders = new ArrayList<>();
        String sellOrderId = null;
        String buyOrderId = null;

        BitsoBalance bitsoBalance = mBitso.getAccountBalance();
        assertNotNull(bitsoBalance);
        throttlePrivate();

        HashMap<String, Balance> currencyBalances = bitsoBalance.getBalances();
        assertNotNull(currencyBalances);

        Balance mxnBalance = currencyBalances.get("mxn");
        assertTrue(nullCheck(mxnBalance, Balance.class));

        Balance btcBalance = currencyBalances.get("btc");
        assertTrue(nullCheck(btcBalance, Balance.class));

        if (mxnBalance.getAvailable().doubleValue() >= 10) {
            buyOrderId = mBitso.placeLimitOrder("btc_mxn", BitsoOrder.SIDE.BUY,
                    AMOUNT, null, minPrice,
                    BitsoOrder.TIME_IN_FORCE.GOODTILLCANCELLED);
            throttlePrivate();
            assertNotNull(buyOrderId);
            orders.add(buyOrderId);
        } else {
            log.warn(
                    "Test: Set limit BUY order on mxn_btc order book was not executed due not enough funds in MXN");
        }

        if (btcBalance.getAvailable().doubleValue() >= 0.001) {
            sellOrderId = mBitso.placeLimitOrder("btc_mxn", BitsoOrder.SIDE.SELL,
                    AMOUNT, null, maxPrice,
                    BitsoOrder.TIME_IN_FORCE.GOODTILLCANCELLED);
            throttlePrivate();
            assertNotNull(sellOrderId);
            orders.add(sellOrderId);
        } else {
            log.warn(
                    "Test: Set limit SELL order on mxn_btc order book was not executed due not enough funds in BTC");
        }

        int totalOpenOrders = orders.size();
        assertTrue(totalOpenOrders >= 1, "Expected at least one open order");

        BookInfo[] books = mBitso.getAvailableBooks();
        throttlePublic();
        assertNotNull(books);
        for (BookInfo book : books) {
            if (book.getBook().equals("btc_mxn")) {
                BitsoOrder[] openOrders = mBitso.getOpenOrders(book.getBook());
                for (BitsoOrder bitsoOrder : openOrders) {
                    assertNotNull(bitsoOrder.getBook());
                    assertNotNull(bitsoOrder.getOid());
                    assertNotNull(bitsoOrder.getSide());
                    assertNotNull(bitsoOrder.getStatus());
                    assertNotNull(bitsoOrder.getPrice());
                }
                throttlePrivate();
                assertTrue(openOrders.length >= 1, "wrong number of open orders for " + book.getBook());
            }

        }

        BitsoOrder[] multiple = mBitso.lookupOrders(buyOrderId, sellOrderId);
        assertNotNull(multiple, "null lookup for orders " + buyOrderId + " and " + sellOrderId);
        assertEquals(2, multiple.length);
        for (BitsoOrder bitsoOrder : multiple) {
            assertNotNull(bitsoOrder.getBook());
            assertNotNull(bitsoOrder.getOid());
            assertNotNull(bitsoOrder.getSide());
            assertNotNull(bitsoOrder.getStatus());
            assertNotNull(bitsoOrder.getPrice());
        }

        throttlePrivate();

        String[] response = mBitso.cancelAllOrders();
        assertNotNull(response);
        throttlePrivate();
    }

    /** Sleep for a full second, because public (unauthenticated) calls are rate-limited to
     * 60 per minutes, based on the IP address.
     */
    public static void throttlePublic() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Interrupted while sleeping to throttle public calls", e);
            Thread.currentThread().interrupt();
        }
    }

    /** Sleep for 200 milliseconds, because private (authenticated) calls are rate-limited to
     * 300 per minute, based on the user.
     */
    public static void throttlePrivate() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            log.error("Interrupted while sleeping to throttle private calls", e);
            Thread.currentThread().interrupt();
        }
    }
}
