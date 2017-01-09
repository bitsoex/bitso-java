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
        // String secret = ConfigManager.getMapValue("bitso_dev_private");
        // String key = ConfigManager.getMapValue("bitso_dev_public_key");
        bitso = new Bitso("snYcMGwkOT", "e15e750bf59789cb133ab6a1227acab4", null, 0, true, false);
    }

    @Test
    public void testAvailableBooks() {
        ArrayList<BookInfo> books = bitso.availableBooks();
        for (BookInfo bi : books) {
            assertEquals(nullCheck(bi, BookInfo.class), true);
        }
        assertEquals(books.size() > 0, true);
    }

    @Test
    public void testTicker() {
        BitsoTicker bb = bitso.getTicker(BitsoBook.BTC_MXN);
        assertEquals(nullCheck(bb, Ticker.class), true);
        assertEquals(nullCheck(bb, BitsoTicker.class), true);
    }

    @Test
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
        // unfinished test
        bitso.getUserAccountStatus();
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
