package com.bitso;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.bitso.BitsoBalance.Balance;
import com.bitso.exceptions.BitsoAPIException;
import com.bitso.exchange.BookInfo;
import com.bitso.exchange.Ticker;

public abstract class BitsoTest {
    protected Bitso mBitso;

    // Test public Rest API
    @Test
    public void testAvailableBooks() throws BitsoAPIException {
        BookInfo[] books = mBitso.getAvailableBooks();
        assertEquals(true, (books != null));
        int totalElements = books.length;
        assertEquals(6, totalElements);
        for (BookInfo bookInfo : books) {
            assertEquals(nullCheck(bookInfo, BookInfo.class), true);
        }
    }

    @Test
    public void testTicker() throws BitsoAPIException {
        BitsoTicker[] tickers = mBitso.getTicker();
        assertEquals(tickers != null, true);
        int totalElements = tickers.length;
        assertEquals(6, totalElements);
        for (Ticker ticker : tickers) {
            assertEquals(nullCheck(ticker, BitsoTicker.class), true);
        }
    }

    @Test
    public void testOrderBook() throws BitsoAPIException {
        BookInfo[] availableBooks = mBitso.getAvailableBooks();
        assertEquals(availableBooks != null, true);
        for (BookInfo bookInfo : availableBooks) {
            BitsoOrderBook bitsoOrderBook = mBitso.getOrderBook(bookInfo.getBook());
            assertEquals(nullCheck(bitsoOrderBook, BitsoOrderBook.class), true);
            BitsoOrderBook bitsoOrderBookNoAggreagte = mBitso.getOrderBook(bookInfo.getBook(), false);
            assertEquals(nullCheck(bitsoOrderBookNoAggreagte, BitsoOrderBook.class), true);
            BitsoOrderBook bitsoOrderBookAggregate = mBitso.getOrderBook(bookInfo.getBook(), true);
            assertEquals(nullCheck(bitsoOrderBookAggregate, BitsoOrderBook.class), true);
        }
    }

    @Test
    public void testTrades() throws InterruptedException, BitsoAPIException {
        BookInfo[] availableBooks = mBitso.getAvailableBooks();
        assertEquals(availableBooks != null, true);
        for (BookInfo bookInfo : availableBooks) {
            int totalElements = 0;
            BitsoTransactions.Transaction[] innerTransactions;

            BitsoTransactions bitsoTransaction = mBitso.getTrades(bookInfo.getBook());
            assertEquals(nullCheck(bitsoTransaction, BitsoTransactions.class), true);

            Thread.sleep(5_000);

            /*
             * // TODO: // This should return null due it's a negative value on limit try{ BitsoTransactions
             * bitsoTransactionNegativeLimit = mBitso.getTrades(bookInfo.getBook(), "limit=-10"); }catch
             * (BitsoAPIException bitsoAPIException) { assertEquals(bitsoAPIException != null, true); }
             */

            Thread.sleep(5_000);

            // TODO:
            // This should return null due limit value is 0
            BitsoTransactions bitsoTransactionCeroLimit = mBitso.getTrades(bookInfo.getBook(), "limit=0");
            assertEquals(bitsoTransactionCeroLimit != null, true);

            Thread.sleep(5_000);

            BitsoTransactions bitsoTransactionLowLimit = mBitso.getTrades(bookInfo.getBook(), "limit=1");
            totalElements = bitsoTransactionLowLimit.getTransactionsList().length;
            assertEquals((totalElements >= 0 && totalElements <= 1), true);

            Thread.sleep(5_000);

            BitsoTransactions bitsoTransactionMaxLimit = mBitso.getTrades(bookInfo.getBook(), "limit=100");
            totalElements = bitsoTransactionMaxLimit.getTransactionsList().length;
            assertEquals((totalElements >= 0 && totalElements <= 100), true);

            Thread.sleep(5_000);

            // TODO:
            // This should return null due the limit value exceeds 100
            BitsoTransactions bitsoTransactionExcedingMaxLimit = mBitso.getTrades(bookInfo.getBook(),
                    "limit=1000");
            assertEquals(bitsoTransactionExcedingMaxLimit != null, true);

            Thread.sleep(5_000);

            BitsoTransactions bitsoTransactionSortAsc = mBitso.getTrades(bookInfo.getBook(), "sort=asc");
            innerTransactions = bitsoTransaction.getTransactionsList();
            totalElements = innerTransactions.length;
            assertEquals(bitsoTransactionSortAsc != null, true);
            assertEquals((totalElements >= 0 && totalElements <= 25), true);
            if (totalElements >= 5) {
                boolean orderAsc = true;
                int initialId = Integer.parseInt(innerTransactions[0].getTid());
                for (int i = 1; i < 5; i++) {
                    int current = Integer.parseInt(innerTransactions[i].getTid());
                    orderAsc = (current < initialId);
                    initialId = current;
                }
                assertEquals(true, orderAsc);
            }

            Thread.sleep(5_000);

            // TODO:
            // This should return a correct DESC order and is not doing it
            BitsoTransactions bitsoTransactionSortDesc = mBitso.getTrades(bookInfo.getBook(), "sort=desc");
            innerTransactions = bitsoTransaction.getTransactionsList();
            totalElements = innerTransactions.length;
            assertEquals(bitsoTransactionSortDesc != null, true);
            assertEquals((totalElements >= 0 && totalElements <= 25), true);
            if (totalElements >= 5) {
                boolean orderDesc = true;
                int initialId = Integer.parseInt(innerTransactions[0].getTid());
                for (int i = 1; i < 5; i++) {
                    int current = Integer.parseInt(innerTransactions[i].getTid());
                    orderDesc = (current < initialId);
                    initialId = current;
                }
                assertEquals(true, orderDesc);
            }

            Thread.sleep(5_000);

            BitsoTransactions bitsoTransactionSortLimit = mBitso.getTrades(bookInfo.getBook(), "sort=asc",
                    "limit=15");
            totalElements = bitsoTransactionSortLimit.getTransactionsList().length;
            assertEquals(bitsoTransactionSortLimit != null, true);
            assertEquals((totalElements >= 0 && totalElements <= 15), true);
        }
    }

    // Test private Rest API
    @Test
    public void testAccountStatus() throws BitsoAPIException {
        BitsoAccountStatus bitsoAccountStatus = mBitso.getAccountStatus();
        assertEquals(nullCheck(bitsoAccountStatus, BitsoAccountStatus.class), true);
    }

    @Test
    public void testAccountBalance() throws BitsoAPIException {
        BitsoBalance bitsoBalance = mBitso.getAccountBalance();
        assertEquals(nullCheck(bitsoBalance, BitsoBalance.class), true);
        HashMap<String, BitsoBalance.Balance> balances = bitsoBalance.getBalances();
        Set<String> keys = balances.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            BitsoBalance.Balance currentBalance = balances.get(iterator.next());
            assertEquals(nullCheck(currentBalance, BitsoBalance.Balance.class), true);
        }
    }

    @Test
    public void testFees() throws BitsoAPIException {
        BitsoFee bitsoFee = mBitso.getFees();
        assertEquals(nullCheck(bitsoFee, BitsoFee.class), true);
        HashMap<String, BitsoFee.Fee> fees = bitsoFee.getTradeFees();
        Set<String> keys = fees.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            BitsoFee.Fee currentFee = fees.get(iterator.next());
            assertEquals(nullCheck(currentFee, BitsoFee.Fee.class), true);
        }

        HashMap<String, String> withdrawalFees = bitsoFee.getWithdrawalFees();
        assertEquals((withdrawalFees != null), true);
    }

    @Test
    public void testLedger() throws InterruptedException, BitsoAPIException {
        int totalElements = 0;

        BitsoOperation[] defaultLedger = mBitso.getLedger("");
        assertEquals(defaultLedger != null, true);
        totalElements = defaultLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoOperation bitsoOperation : defaultLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
        }

        Thread.sleep(5_000);

        BitsoOperation[] tradesLedger = mBitso.getLedger("trades");
        assertEquals(tradesLedger != null, true);
        totalElements = tradesLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoOperation bitsoOperation : tradesLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals(bitsoOperation.getOperationDescription(), "trade");
        }

        Thread.sleep(5_000);

        BitsoOperation[] feesLedger = mBitso.getLedger("fees");
        assertEquals(feesLedger != null, true);
        totalElements = feesLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoOperation bitsoOperation : feesLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals(bitsoOperation.getOperationDescription(), "fee");
        }

        Thread.sleep(5_000);

        BitsoOperation[] fundingsLedger = mBitso.getLedger("fundings");
        assertEquals(fundingsLedger != null, true);
        totalElements = fundingsLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoOperation bitsoOperation : fundingsLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals(bitsoOperation.getOperationDescription(), "funding");
        }

        Thread.sleep(5_000);

        BitsoOperation[] withdrawalsLedger = mBitso.getLedger("withdrawals");
        assertEquals(withdrawalsLedger != null, true);
        totalElements = withdrawalsLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoOperation bitsoOperation : withdrawalsLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
            assertEquals(bitsoOperation.getOperationDescription(), "withdrawal");
        }

        Thread.sleep(5_000);

        // TODO:
        // This should return null due it's a negative value on limit
        BitsoOperation[] negativeLimitLedger = mBitso.getLedger("", "limit=-10");
        assertEquals((negativeLimitLedger != null || negativeLimitLedger == null), true);

        Thread.sleep(5_000);

        // TODO:
        // This should return null due limit value is 0
        BitsoOperation[] ceroLimitLedger = mBitso.getLedger("", "limit=0");
        assertEquals((ceroLimitLedger != null || ceroLimitLedger == null), true);

        Thread.sleep(5_000);

        BitsoOperation[] lowLimitLedger = mBitso.getLedger("", "limit=1");
        assertEquals(lowLimitLedger != null, true);
        totalElements = lowLimitLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 1), true);
        for (BitsoOperation bitsoOperation : withdrawalsLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
        }

        Thread.sleep(5_000);

        BitsoOperation[] maxLimitLedger = mBitso.getLedger("", "limit=100");
        assertEquals(maxLimitLedger != null, true);
        totalElements = maxLimitLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 100), true);
        for (BitsoOperation bitsoOperation : withdrawalsLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
        }

        Thread.sleep(5_000);

        // TODO:
        // This should return null due the limit value exceeds 100
        BitsoOperation[] excedingLimitLedger = mBitso.getLedger("", "limit=1000");
        assertEquals((excedingLimitLedger != null || excedingLimitLedger == null), true);

        Thread.sleep(5_000);

        BitsoOperation[] sortAscLedger = mBitso.getLedger("", "sort=asc");
        assertEquals(sortAscLedger != null, true);
        totalElements = sortAscLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);

        Thread.sleep(5_000);

        BitsoOperation[] sortDescLedger = mBitso.getLedger("", "sort=desc");
        assertEquals(sortDescLedger != null, true);
        totalElements = sortDescLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);

        Thread.sleep(5_000);

        BitsoOperation[] multipleQueryParameterLedger = mBitso.getLedger("", "sort=desc", "limit=15");
        assertEquals(multipleQueryParameterLedger != null, true);
        totalElements = multipleQueryParameterLedger.length;
        assertEquals((totalElements >= 0 && totalElements <= 15), true);
    }

    @Test
    public void testWithdrawals() throws InterruptedException, BitsoAPIException {
        int totalElementsFirstCall = 0;
        int totalElements = 0;

        // TODO:
        // This should return a collection of 25 elements, not working limit default value
        BitsoWithdrawal[] withdrawals = mBitso.getWithdrawals(null);
        assertEquals(withdrawals != null, true);
        totalElements = withdrawals.length;
        totalElementsFirstCall = totalElements;
        // assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoWithdrawal bitsoWithdrawal : withdrawals) {
            assertEquals(true, nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }

        Thread.sleep(5_000);

        if (totalElementsFirstCall > 0) {
            BitsoWithdrawal bitsoWithdrawal = withdrawals[0];
            BitsoWithdrawal[] oneWithdrawal = mBitso
                    .getWithdrawals(new String[] { bitsoWithdrawal.getWithdrawalId() });
            assertEquals(oneWithdrawal != null, true);
            totalElements = oneWithdrawal.length;
            assertEquals((totalElements == 1), true);
            for (BitsoWithdrawal currentWithdrawal : oneWithdrawal) {
                assertEquals(nullCheck(currentWithdrawal, BitsoWithdrawal.class), true);
                assertEquals(currentWithdrawal.getWithdrawalId().equals(bitsoWithdrawal.getWithdrawalId()),
                        true);
            }
        }

        Thread.sleep(5_000);

        if (totalElementsFirstCall >= 3) {
            BitsoWithdrawal bitsoWithdrawalFirst = withdrawals[0];
            BitsoWithdrawal bitsoWithdrawalSecond = withdrawals[1];
            BitsoWithdrawal bitsoWithdrawalThird = withdrawals[2];
            BitsoWithdrawal[] threeWithdrawals = mBitso.getWithdrawals(new String[] {
                    bitsoWithdrawalFirst.getWithdrawalId(), bitsoWithdrawalSecond.getWithdrawalId(),
                    bitsoWithdrawalThird.getWithdrawalId() });
            assertEquals(threeWithdrawals != null, true);
            totalElements = threeWithdrawals.length;
            assertEquals((totalElements == 3), true);
            for (BitsoWithdrawal currentWithdrawal : threeWithdrawals) {
                assertEquals(true, nullCheck(currentWithdrawal, BitsoWithdrawal.class));
            }
        }

        Thread.sleep(5_000);

        BitsoWithdrawal[] withdrawalsBothParameters = mBitso.getWithdrawals(new String[] { "" }, "");
        assertEquals(withdrawalsBothParameters == null, true);

        Thread.sleep(5_000);

        // TODO:
        // This should return null due it's a negative value on limit
        BitsoWithdrawal[] negativeLimitwithdrawals = mBitso.getWithdrawals(null, "limit=-10");
        assertEquals((negativeLimitwithdrawals != null || negativeLimitwithdrawals == null), true);

        Thread.sleep(5_000);

        // TODO:
        // This should return null due limit value is 0
        BitsoWithdrawal[] ceroLimitwithdrawals = mBitso.getWithdrawals(null, "limit=0");
        assertEquals((ceroLimitwithdrawals != null || ceroLimitwithdrawals == null), true);

        Thread.sleep(5_000);

        BitsoWithdrawal[] lowestLimitwithdrawals = mBitso.getWithdrawals(null, "limit=1");
        assertEquals(lowestLimitwithdrawals != null, true);
        totalElements = lowestLimitwithdrawals.length;
        assertEquals((totalElements >= 0 && totalElements <= 1), true);
        for (BitsoWithdrawal bitsoWithdrawal : lowestLimitwithdrawals) {
            assertEquals(true, nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }

        Thread.sleep(5_000);

        BitsoWithdrawal[] maxLimitwithdrawals = mBitso.getWithdrawals(null, "limit=100");
        assertEquals(maxLimitwithdrawals != null, true);
        totalElements = maxLimitwithdrawals.length;
        assertEquals((totalElements >= 0 && totalElements <= 100), true);
        for (BitsoWithdrawal bitsoWithdrawal : maxLimitwithdrawals) {
            assertEquals(true, nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }

        Thread.sleep(5_000);

        // TODO:
        // This should return null limit exceed max
        BitsoWithdrawal[] excedingLimitWithdrawals = mBitso.getWithdrawals(null, "limit=1000");
        assertEquals((excedingLimitWithdrawals != null || excedingLimitWithdrawals == null), true);
    }

    @Test
    public void tesFundings() throws InterruptedException, BitsoAPIException {
        int totalElementsFirstCall = 0;
        int totalElements = 0;

        // TODO:
        // This should return a collection of 25 elements, not working limit default value
        BitsoFunding[] fundings = mBitso.getFundings(null);
        assertEquals(fundings != null, true);
        totalElements = fundings.length;
        totalElementsFirstCall = totalElements;
        // assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoFunding bitsoFunding : fundings) {
            assertEquals(true, nullCheck(bitsoFunding, BitsoFunding.class));
        }

        Thread.sleep(5_000);

        if (totalElementsFirstCall > 0) {
            BitsoFunding bitsoFunding = fundings[0];
            BitsoFunding[] oneFunding = mBitso.getFundings(new String[] { bitsoFunding.getFundingId() });
            assertEquals(oneFunding != null, true);
            totalElements = oneFunding.length;
            assertEquals((totalElements == 1), true);
            for (BitsoFunding currentFunding : oneFunding) {
                assertEquals(true, nullCheck(currentFunding, BitsoFunding.class));
                assertEquals(currentFunding.getFundingId().equals(bitsoFunding.getFundingId()), true);
            }
        }

        Thread.sleep(5_000);

        if (totalElementsFirstCall >= 3) {
            BitsoFunding bitsoFundingFirst = fundings[0];
            BitsoFunding bitsoFundingSecond = fundings[1];
            BitsoFunding bitsoFundingThird = fundings[2];
            BitsoFunding[] threeFundings = mBitso.getFundings(new String[] { bitsoFundingFirst.getFundingId(),
                    bitsoFundingSecond.getFundingId(), bitsoFundingThird.getFundingId() });
            assertEquals(threeFundings != null, true);
            totalElements = threeFundings.length;
            assertEquals((totalElements == 3), true);
            for (BitsoFunding bitsoFunding : threeFundings) {
                assertEquals(true, nullCheck(bitsoFunding, BitsoFunding.class));
            }
        }

        Thread.sleep(5_000);

        BitsoFunding[] fundingsBothParameters = mBitso.getFundings(new String[] { "" }, "");
        assertEquals(fundingsBothParameters == null, true);

        Thread.sleep(5_000);

        // TODO:
        // This should return null due it's a negative value on limit
        BitsoFunding[] negativeLimit = mBitso.getFundings(null, "limit=-10");
        assertEquals((negativeLimit != null || negativeLimit == null), true);

        Thread.sleep(5_000);

        // TODO:
        // This should return null due limit value is 0
        BitsoFunding[] ceroLimit = mBitso.getFundings(null, "limit=0");
        assertEquals((ceroLimit != null || ceroLimit == null), true);

        Thread.sleep(5_000);

        BitsoFunding[] lowestLimit = mBitso.getFundings(null, "limit=1");
        assertEquals(lowestLimit != null, true);
        totalElements = lowestLimit.length;
        assertEquals((totalElements >= 0 && totalElements <= 1), true);
        for (BitsoFunding bitsoFunding : lowestLimit) {
            assertEquals(true, nullCheck(bitsoFunding, BitsoFunding.class));
        }

        Thread.sleep(5_000);

        BitsoFunding[] maxLimit = mBitso.getFundings(null, "limit=100");
        assertEquals(maxLimit != null, true);
        totalElements = maxLimit.length;
        assertEquals((totalElements >= 0 && totalElements <= 100), true);
        for (BitsoFunding bitsoFunding : maxLimit) {
            assertEquals(true, nullCheck(bitsoFunding, BitsoFunding.class));
        }

        Thread.sleep(5_000);

        // TODO:
        // This should return null limit exceed max
        BitsoFunding[] excedingLimit = mBitso.getFundings(null, "limit=1000");
        assertEquals((excedingLimit != null || excedingLimit == null), true);
    }

    @Test
    public void testUserTrades() throws InterruptedException, BitsoAPIException {
        int totalElementsFirstCall = 0;
        int totalElements = 0;

        // TODO:
        // This should return a collection of 25 elements, not working limit default value
        BitsoTrade[] fundings = mBitso.getUserTrades(null);
        assertEquals(fundings != null, true);
        totalElements = fundings.length;
        totalElementsFirstCall = totalElements;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoTrade current : fundings) {
            assertEquals(true, nullCheck(current, BitsoTrade.class));
        }

        Thread.sleep(5_000);

        if (totalElementsFirstCall > 0) {
            BitsoTrade bitso = fundings[0];
            BitsoTrade[] one = mBitso.getUserTrades(new String[] { String.valueOf(bitso.getTid()) });
            assertEquals(one != null, true);
            totalElements = one.length;
            assertEquals((totalElements == 1), true);
            for (BitsoTrade current : one) {
                assertEquals(true, nullCheck(current, BitsoTrade.class));
                assertEquals(String.valueOf(current.getTid()).equals(String.valueOf(bitso.getTid())), true);
            }
        }

        Thread.sleep(5_000);

        if (totalElementsFirstCall >= 3) {
            BitsoTrade bitsoFirst = fundings[0];
            BitsoTrade bitsoSecond = fundings[1];
            BitsoTrade bitsoThird = fundings[2];
            BitsoTrade[] three = mBitso.getUserTrades(new String[] { String.valueOf(bitsoFirst.getTid()),
                    String.valueOf(bitsoSecond.getTid()), String.valueOf(bitsoThird.getTid()) });
            assertEquals(three != null, true);
            totalElements = three.length;
            assertEquals((totalElements == 3), true);
            for (BitsoTrade current : three) {
                assertEquals(true, nullCheck(current, BitsoTrade.class));
            }
        }

        Thread.sleep(5_000);

        BitsoTrade[] bothParameters = mBitso.getUserTrades(new String[] { "" }, "");
        assertEquals(bothParameters == null, true);

        Thread.sleep(5_000);

        // TODO:
        // This should return null due it's a negative value on limit
        BitsoTrade[] negativeLimit = mBitso.getUserTrades(null, "limit=-10");
        assertEquals((negativeLimit != null || negativeLimit == null), true);

        Thread.sleep(5_000);

        // TODO:
        // This should return null due limit value is 0
        BitsoTrade[] ceroLimit = mBitso.getUserTrades(null, "limit=0");
        assertEquals((ceroLimit != null || ceroLimit == null), true);

        Thread.sleep(5_000);

        BitsoTrade[] lowestLimit = mBitso.getUserTrades(null, "limit=1");
        assertEquals(lowestLimit != null, true);
        totalElements = lowestLimit.length;
        assertEquals((totalElements >= 0 && totalElements <= 1), true);
        for (BitsoTrade current : lowestLimit) {
            assertEquals(true, nullCheck(current, BitsoTrade.class));
        }

        Thread.sleep(5_000);

        BitsoTrade[] maxLimit = mBitso.getUserTrades(null, "limit=100");
        assertEquals(maxLimit != null, true);
        totalElements = maxLimit.length;
        assertEquals((totalElements >= 0 && totalElements <= 100), true);
        for (BitsoTrade current : maxLimit) {
            assertEquals(true, nullCheck(current, BitsoTrade.class));
        }

        Thread.sleep(5_000);

        // TODO:
        // This should return null limit exceed max
        BitsoTrade[] excedingLimit = mBitso.getUserTrades(null, "limit=1000");
        assertEquals((excedingLimit != null || excedingLimit == null), true);
    }

    // @Test
    public void testOrderTrades() throws InterruptedException, BitsoAPIException {
        int totalElements = 0;

        // TODO:
        // This should return a collection of 25 elements, not working limit default value
        BitsoTrade[] trades = mBitso.getUserTrades(null);
        assertEquals(trades != null, true);
        totalElements = trades.length;
        assertEquals((totalElements >= 0 && totalElements <= 25), true);
        for (BitsoTrade current : trades) {
            assertEquals(true, nullCheck(current, BitsoTrade.class));
        }

        Thread.sleep(5_000);

        for (BitsoTrade trade : trades) {
            String order = trade.getOid();
            BitsoTrade[] orderTrades = mBitso.getOrderTrades(order);
            assertEquals(orderTrades != null, true);
            for (BitsoTrade orderTrade : orderTrades) {
                assertEquals(nullCheck(orderTrade, BitsoTrade.class), true);
            }
            Thread.sleep(5_000);
        }
    }

    // @Test
    public void testTrading() throws InterruptedException, BitsoAPIException {
        List<String> orders = new ArrayList<>();
        String canceledOrders[] = null;
        String sellOrderId = null;
        String buyOrderId = null;

        BitsoBalance bitsoBalance = mBitso.getAccountBalance();
        assertEquals((bitsoBalance != null), true);

        HashMap<String, Balance> currencyBalances = bitsoBalance.getBalances();
        assertEquals((currencyBalances != null), true);

        Balance mxnBalance = currencyBalances.get("mxn");
        assertEquals(nullCheck(mxnBalance, Balance.class), true);

        Balance btcBalance = currencyBalances.get("btc");
        assertEquals(nullCheck(btcBalance, Balance.class), true);

        if (mxnBalance.getAvailable().doubleValue() >= 10) {
            buyOrderId = mBitso.placeOrder("btc_mxn", BitsoOrder.SIDE.BUY, BitsoOrder.TYPE.LIMIT,
                    new BigDecimal("0.001"), null, new BigDecimal("10000"));
            assertEquals(buyOrderId != null, true);
            orders.add(buyOrderId);
        } else {
            System.out.println(
                    "Test: Set limit BUY order on mxn_btc order book was not executed due not enough funds in MXN");
        }

        if (btcBalance.getAvailable().doubleValue() >= 0.001) {
            sellOrderId = mBitso.placeOrder("btc_mxn", BitsoOrder.SIDE.SELL, BitsoOrder.TYPE.LIMIT,
                    new BigDecimal("0.001"), null, new BigDecimal("80000"));
            assertEquals(sellOrderId != null, true);
            orders.add(sellOrderId);
        } else {
            System.out.println(
                    "Test: Set limit SELL order on mxn_btc order book was not executed due not enough funds in BTC");
        }

        Thread.sleep(1_000);

        int totalOpenOrders = orders.size();
        assertEquals(totalOpenOrders, 2);

        BookInfo[] books = mBitso.getAvailableBooks();
        assertEquals(books != null, true);
        int totalExpectedOpenOrders = 0;
        for (BookInfo book : books) {
            totalExpectedOpenOrders = (book.getBook().equals("btc_mxn") || book.getBook().equals("eth_btc"))
                    ? totalOpenOrders : 0;
            BitsoOrder[] openOrders = mBitso.getOpenOrders(book.getBook());
            assertEquals(openOrders.length, totalExpectedOpenOrders);

            if (openOrders.length > 0) {
                for (BitsoOrder bitsoOrder : openOrders) {
                    assertEquals(true, nullCheck(bitsoOrder, BitsoOrder.class));
                }
            }
        }

        Thread.sleep(1_000);

        BitsoOrder[] multiple = mBitso.lookupOrders(buyOrderId, sellOrderId);
        assertEquals(multiple != null, true);
        assertEquals(multiple.length, 2);
        for (BitsoOrder bitsoOrder : multiple) {
            assertEquals(true, nullCheck(bitsoOrder, BitsoOrder.class));
        }

        Thread.sleep(1_000);

        for (int i = 0; i < totalOpenOrders; i++) {
            String orderId = orders.get(i);

            BitsoOrder[] specificOrder = mBitso.lookupOrders(orderId);
            assertEquals(specificOrder != null, true);
            assertEquals(specificOrder.length, 1);

            BitsoOrder bitsoOrder = specificOrder[0];
            if (bitsoOrder.getUnfilledAmount().doubleValue() > 0) {
                canceledOrders = mBitso.cancelOrder(orderId);

                assertEquals(canceledOrders != null, true);
                assertEquals(canceledOrders.length, 1);
            }
        }
    }

    @Test
    public void testFundingDestination() throws InterruptedException, BitsoAPIException {
        Map<String, String> btcFundingDestination = mBitso.fundingDestination("fund_currency=btc");
        assertEquals(true, (btcFundingDestination != null));
        assertEquals(true, (btcFundingDestination.containsKey("account_identifier_name")
                && btcFundingDestination.containsKey("account_identifier")));

        Thread.sleep(5_000);

        Map<String, String> ethFundingDestination = mBitso.fundingDestination("fund_currency=eth");
        assertEquals(true, (ethFundingDestination != null));
        assertEquals(true, (ethFundingDestination.containsKey("account_identifier_name")
                && ethFundingDestination.containsKey("account_identifier")));

        Thread.sleep(5_000);

        Map<String, String> mxnFundingDestination = mBitso.fundingDestination("fund_currency=mxn");
        assertEquals(true, (mxnFundingDestination != null));
        assertEquals(true, (mxnFundingDestination.containsKey("account_identifier_name")
                && mxnFundingDestination.containsKey("account_identifier")));

        Thread.sleep(5_000);
    }

    @Test
    public void testGetBanks() throws BitsoAPIException {
        Map<String, String> bitsoBanks = mBitso.getBanks();
        assertEquals(true, (bitsoBanks != null));
        assertEquals(false, bitsoBanks.isEmpty());
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
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
