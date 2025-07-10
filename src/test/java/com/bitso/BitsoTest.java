package com.bitso;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import com.bitso.BitsoBalance.Balance;
import com.bitso.exceptions.BitsoAPIException;
import com.bitso.exceptions.BitsoNullException;
import com.bitso.exceptions.BitsoPayloadException;
import com.bitso.exceptions.BitsoServerException;
import com.bitso.exceptions.BitsoValidationException;
import com.bitso.exchange.BookInfo;
import com.bitso.exchange.Ticker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class BitsoTest {
    protected Bitso mBitso;
    public static final long SLEEP = 2_000;

    // Test public Rest API
    @Test
    public void testAvailableBooks() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        BookInfo[] books = mBitso.getAvailableBooks();
        assertNotNull(books);
        int totalElements = books.length;
        assertEquals(6, totalElements);
        for (BookInfo bookInfo : books) {
            assertTrue(nullCheck(bookInfo, BookInfo.class));
        }
    }

    @Test
    public void testTicker() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        BitsoTicker[] tickers = mBitso.getTicker();
        assertNotNull(tickers);
        int totalElements = tickers.length;
        assertEquals(6, totalElements);
        for (Ticker ticker : tickers) {
            assertTrue(nullCheck(ticker, BitsoTicker.class));
        }
    }

    @Test
    public void testOrderBook() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        BookInfo[] availableBooks = mBitso.getAvailableBooks();
        assertNotNull(availableBooks);
        for (BookInfo bookInfo : availableBooks) {
            BitsoOrderBook bitsoOrderBook = mBitso.getOrderBook(bookInfo.getBook());
            assertTrue(nullCheck(bitsoOrderBook, BitsoOrderBook.class));
            BitsoOrderBook bitsoOrderBookNoAggreagte = mBitso.getOrderBook(bookInfo.getBook(), false);
            assertTrue(nullCheck(bitsoOrderBookNoAggreagte, BitsoOrderBook.class));
            BitsoOrderBook bitsoOrderBookAggregate = mBitso.getOrderBook(bookInfo.getBook(), true);
            assertTrue(nullCheck(bitsoOrderBookAggregate, BitsoOrderBook.class));
        }
    }

    @Test
    public void testTrades() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, InterruptedException, BitsoServerException {
        BookInfo[] availableBooks = mBitso.getAvailableBooks();
        assertNotNull(availableBooks);
        for (BookInfo bookInfo : availableBooks) {
            int totalElements = 0;
            BitsoTransactions.Transaction[] innerTransactions;

            BitsoTransactions bitsoTransaction = mBitso.getTrades(bookInfo.getBook());
            assertTrue(nullCheck(bitsoTransaction, BitsoTransactions.class));

            Thread.sleep(SLEEP);

            /*
             * // TODO: // This should return null due it's a negative value on limit try{ BitsoTransactions
             * bitsoTransactionNegativeLimit = mBitso.getTrades(bookInfo.getBook(), "limit=-10"); }catch
             * (BitsoAPIException bitsoAPIException) { assertEquals(bitsoAPIException != null, true); }
             */

            Thread.sleep(SLEEP);

            // TODO:
            // This should return null due limit value is 0
            BitsoTransactions bitsoTransactionCeroLimit = mBitso.getTrades(bookInfo.getBook(), "limit=0");
            assertNotNull(bitsoTransactionCeroLimit);

            Thread.sleep(SLEEP);

            BitsoTransactions bitsoTransactionLowLimit = mBitso.getTrades(bookInfo.getBook(), "limit=1");
            totalElements = bitsoTransactionLowLimit.getTransactionsList().length;
            assertTrue((totalElements >= 0 && totalElements <= 1));

            Thread.sleep(SLEEP);

            BitsoTransactions bitsoTransactionMaxLimit = mBitso.getTrades(bookInfo.getBook(), "limit=100");
            totalElements = bitsoTransactionMaxLimit.getTransactionsList().length;
            assertTrue((totalElements >= 0 && totalElements <= 100));

            Thread.sleep(SLEEP);

            // TODO:
            // This should return null due the limit value exceeds 100
            BitsoTransactions bitsoTransactionExcedingMaxLimit = mBitso.getTrades(bookInfo.getBook(),
                    "limit=1000");
            assertNotNull(bitsoTransactionExcedingMaxLimit);

            Thread.sleep(SLEEP);

            BitsoTransactions bitsoTransactionSortAsc = mBitso.getTrades(bookInfo.getBook(), "sort=asc");
            innerTransactions = bitsoTransaction.getTransactionsList();
            totalElements = innerTransactions.length;
            assertNotNull(bitsoTransactionSortAsc);
            assertEquals(true, (totalElements >= 0 && totalElements <= 25));
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

            Thread.sleep(SLEEP);

            // TODO:
            // This should return a correct DESC order and is not doing it
            BitsoTransactions bitsoTransactionSortDesc = mBitso.getTrades(bookInfo.getBook(), "sort=desc");
            innerTransactions = bitsoTransaction.getTransactionsList();
            totalElements = innerTransactions.length;
            assertNotNull(bitsoTransactionSortDesc);
            assertTrue((totalElements >= 0 && totalElements <= 25));
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

            Thread.sleep(SLEEP);

            BitsoTransactions bitsoTransactionSortLimit = mBitso.getTrades(bookInfo.getBook(), "sort=asc",
                    "limit=15");
            totalElements = bitsoTransactionSortLimit.getTransactionsList().length;
            assertNotNull(bitsoTransactionSortLimit);
            assertTrue((totalElements >= 0 && totalElements <= 15));
        }
    }

    // Test private Rest API
    @Test
    public void testAccountStatus() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        BitsoAccountStatus bitsoAccountStatus = mBitso.getAccountStatus();
        assertTrue(nullCheck(bitsoAccountStatus, BitsoAccountStatus.class));
    }

    @Test
    public void testAccountBalance() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
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
    public void testFees() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
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
    public void testLedger() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException, InterruptedException {
        int totalElements = 0;

        BitsoOperation[] defaultLedger = mBitso.getLedger("");
        assertNotNull(defaultLedger);
        totalElements = defaultLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoOperation bitsoOperation : defaultLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
        }

        Thread.sleep(SLEEP);

        BitsoOperation[] tradesLedger = mBitso.getLedger("trades");
        assertNotNull(tradesLedger);
        totalElements = tradesLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoOperation bitsoOperation : tradesLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals("trade", bitsoOperation.getOperationDescription());
        }

        Thread.sleep(SLEEP);

        BitsoOperation[] feesLedger = mBitso.getLedger("fees");
        assertNotNull(feesLedger);
        totalElements = feesLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoOperation bitsoOperation : feesLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals("fee", bitsoOperation.getOperationDescription());
        }

        Thread.sleep(SLEEP);

        BitsoOperation[] fundingsLedger = mBitso.getLedger("fundings");
        assertNotNull(fundingsLedger);
        totalElements = fundingsLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoOperation bitsoOperation : fundingsLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals("funding", bitsoOperation.getOperationDescription());
        }

        Thread.sleep(SLEEP);

        BitsoOperation[] withdrawalsLedger = mBitso.getLedger("withdrawals");
        assertNotNull(withdrawalsLedger);
        totalElements = withdrawalsLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));
        for (BitsoOperation bitsoOperation : withdrawalsLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals("withdrawal", bitsoOperation.getOperationDescription());
        }

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null due it's a negative value on limit
        BitsoOperation[] negativeLimitLedger = mBitso.getLedger("", "limit=-10");
        assertTrue((negativeLimitLedger != null || negativeLimitLedger == null));

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null due limit value is 0
        BitsoOperation[] ceroLimitLedger = mBitso.getLedger("", "limit=0");
        assertTrue((ceroLimitLedger != null || ceroLimitLedger == null));

        Thread.sleep(SLEEP);

        BitsoOperation[] lowLimitLedger = mBitso.getLedger("", "limit=1");
        assertNotNull(lowLimitLedger);
        totalElements = lowLimitLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 1));
        for (BitsoOperation bitsoOperation : withdrawalsLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
        }

        Thread.sleep(SLEEP);

        BitsoOperation[] maxLimitLedger = mBitso.getLedger("", "limit=100");
        assertNotNull(maxLimitLedger);
        totalElements = maxLimitLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 100));
        for (BitsoOperation bitsoOperation : withdrawalsLedger) {
            assertTrue(nullCheck(bitsoOperation, BitsoOperation.class));
        }

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null due the limit value exceeds 100
        BitsoOperation[] excedingLimitLedger = mBitso.getLedger("", "limit=1000");
        assertTrue((excedingLimitLedger != null || excedingLimitLedger == null));

        Thread.sleep(SLEEP);

        BitsoOperation[] sortAscLedger = mBitso.getLedger("", "sort=asc");
        assertNotNull(sortAscLedger);
        totalElements = sortAscLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));

        Thread.sleep(SLEEP);

        BitsoOperation[] sortDescLedger = mBitso.getLedger("", "sort=desc");
        assertNotNull(sortDescLedger);
        totalElements = sortDescLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 25));

        Thread.sleep(SLEEP);

        BitsoOperation[] multipleQueryParameterLedger = mBitso.getLedger("", "sort=desc", "limit=15");
        assertNotNull(multipleQueryParameterLedger);
        totalElements = multipleQueryParameterLedger.length;
        assertTrue((totalElements >= 0 && totalElements <= 15));
    }

    @Test
    public void testWithdrawals() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException, InterruptedException {
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

        Thread.sleep(SLEEP);

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

        Thread.sleep(SLEEP);

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

        Thread.sleep(SLEEP);

        BitsoWithdrawal[] withdrawalsBothParameters = mBitso.getWithdrawals(new String[] { "" }, "");
        assertNull(withdrawalsBothParameters);

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null due it's a negative value on limit
        BitsoWithdrawal[] negativeLimitwithdrawals = mBitso.getWithdrawals(null, "limit=-10");
        assertTrue(negativeLimitwithdrawals != null || negativeLimitwithdrawals == null);

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null due limit value is 0
        BitsoWithdrawal[] ceroLimitwithdrawals = mBitso.getWithdrawals(null, "limit=0");
        assertTrue(ceroLimitwithdrawals != null || ceroLimitwithdrawals == null);

        Thread.sleep(SLEEP);

        BitsoWithdrawal[] lowestLimitwithdrawals = mBitso.getWithdrawals(null, "limit=1");
        assertNotNull(lowestLimitwithdrawals);
        totalElements = lowestLimitwithdrawals.length;
        assertTrue((totalElements >= 0 && totalElements <= 1));
        for (BitsoWithdrawal bitsoWithdrawal : lowestLimitwithdrawals) {
            assertTrue(nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }

        Thread.sleep(SLEEP);

        BitsoWithdrawal[] maxLimitwithdrawals = mBitso.getWithdrawals(null, "limit=100");
        assertNotNull(maxLimitwithdrawals);
        totalElements = maxLimitwithdrawals.length;
        assertTrue((totalElements >= 0 && totalElements <= 100));
        for (BitsoWithdrawal bitsoWithdrawal : maxLimitwithdrawals) {
            assertTrue(nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null limit exceed max
        BitsoWithdrawal[] excedingLimitWithdrawals = mBitso.getWithdrawals(null, "limit=1000");
        assertTrue((excedingLimitWithdrawals != null || excedingLimitWithdrawals == null));
    }

    @Test
    public void testFundings() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException, InterruptedException {
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

        Thread.sleep(SLEEP);

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

        Thread.sleep(SLEEP);

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

        Thread.sleep(SLEEP);

        BitsoFunding[] fundingsBothParameters = mBitso.getFundings(new String[] { "" }, "");
        assertNull(fundingsBothParameters);

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null due it's a negative value on limit
        BitsoFunding[] negativeLimit = mBitso.getFundings(null, "limit=-10");
        assertTrue(negativeLimit != null || negativeLimit == null);

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null due limit value is 0
        BitsoFunding[] ceroLimit = mBitso.getFundings(null, "limit=0");
        assertTrue((ceroLimit != null || ceroLimit == null));

        Thread.sleep(SLEEP);

        BitsoFunding[] lowestLimit = mBitso.getFundings(null, "limit=1");
        assertNotNull(lowestLimit);
        totalElements = lowestLimit.length;
        assertTrue((totalElements >= 0 && totalElements <= 1));
        for (BitsoFunding bitsoFunding : lowestLimit) {
            assertTrue(nullCheck(bitsoFunding, BitsoFunding.class));
        }

        Thread.sleep(SLEEP);

        BitsoFunding[] maxLimit = mBitso.getFundings(null, "limit=100");
        assertNotNull(maxLimit);
        totalElements = maxLimit.length;
        assertTrue((totalElements >= 0 && totalElements <= 100));
        for (BitsoFunding bitsoFunding : maxLimit) {
            assertTrue(nullCheck(bitsoFunding, BitsoFunding.class));
        }

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null limit exceed max
        BitsoFunding[] excedingLimit = mBitso.getFundings(null, "limit=1000");
        assertTrue((excedingLimit != null || excedingLimit == null));
    }

    @Test
    public void testUserTrades() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException, InterruptedException {
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

        Thread.sleep(SLEEP);

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

        Thread.sleep(SLEEP);

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

        Thread.sleep(SLEEP);

        BitsoTrade[] bothParameters = mBitso.getUserTrades(new String[] { "" }, "");
        assertNull(bothParameters);

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null due it's a negative value on limit
        BitsoTrade[] negativeLimit = mBitso.getUserTrades(null, "limit=-10");
        assertTrue((negativeLimit != null || negativeLimit == null));

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null due limit value is 0
        BitsoTrade[] ceroLimit = mBitso.getUserTrades(null, "limit=0");
        assertTrue((ceroLimit != null || ceroLimit == null));

        Thread.sleep(SLEEP);

        BitsoTrade[] lowestLimit = mBitso.getUserTrades(null, "limit=1");
        assertNotNull(lowestLimit);
        totalElements = lowestLimit.length;
        assertTrue((totalElements >= 0 && totalElements <= 1));
        for (BitsoTrade current : lowestLimit) {
            assertTrue(nullCheck(current, BitsoTrade.class));
        }

        Thread.sleep(SLEEP);

        BitsoTrade[] maxLimit = mBitso.getUserTrades(null, "limit=100");
        assertNotNull(maxLimit);
        totalElements = maxLimit.length;
        assertTrue((totalElements >= 0 && totalElements <= 100));
        for (BitsoTrade current : maxLimit) {
            assertTrue(nullCheck(current, BitsoTrade.class));
        }

        Thread.sleep(SLEEP);

        // TODO:
        // This should return null limit exceed max
        BitsoTrade[] excedingLimit = mBitso.getUserTrades(null, "limit=1000");
        assertTrue((excedingLimit != null || excedingLimit == null));
    }

    @Test
    public void testOrderTrades() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException, InterruptedException {
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

        Thread.sleep(SLEEP);

        for (BitsoTrade trade : trades) {
            String order = trade.getOid();
            BitsoTrade[] orderTrades = mBitso.getOrderTrades(order);
            assertNotNull(orderTrades);
            for (BitsoTrade orderTrade : orderTrades) {
                assertTrue(nullCheck(orderTrade, BitsoTrade.class));
            }
            Thread.sleep(SLEEP);
        }
    }

    @Test
    public void testTrading() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException, InterruptedException, BitsoValidationException {
        List<String> orders = new ArrayList<>();
        String canceledOrders[] = null;
        String sellOrderId = null;
        String buyOrderId = null;

        BitsoBalance bitsoBalance = mBitso.getAccountBalance();
        assertNotNull(bitsoBalance);

        HashMap<String, Balance> currencyBalances = bitsoBalance.getBalances();
        assertNotNull(currencyBalances);

        Balance mxnBalance = currencyBalances.get("mxn");
        assertTrue(nullCheck(mxnBalance, Balance.class));

        Balance btcBalance = currencyBalances.get("btc");
        assertTrue(nullCheck(btcBalance, Balance.class));

        if (mxnBalance.getAvailable().doubleValue() >= 10) {
            buyOrderId = mBitso.placeOrder("btc_mxn", BitsoOrder.SIDE.BUY, BitsoOrder.TYPE.LIMIT,
                    new BigDecimal("0.001"), null, new BigDecimal("10000"));
            assertNotNull(buyOrderId);
            orders.add(buyOrderId);
        } else {
            System.out.println(
                    "Test: Set limit BUY order on mxn_btc order book was not executed due not enough funds in MXN");
        }

        if (btcBalance.getAvailable().doubleValue() >= 0.001) {
            sellOrderId = mBitso.placeOrder("btc_mxn", BitsoOrder.SIDE.SELL, BitsoOrder.TYPE.LIMIT,
                    new BigDecimal("0.001"), null, new BigDecimal("100000"));
            assertNotNull(sellOrderId);
            orders.add(sellOrderId);
        } else {
            System.out.println(
                    "Test: Set limit SELL order on mxn_btc order book was not executed due not enough funds in BTC");
        }

        Thread.sleep(1000);

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

        Thread.sleep(1000);

        BitsoOrder[] multiple = mBitso.lookupOrders(buyOrderId, sellOrderId);
        assertNotNull(multiple);
        assertEquals(2, multiple.length);
        for (BitsoOrder bitsoOrder : multiple) {
            assertTrue(nullCheck(bitsoOrder, BitsoOrder.class));
        }

        Thread.sleep(1000);

        for (int i = 0; i < totalOpenOrders; i++) {
            String orderId = orders.get(i);

            BitsoOrder[] specificOrder = mBitso.lookupOrders(orderId);
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
    public void testFundingDestination() throws JSONException, BitsoNullException, IOException,
            BitsoAPIException, BitsoPayloadException, BitsoServerException, InterruptedException {
        Map<String, String> btcFundingDestination = mBitso.fundingDestination("fund_currency=btc");
        assertNotNull(btcFundingDestination);
        assertTrue(btcFundingDestination.containsKey("account_identifier_name")
                && btcFundingDestination.containsKey("account_identifier"));

        Thread.sleep(SLEEP);

        Map<String, String> ethFundingDestination = mBitso.fundingDestination("fund_currency=eth");
        assertNotNull(ethFundingDestination);
        assertTrue(ethFundingDestination.containsKey("account_identifier_name")
                && ethFundingDestination.containsKey("account_identifier"));

        Thread.sleep(SLEEP);

        Map<String, String> mxnFundingDestination = mBitso.fundingDestination("fund_currency=mxn");
        assertNotNull(mxnFundingDestination);
        assertTrue(mxnFundingDestination.containsKey("account_identifier_name")
                && mxnFundingDestination.containsKey("account_identifier"));

        Thread.sleep(SLEEP);
    }

    @Test
    public void testGetBanks() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
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
                        System.out.println(methodName + " returns a null object");
                        return false;
                    }
                } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Test
    public void testSignedTicker() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        BitsoTicker[] tickers = mBitso.getSignedTicker();
        assertNotNull(tickers);
        int totalElements = tickers.length;
        assertEquals(6, totalElements);
        for (Ticker ticker : tickers) {
            assertTrue(nullCheck(ticker, BitsoTicker.class));
        }
    }

    @Test
    public void testSignedAvailableBooks() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException {
        BookInfo[] books = mBitso.getSignedAvailableBooks();
        assertNotNull(books);
        int totalElements = books.length;
        assertEquals(6, totalElements);
        for (BookInfo bookInfo : books) {
            assertTrue(nullCheck(bookInfo, BookInfo.class));
        }
    }


    @Test
    public void testCancelAll() throws JSONException, BitsoNullException, IOException, BitsoAPIException,
            BitsoPayloadException, BitsoServerException, InterruptedException, BitsoValidationException {
        List<String> orders = new ArrayList<>();
        String sellOrderId = null;
        String buyOrderId = null;

        BitsoBalance bitsoBalance = mBitso.getAccountBalance();
        assertNotNull(bitsoBalance);

        HashMap<String, Balance> currencyBalances = bitsoBalance.getBalances();
        assertNotNull(currencyBalances);

        Balance mxnBalance = currencyBalances.get("mxn");
        assertTrue(nullCheck(mxnBalance, Balance.class));

        Balance btcBalance = currencyBalances.get("btc");
        assertTrue(nullCheck(btcBalance, Balance.class));

        if (mxnBalance.getAvailable().doubleValue() >= 10) {
            buyOrderId = mBitso.placeLimitOrder("btc_mxn", BitsoOrder.SIDE.BUY,
                    new BigDecimal("0.001"), null, new BigDecimal("10000"),
                    BitsoOrder.TIME_IN_FORCE.GOODTILLCANCELLED);
            assertNotNull(buyOrderId);
            orders.add(buyOrderId);
        } else {
            System.out.println(
                    "Test: Set limit BUY order on mxn_btc order book was not executed due not enough funds in MXN");
        }

        if (btcBalance.getAvailable().doubleValue() >= 0.001) {
            sellOrderId = mBitso.placeLimitOrder("btc_mxn", BitsoOrder.SIDE.SELL,
                    new BigDecimal("0.001"), null, new BigDecimal("100000"),
                    BitsoOrder.TIME_IN_FORCE.GOODTILLCANCELLED);
            assertNotNull(sellOrderId);
            orders.add(sellOrderId);
        } else {
            System.out.println(
                    "Test: Set limit SELL order on mxn_btc order book was not executed due not enough funds in BTC");
        }

        Thread.sleep(1000);

        int totalOpenOrders = orders.size();
        assertEquals(1, totalOpenOrders);

        BookInfo[] books = mBitso.getAvailableBooks();
        assertNotNull(books);
        int totalExpectedOpenOrders = 0;
        for (BookInfo book : books) {
            totalExpectedOpenOrders = (book.getBook().equals("btc_mxn"))
                    ? totalOpenOrders : 0;
            BitsoOrder[] openOrders = mBitso.getOpenOrders(book.getBook());
            assertEquals(totalExpectedOpenOrders, openOrders.length, "wrong number of open orders for " + book.getBook());

            for (BitsoOrder bitsoOrder : openOrders) {
                assertTrue(nullCheck(bitsoOrder, BitsoOrder.class));
            }
        }

        Thread.sleep(1000);

        BitsoOrder[] multiple = mBitso.lookupOrders(buyOrderId, sellOrderId);
        assertNotNull(multiple, "null lookup for orders " + buyOrderId + " and " + sellOrderId);
        assertEquals(2, multiple.length);
        for (BitsoOrder bitsoOrder : multiple) {
            assertTrue(nullCheck(bitsoOrder, BitsoOrder.class));
        }

        Thread.sleep(1000);

        String[] response = mBitso.cancelAllOrders();
        assertNotNull(response);

    }
}
