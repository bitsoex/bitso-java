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
        BitsoLedger fullLedger = bitso.getUserLedger();
        assertEquals(true, nullCheck(fullLedger, BitsoLedger.class));
        for(int i=0; i<operations.length; i++){
            BitsoLedger specificLedger = bitso.getUserLedger(operations[i]);
            assertEquals(true, nullCheck(specificLedger, BitsoLedger.class));
        }
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
