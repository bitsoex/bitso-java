package com.bitso;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.bitso.exchange.BookInfo;
import com.bitso.exchange.Ticker;

public class BitsoTest {
    Bitso bitso;

    @Before
    public void setUp() throws Exception {
        bitso = new Bitso(null, null, null);
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
