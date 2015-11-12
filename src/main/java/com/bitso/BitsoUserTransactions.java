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

    /**
     * Old list of trades
     *
     * @deprecated use {@link #trades} instead.
     */
    @Deprecated
    public ArrayList<BookOrder> list = new ArrayList<BookOrder>();
    public ArrayList<BookOrder> trades = new ArrayList<BookOrder>();
    public ArrayList<Deposit> deposits = new ArrayList<Deposit>();
    public ArrayList<Withdrawal> withdrawals = new ArrayList<Withdrawal>();

    public BitsoUserTransactions(JSONArray a) {
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.getJSONObject(i);
            String dateTime = o.getString("datetime");
            int transactionType = o.getInt("type");
            if (transactionType == 0) {
                Deposit d = new Deposit();
                d.dateTime = dateTime;
                d.amount = new BigDecimal(o.getString("amount"));
                d.method = o.getString("method");
                d.currency = o.getString("currency");
                deposits.add(d);
            } else if (transactionType == 1) {
                Withdrawal w = new Withdrawal();
                w.dateTime = dateTime;
                w.amount = new BigDecimal(o.getString("amount"));
                w.method = o.getString("method");
                w.currency = o.getString("currency");
                withdrawals.add(w);
            } else if (transactionType == 2) {
                BigDecimal price = new BigDecimal(o.getString("rate"));
                BigDecimal major = new BigDecimal(o.getString("btc"));
                BigDecimal minor = new BigDecimal(o.getString("mxn"));
                BookOrder.TYPE type = BookOrder.TYPE.SELL;
                if (major.compareTo(BigDecimal.ZERO) > 0) {
                    type = BookOrder.TYPE.BUY;
                }
                BookOrder order = new BookOrder(price, major, type);
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
        sb.append("Deposits\n");
        for (Deposit d : deposits) {
            sb.append(d);
        }
        sb.append("\n\nWithdrawals\n");
        for (Withdrawal w : withdrawals) {
            sb.append(w);
        }
        sb.append("\n\nTrades\n");
        for (BookOrder o : trades) {
            sb.append(o);
        }
        return sb.toString();
    }

    public class Deposit {
        public String dateTime;
        public BigDecimal amount;
        public String method;
        public String currency;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=====\ndateTime=");
            sb.append(dateTime);
            sb.append("\namount=");
            sb.append(amount);
            sb.append("\ncurrency=");
            sb.append(currency);
            sb.append("\nmethod=");
            sb.append(method);
            sb.append("\n=====");
            return sb.toString();
        }
    }

    public class Withdrawal {
        public String dateTime;
        public BigDecimal amount;
        public String method;
        public String currency;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=====\ndateTime=");
            sb.append(dateTime);
            sb.append("\namount=");
            sb.append(amount);
            sb.append("\ncurrency=");
            sb.append(currency);
            sb.append("\nmethod=");
            sb.append(method);
            sb.append("\n=====");
            return sb.toString();
        }
    }
}
