package com.bitso;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.BitsoTransactions.Transaction.SIDE;
import com.bitso.helpers.Helpers;

public class BitsoTransactions {
    public ArrayList<Transaction> list;

    public BitsoTransactions(JSONArray a) {
        list = new ArrayList<Transaction>(a.length());
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.getJSONObject(i);
            Transaction t = new Transaction();
            t.date = Long.parseLong(o.getString("date"));
            t.tid = o.getLong("tid");
            t.price = new BigDecimal(o.getString("price"));
            t.amount = new BigDecimal(o.getString("amount"));
            t.side = SIDE.valueOf(o.getString("side").toUpperCase());
            list.add(t);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bitso Recent Transactions\n");
        for (Transaction t : list) {
            sb.append(t);
        }

        return sb.toString();
    }

    public static class Transaction {

        public enum SIDE {
            SELL, BUY
        };

        long date;
        long tid;
        BigDecimal price;
        BigDecimal amount;
        SIDE side;

        public String toString() {
            return Helpers.fieldPrinter(this);
        }
    }
}
