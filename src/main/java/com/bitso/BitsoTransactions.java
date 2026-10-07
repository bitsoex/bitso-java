package com.bitso;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.ToString;
import lombok.Value;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

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

    @Value
    @ToString
    public class Transaction {
        Date date;
        String tid;
        BigDecimal price;
        BigDecimal amount;
        BitsoOrder.SIDE side;
        String book;
    }
}
