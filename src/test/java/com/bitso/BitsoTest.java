package com.bitso;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;

import com.bitso.exchange.BookInfo;
import com.bitso.exchange.Ticker;

public abstract class BitsoTest {
    protected Bitso mBitso;
    
    // Test public endpoints
    @Test
    public void testAvailableBooks() {
        ArrayList<BookInfo> books = mBitso.availableBooks();
        assertEquals(true, (books != null));
        for (BookInfo bi : books) {
            assertEquals(nullCheck(bi, BookInfo.class), true);
        }
        assertEquals(books.size() > 0, true);
    }

    @Test
    public void testTicker() {
        BitsoTicker bb = mBitso.getTicker(BitsoBook.BTC_MXN);
        assertEquals(nullCheck(bb, Ticker.class), true);
        assertEquals(nullCheck(bb, BitsoTicker.class), true);
    }

    @Test
    public void testOpenOrders(){
        BitsoOrder[] orders = mBitso.getOpenOrders();
        for (BitsoOrder bitsoOrder : orders) {
            assertEquals(true, nullCheck(bitsoOrder, BitsoOrder.class));
        }
    }

    // Test private endpoints
    @Test
    public void testUserAccountBalance(){
        BitsoBalance balance = mBitso.getUserAccountBalance();
        assertEquals(true, nullCheck(balance, BitsoBalance.class));
    }

    @Test
    public void testUserFees() {
        BitsoFee fee = mBitso.getUserFees();
        assertEquals(true, nullCheck(fee, BitsoFee.class));

    }

    @Test
    public void testUserLedgers(){
        BitsoOperation[] fullLedger = mBitso.getUserLedger(null);
        for (BitsoOperation bitsoOperation : fullLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
        }
    }

    @Test
    public void testUserWithdrawals(){
        BitsoWithdrawal[] fullWithdraws = mBitso.getUserWithdrawals();
        for (BitsoWithdrawal bitsoWithdrawal : fullWithdraws) {
            assertEquals(true, nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }
    }

    @Test
    public void testCurrencyWithdrawals(){
        BitsoWithdrawal btcWithdrawal =  mBitso.bitcoinWithdrawal(new BigDecimal("0.001"),
                "31yTCKDHTqNXF5eZcsddJDe76BzBh8pVLb");
        assertEquals(true, nullCheck(btcWithdrawal, BitsoWithdrawal.class));

        BitsoWithdrawal ethWithdrawal =  mBitso.etherWithdrawal(new BigDecimal("0.001"),
                "0xc83adea9e8fea3797139942a5939b961f67abfb8");
         assertEquals(true, nullCheck(ethWithdrawal, BitsoWithdrawal.class));
    }

    @Test
    public void testGetBanks(){
        Map<String, String> bitsoBanks = mBitso.getBanks();
        assertEquals(true, (bitsoBanks != null));
        assertEquals(false, bitsoBanks.isEmpty());
    }

    @Test
    public void testUserFundings(){
        BitsoFunding[] fullFundings = mBitso.getUserFundings();
        for (BitsoFunding bitsoFunding : fullFundings) {
            assertEquals(true, nullCheck(bitsoFunding, BitsoFunding.class));
        }
    }

    @Test
    public void testUserTrades(){
        BitsoTrade[] fullTrades = mBitso.getUserTrades();
        for (BitsoTrade bitsoTrade : fullTrades) {
            assertEquals(true, nullCheck(bitsoTrade, BitsoTrade.class));
        }
    }

    @Test
    public void testLookUpOrders(){
        BitsoOrder[] specificOrder = mBitso.lookupOrders("kRrcjsp5n9og98qa");
        for (BitsoOrder bitsoOrder : specificOrder) {
            assertEquals(true, nullCheck(bitsoOrder, BitsoOrder.class));
        }
    }

    @Test
    public void testCancelUserOrder() {
        String[] cancelParticularOrder = mBitso.cancelOrder("pj251R8m6im5lO82");
        assertEquals(true, (cancelParticularOrder != null));
    }

    @Test
    public void testPlaceUserOrder(){
        String orderId = mBitso.placeOrder(BitsoBook.BTC_MXN, BitsoOrder.SIDE.BUY,
                BitsoOrder.TYPE.LIMIT, new BigDecimal("15.4"), null,
                new BigDecimal("20854.4"));
        assertEquals(true, ((orderId != null) && (orderId.length() > 0)));
    }

    @Test
    public void testFundingDestination(){
        Map<String, String> btcFundingDestination = mBitso.fundingDestination("btc");
        assertEquals(true, (btcFundingDestination != null));
        assertEquals(true, (btcFundingDestination.containsKey("accountIdentifierName") &&
                btcFundingDestination.containsKey("accountIdentifier")));

        Map<String, String> ethFundingDestination = mBitso.fundingDestination("eth");
        assertEquals(true, (ethFundingDestination != null));
        assertEquals(true, (ethFundingDestination.containsKey("accountIdentifierName") &&
                ethFundingDestination.containsKey("accountIdentifier")));

        Map<String, String> mxnFundingDestination = mBitso.fundingDestination("mxn");
        assertEquals(true, (mxnFundingDestination != null));
        assertEquals(true, (mxnFundingDestination.containsKey("accountIdentifierName") &&
                mxnFundingDestination.containsKey("accountIdentifier")));
    }
    
    protected boolean nullCheck(Object obj, Class<?> genericType) {
        Field[] fields = genericType.getDeclaredFields();
        for (Field f : fields) {
            try {
                if (f.get(obj) == null){
                    System.out.println(f.getName() + " attribute is null");
                    return false;
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return true;

    }
}
