package com.bitso;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exchange.BookOrder;
import com.bitso.helpers.Helpers;

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

    /**
     * Old list of trades
     *
     * @deprecated use {@link #trades} instead.
     */
    @Deprecated
    public ArrayList<BookOrder> list = new ArrayList<BookOrder>();
    public ArrayList<BookOrder> trades = new ArrayList<BookOrder>();
    public ArrayList<Movement> movements = new ArrayList<Movement>();

    public BitsoUserTransactions(JSONArray a, BitsoBook book) {
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.getJSONObject(i);
            String dateTime = o.getString("datetime");
            int transactionType = o.getInt("type");
            if (transactionType == 0 || transactionType == 1) {
                Movement m = new Movement();
                if (transactionType == 0) {
                    m.type = Movement.TYPE.DEPOSIT;
                }
                if (transactionType == 1) {
                    m.type = Movement.TYPE.WITHDRAWAL;
                }
                m.dateTime = dateTime;
                if (o.has("mxn")) {
                    m.mxn = new BigDecimal(o.getString("mxn"));
                } else if (o.has("btc")) {
                    m.btc = new BigDecimal(o.getString("btc"));
                } else if (o.has("eth")) {
                    m.eth = new BigDecimal(o.getString("eth"));
                }
                m.method = o.getString("method");
                movements.add(m);
            } else if (transactionType == 2) {
                BitsoCurrency majorCurrency = book.getMajor();
                BitsoCurrency minorCurrency = book.getMinor();
                BigDecimal price = new BigDecimal(o.getString("rate"));
                BigDecimal major = new BigDecimal(o.getString(majorCurrency.toString()));

                BigDecimal minor = new BigDecimal(o.getString(minorCurrency.toString()));
                BookOrder.TYPE type = BookOrder.TYPE.SELL;
                if (major.compareTo(BigDecimal.ZERO) > 0) {
                    type = BookOrder.TYPE.BUY;
                }
                BookOrder order = new BookOrder(price, major, type);
                order.book = book.toString();
                order.id = o.getString("order_id");
                order.dateTime = dateTime;
                order.status = BookOrder.STATUS.COMPLETE;
                order.minor = minor;
                trades.add(order);
                list.add(order);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Movements\n");
        for (Movement m : movements) {
            sb.append(m);
        }

        sb.append("\n\nTrades\n");
        for (BookOrder o : trades) {
            sb.append(o);
        }
        return sb.toString();
    }

    public static class Movement {

        public static enum TYPE {
            DEPOSIT, WITHDRAWAL
        }

        public String dateTime;
        public TYPE type;
        public BigDecimal mxn;
        public BigDecimal btc;
        public BigDecimal eth;
        public String method;

        public String toString() {
            return Helpers.fieldPrinter(this);
        }
    }
}
