package com.bitso;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.bitso.exchange.BookInfo;
import com.bitso.exchange.Order;
import com.bitso.exchange.OrderBook;
import com.bitso.exchange.Ticker;
import com.bitso.exchange.Withdrawal;

public class BitsoTest {
    Bitso bitso;

    @Before
    public void setUp() throws Exception {
        String secret = System.getenv("bitso_dev_private");
        String key = System.getenv("bitso_dev_public_key");
        bitso = new Bitso(key, secret, null, 0, true, false);
    }

    // @Test
    public void testAvailableBooks() {
        ArrayList<BookInfo> books = bitso.availableBooks();
        for (BookInfo bi : books) {
            assertEquals(nullCheck(bi, BookInfo.class), true);
        }
        assertEquals(books.size() > 0, true);
    }

    // @Test
    public void testTicker() {
        BitsoTicker bb = bitso.getTicker(BitsoBook.BTC_MXN);
        assertEquals(nullCheck(bb, Ticker.class), true);
        assertEquals(nullCheck(bb, BitsoTicker.class), true);
    }

    // @Test
    public void testGetOrderBook() {
        ArrayList<BookInfo> books = bitso.availableBooks();
        for (BookInfo bi : books) {
            BitsoOrder ob = (BitsoOrder) bitso.getOrderBook(bi.book);
            assertEquals(nullCheck(ob, OrderBook.class), true);
        }
    }

    // Private endpoints
    // @Test
    public void testUserAccountStatus() {
        BitsoAccountStatus status = bitso.getUserAccountStatus();
        assertEquals(nullCheck(status, BitsoAccountStatus.class), true);
    }

    // @Test
    public void testUserAccountBalance(){
        BitsoBalance balance = bitso.getUserAccountBalance();
        assertEquals(true, nullCheck(balance, BitsoBalance.class));
    }

    // @Test
    public void testUserFees(){
        BitsoFee fee = bitso.getUserFees();
        assertEquals(true, nullCheck(fee, BitsoFee.class));
    }

    // @Test
    public void testUserLedgers(){
        String[] operations = {"trades", "fees", "fundings", "withdrawals"};
        // Global ledger request
        BitsoLedger fullLedger = bitso.getUserLedger(null);
        assertEquals(true, nullCheck(fullLedger, BitsoLedger.class));

        // Specific operation type request
        for(String operationType : operations){
            BitsoLedger specificLedger = bitso.getUserLedger(operationType);
            assertEquals(true, nullCheck(specificLedger, BitsoLedger.class));
        }
    }

    // @Test
    public void testUserWithdrawals(){
        // Testing withdrawal ids
        String[] wids = {"65532d428d4c1b2642833b9e78c1b9fd", "d5764355792aff733f31ee7bfc38a832",
                "e7dba07657459c194514d3088d117e18"};
        BitsoWithdrawal fullWithdraws = bitso.getUserWithdrawals();
        assertEquals(true, nullCheck(fullWithdraws, BitsoWithdrawal.class));

        // Specific withdrawal id
        BitsoWithdrawal specificWithdraw = bitso.getUserWithdrawals(wids[0]);
        assertEquals(true, nullCheck(specificWithdraw, BitsoWithdrawal.class));

        // Multiple withdrawal ids
        BitsoWithdrawal multipleWithdraws =  bitso.getUserWithdrawals(wids);
        assertEquals(true, nullCheck(multipleWithdraws, BitsoWithdrawal.class));
    }

    // @Test
    public void testUserFundings(){
        // Testing funding ids
        String[] fids = {"2ab6b5cccf2be8d1fb8382234203f8e1", "e1b96fe7d22cfbfdb83df51a68eca9b0",
                "1ae6d8af23111799698a4821b8d1d156", "7a68bac79c89af4bc24dd153f535ad54"};
        BitsoFunding fullFundings = bitso.getUserFundings();
        assertEquals(true, nullCheck(fullFundings, BitsoFunding.class));

        // Specific funding id
        BitsoFunding specificFunding = bitso.getUserFundings(fids[0]);
        assertEquals(true, nullCheck(specificFunding, BitsoFunding.class));

        // Multiple funding ids
        BitsoFunding multipleFundings = bitso.getUserFundings(fids);
        assertEquals(true, nullCheck(multipleFundings, BitsoFunding.class));
    }

    // @Test
    public void testUserTrades(){
        // Testing trades ids
        String[] tids = {"1431", "1430", "1429", "1428"};
        BitsoTrade fullTrades = bitso.getUserTrades();
        assertEquals(true, nullCheck(fullTrades, BitsoTrade.class));

        // Specific trade id
        BitsoTrade specificTrade = bitso.getUserTrades(tids[0]);
        assertEquals(true, nullCheck(specificTrade, BitsoTrade.class));

        // Multiple trade ids
        BitsoTrade multipleTrades = bitso.getUserTrades(tids);
        assertEquals(true, nullCheck(multipleTrades, BitsoTrade.class));
    }

    // @Test
    public void testOpenOrders(){
        BitsoOrder orders = bitso.getOpenOrders();
        assertEquals(true, nullCheck(orders, BitsoOrder.class));
    }

    // @Test
    public void testlookupOrders(){
        String[] values = {"kRrcjsp5n9og98qa", "V4RVg7OJ1jl5O5Om", "4fVvpQrR59M26ojl",
                "Rhvak2cOOX552s69", "n8JvMOl4iO8s22r2"};

        BitsoOrder specificOrder = bitso.lookupOrders(values[0]);
        assertEquals(true, nullCheck(specificOrder, BitsoOrder.class));

        BitsoOrder multipleOrders = bitso.lookupOrders(values);
        assertEquals(true, nullCheck(multipleOrders, BitsoOrder.class));
    }

    // @Test
    public void testCancelUserOrder() {
        String[] orders = { "pj251R8m6im5lO82", "4nQl95irVlfQRkXp", "vdfVrXVQJ0iJdV6h" };

        String[] cancelParticularOrder = bitso.cancelOrder(orders[0]);
        assertEquals(true, (cancelParticularOrder != null));

        String[] cancelMultipleOrders = bitso.cancelOrder(orders);
        assertEquals(true, (cancelMultipleOrders != null));

        String[] cancelAllOrders = bitso.cancelOrder("all");
        assertEquals(true, (cancelAllOrders != null));
    }

    @Test
    public void testPlaceUserOrder(){
        String orderId = bitso.placeOrder(BitsoBook.BTC_MXN, Order.SIDE.BUY,
                Order.TYPE.LIMIT, new BigDecimal("15.4"), null,
                new BigDecimal("20854.4"));
        assertEquals(true, ((orderId != null) && (orderId.length() > 0)));
    }

    @Test
    public void testFundingDestination(){
        Map<String, String> btcFundingDestination = bitso.fundingDestination("btc");
        assertEquals(true, (btcFundingDestination != null));
        assertEquals(true, (btcFundingDestination.containsKey("accountIdentifierName") &&
                btcFundingDestination.containsKey("accountIdentifier")));

        Map<String, String> ethFundingDestination = bitso.fundingDestination("eth");
        assertEquals(true, (ethFundingDestination != null));
        assertEquals(true, (ethFundingDestination.containsKey("accountIdentifierName") &&
                ethFundingDestination.containsKey("accountIdentifier")));

        Map<String, String> mxnFundingDestination = bitso.fundingDestination("mxn");
        assertEquals(true, (mxnFundingDestination != null));
        assertEquals(true, (mxnFundingDestination.containsKey("accountIdentifierName") &&
                mxnFundingDestination.containsKey("accountIdentifier")));
    }

    @Test
    public void testCurrencyWithdrawals(){
        Withdrawal btcWithdrawal =  bitso.bitcoinWithdrawal(new BigDecimal("0.001"),
                "31yTCKDHTqNXF5eZcsddJDe76BzBh8pVLb");
        assertEquals(true, nullCheck(btcWithdrawal, Withdrawal.class));

        // TODO:
        // Check server response, json wiht null values
        // Withdrawal ethWithdrawal =  bitso.etherWithdrawal(new BigDecimal("0.001"),
        //        "0xc83adea9e8fea3797139942a5939b961f67abfb8");
        // assertEquals(true, nullCheck(ethWithdrawal, Withdrawal.class));
    }

/*    // TODO:
    // Error in JSON missing details key on payload
    @Test
    public void testSPEIWithdrawal(){
        Withdrawal speiWithdrawal =  bitso.speiWithdrawal(new BigDecimal("50"),
                "name", "surname", "044180001059660729", "testing reference", "5706");
        assertEquals(true, nullCheck(speiWithdrawal, Withdrawal.class));
    }*/

    @Test
    public void testGetBanks(){
        BitsoBank bitsoBanks[] = bitso.getBanks();
        assertEquals(true, (bitsoBanks != null));
    }

    @Test
    public void testDebitCardWithdrawal(){
        Withdrawal debitCardWithdrawal = bitso.debitCardWithdrawal(new BigDecimal("50"),
                "name test", "surname test", "5579209071039769", "40044");
        assertEquals(true, nullCheck(debitCardWithdrawal, Withdrawal.class));
    }

/*    // TODO:
    // Get a valid phone number that receives a SPEI transaction
    @Test
    public void testphoneWithdrawal(){
        Withdrawal phoneWithdrawal = bitso.phoneWithdrawal(new BigDecimal("50"),
                "name test", "surname test", "phone number", "40044");
        assertEquals(true, nullCheck(phoneWithdrawal, Withdrawal.class));
    }*/

    // need to specify the class because java reflection is bizarre
    // and if you want to check the parent class of the object its
    // easier to just specify the class
    public boolean nullCheck(Object obj, Class<?> genericType) {
        Field[] fields = genericType.getDeclaredFields();
        for (Field f : fields) {
            try {
                if (f.get(obj) == null){
                    System.out.println(f.getName());
                    return false;
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;

    }

}
