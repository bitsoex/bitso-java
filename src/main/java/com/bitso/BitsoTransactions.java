package com.bitso;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoTransactions {
    private Transaction[] mTransactionsList;

    public BitsoTransactions(JSONArray jsonArray) {
        int totalElements = jsonArray.length();
        mTransactionsList = new Transaction[totalElements];
        for (int i = 0; i < totalElements; i++) {
            JSONObject o = jsonArray.getJSONObject(i);
            Transaction transaction = new Transaction(Helpers.getZonedDatetime(o, "created_at"),
                    String.valueOf(o.getInt("tid")), Helpers.getBD(o, "price"), Helpers.getBD(o, "amount"),
                    BitsoSide.valueOf(Helpers.getString(o, "maker_side").toUpperCase()),
                    BitsoBook.valueOf(Helpers.getString(o, "book").toUpperCase()));
            mTransactionsList[i] = transaction;
        }
    }

    public Transaction[] getTransactionsList() {
        return mTransactionsList;
    }

    public void setmTransactionsList(Transaction[] mTransactionsList) {
        this.mTransactionsList = mTransactionsList;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Bitso Recent Transactions\n");
        for (Transaction transaction : mTransactionsList) {
            stringBuilder.append(transaction);
        }

        return stringBuilder.toString();
    }

    public class Transaction {
        private Date date;
        private String tid;
        private BigDecimal price;
        private BigDecimal amount;
        private BitsoSide side;
        private BitsoBook book;

        public Transaction(Date date, String tid, BigDecimal price, BigDecimal amount, BitsoSide side,
                BitsoBook book) {
            super();
            this.date = date;
            this.tid = tid;
            this.price = price;
            this.amount = amount;
            this.side = side;
            this.book = book;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getTid() {
            return tid;
        }

        public void setTid(String tid) {
            this.tid = tid;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BitsoSide getSide() {
            return side;
        }

        public void setSide(BitsoSide side) {
            this.side = side;
        }

        public BitsoBook getBook() {
            return book;
        }

        public void setBook(BitsoBook book) {
            this.book = book;
        }

        public String toString() {
            return Helpers.fieldPrinter(this, BitsoTransactions.Transaction.class);
        }
    }
}
