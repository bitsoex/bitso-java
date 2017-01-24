package com.bitso;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import com.bitso.exchange.BookInfo;
import com.bitso.exchange.Ticker;

public class BitsoMockTest {
    private Bitso bitso;
    private ArrayList<BookInfo> mockAvailableBooks;
    private BitsoTicker mockTicker;

    @Before
    public void setUp() throws Exception{
        bitso = Mockito.mock(Bitso.class);
        setUpTestMocks();
        Mockito.when(bitso.availableBooks()).thenReturn(mockAvailableBooks);
        Mockito.when(bitso.getTicker(BitsoBook.BTC_MXN)).thenReturn(mockTicker);
        //Mockito.when(bitso.).thenReturn();
    }

    @Test
    public void testAvailableBooks() {
        ArrayList<BookInfo> books = bitso.availableBooks();
        assertEquals(true, (books != null));
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

    private void setUpTestMocks(){
        setUpAvailableBooks(BitsoMockJSON.getJSONFromFile("availableBooks.json"));
        setUpTicker(BitsoMockJSON.getJSONFromFile("ticker.json"));
    }

    private void setUpAvailableBooks(JSONObject o){
        mockAvailableBooks = new ArrayList<BookInfo>();
        JSONArray arr = o.getJSONArray("payload");
        for (int i = 0; i < arr.length(); i++) {
            mockAvailableBooks.add(new BookInfo(arr.getJSONObject(i)));
        }
    }

    private void setUpTicker(JSONObject o){
        mockTicker = new BitsoTicker(o.getJSONObject("payload"));
    }

    private boolean nullCheck(Object obj, Class<?> genericType) {
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
