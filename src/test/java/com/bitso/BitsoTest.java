package com.bitso;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.bitso.exchange.BookInfo;
import com.bitso.exchange.OrderBook;
import com.bitso.exchange.Ticker;

public class BitsoTest {
    Bitso bitso;

    @Before
    public void setUp() throws Exception {
        // bitso_dev_public_key
        // bitso_dev_private
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
            BitsoOrderBook ob = (BitsoOrderBook) bitso.getOrderBook(bi.book);
            assertEquals(nullCheck(ob, OrderBook.class), true);
        }
    }

    // Private endpoints
    @Test
    public void testUserAccountStatus() {
        BitsoAccountStatus status = bitso.getUserAccountStatus();
        assertEquals(nullCheck(status, BitsoAccountStatus.class), true);
    }

    @Test
    public void testUserAccountBalance(){
        BitsoBalance balance = bitso.getUserAccountBalance();
        System.out.println(balance);
        assertEquals(true, nullCheck(balance, BitsoBalance.class));
    }

    @Test
    public void testUserFees(){
        BitsoFee fee = bitso.getUserFees();
        assertEquals(true, nullCheck(fee, BitsoFee.class));
    }

    @Test
    public void testUserLedgers(){
        String[] operations = {"trades", "fees", "fundings", "withdrawals"};
        // Global ledger request
        BitsoLedger fullLedger = bitso.getUserLedger();
        assertEquals(true, nullCheck(fullLedger, BitsoLedger.class));

        // Specific operation type request
        for(String operationType : operations){
            BitsoLedger specificLedger = bitso.getUserLedger(operationType);
            assertEquals(true, nullCheck(specificLedger, BitsoLedger.class));
        }
    }

    @Test
    public void testUserWithdrawals(){
        // Testing withdrawal ids
        String[] wids = {"65532d428d4c1b2642833b9e78c1b9fd", "ea19499f49aa20a86eaf29ebe6af3f9e",
                "d5764355792aff733f31ee7bfc38a832", "e7dba07657459c194514d3088d117e18"};
        BitsoWithdrawal fullWithdraws = bitso.getUserWithdrawals();
        assertEquals(true, nullCheck(fullWithdraws, BitsoWithdrawal.class));

        // Specific withdrawal id
        BitsoWithdrawal specificWithdraw = bitso.getUserWithdrawals(wids[0]);
        assertEquals(true, nullCheck(specificWithdraw, BitsoWithdrawal.class));

        // Multiple withdrawal ids
        BitsoWithdrawal specificWithdrawal =  bitso.getUserWithdrawals(wids);
        assertEquals(true, nullCheck(specificWithdrawal, BitsoWithdrawal.class));
    }

    // need to specify the class because java reflection is bizarre
    // and if you want to check the parent class of the object its
    // easier to just specify the class
    public boolean nullCheck(Object obj, Class<?> genericType) {
        Field[] fields = genericType.getDeclaredFields();
        for (Field f : fields) {
            try {
                if (f.get(obj) == null) return false;
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;

    }

}
