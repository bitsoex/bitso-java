package com.bitso;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exchange.BookOrder;

public class BitsoUserTransactions {
    static enum SORT_ORDER {
        ASCENDING, DESCENDING;

        public String getOrder() {
            switch (this) {
                case ASCENDING:
                    return "asc";
                case DESCENDING:
                    return "desc";
            }
            return null;
        }
    }

    public ArrayList<BookOrder> list = new ArrayList<BookOrder>();

    public BitsoUserTransactions(JSONArray a) {
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.getJSONObject(i);
            BigDecimal price = new BigDecimal(o.getString("rate"));
            BigDecimal major = new BigDecimal(o.getString("btc"));
            BigDecimal minor = new BigDecimal(o.getString("mxn"));
            BookOrder.TYPE type = BookOrder.TYPE.SELL;
            if (major.compareTo(BigDecimal.ZERO) > 0) {
                type = BookOrder.TYPE.BUY;
            }
            BookOrder order = new BookOrder(price, major, type);
            order.id = o.getString("order_id");
            order.dateTime = o.getString("datetime");
            order.status = BookOrder.STATUS.COMPLETE;
            order.minor = minor;
            list.add(order);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (BookOrder o : list) {
            sb.append(o);
        }
        return sb.toString();
    }
}
