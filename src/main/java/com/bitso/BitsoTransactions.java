package com.bitso;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.ToString;
import lombok.Value;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

/** A container for anonimized trades. */
public class BitsoTransactions {
    private final List<Transaction> transactions;

    public BitsoTransactions(JSONArray jsonArray) {
        int totalElements = jsonArray.length();
        var mTransactionsList = new Transaction[totalElements];
        for (int i = 0; i < totalElements; i++) {
            JSONObject o = jsonArray.getJSONObject(i);
            Transaction transaction = new Transaction(Helpers.getZonedDatetime(o, "created_at"),
                    String.valueOf(o.getInt("tid")), Helpers.getBD(o, "price"), Helpers.getBD(o, "amount"),
                    BitsoOrder.SIDE.valueOf(Helpers.getString(o, "maker_side").toUpperCase()),
                    Helpers.getString(o, "book"));
            mTransactionsList[i] = transaction;
        }
        transactions = List.of(mTransactionsList);
    }

    public List<Transaction> getTransactionsList() {
        return transactions;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Bitso Recent Transactions\n");
        for (Transaction transaction : transactions) {
            stringBuilder.append(transaction);
        }

        return stringBuilder.toString();
    }

    /** An anonimized trade inside a book. */
    @Value
    @ToString
    public static class Transaction {
        /** When the trade occurred. */
        Date date;
        /** The public trade ID. */
        String tid;
        /** The price at which the trade occurred. */
        BigDecimal price;
        /** The amount of the trade, in the major currency. */
        BigDecimal amount;
        /** The side of the trade. */
        BitsoOrder.SIDE side;
        /** The book in which the trade happened. */
        String book;
    }
}
