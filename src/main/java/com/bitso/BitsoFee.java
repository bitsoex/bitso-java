package com.bitso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoFee {
    private HashMap<String, Fee> mTradeFees;
    private HashMap<String, String> mWithdrawalFees;

    public BitsoFee(JSONObject o) {
        processTradeFees(o);
        processWithdrawalFees(o);
    }

    private void processTradeFees(JSONObject o) {
        mTradeFees = new HashMap<>();
        JSONArray jsonFees = o.getJSONArray("fees");
        int totalElements = jsonFees.length();
        for (int i = 0; i < totalElements; i++) {
            JSONObject fee = jsonFees.getJSONObject(i);
            String book = Helpers.getString(fee, "book");
            Fee currentFee = new Fee(book, Helpers.getBD(fee, "fee_decimal"),
                    Helpers.getBD(fee, "fee_percent"));
            mTradeFees.put(book, currentFee);
        }
    }

    private void processWithdrawalFees(JSONObject o) {
        mWithdrawalFees = new HashMap<>();
        JSONObject withdrawalFees = o.getJSONObject("withdrawal_fees");
        Iterator<String> it = withdrawalFees.keys();
        while (it.hasNext()) {
            String key = it.next();
            mWithdrawalFees.put(key, withdrawalFees.getString(key));
        }
    }

    public HashMap<String, Fee> getTradeFees() {
        return mTradeFees;
    }

    public void setTradeFees(HashMap<String, Fee> mTradeFees) {
        this.mTradeFees = mTradeFees;
    }

    public HashMap<String, String> getWithdrawalFees() {
        return mWithdrawalFees;
    }

    public void setWithdrawalFees(HashMap<String, String> mWithdrawalFees) {
        this.mWithdrawalFees = mWithdrawalFees;
    }

    public String toString() {
        return Helpers.fieldPrinter(this, BitsoFee.class);
    }

    public class Fee {
        private String mBook;
        private BigDecimal mFeeDecimal;
        private BigDecimal mFeePercent;

        public Fee(String mBook, BigDecimal mFeeDecimal, BigDecimal mFeePercent) {
            super();
            this.mBook = mBook;
            this.mFeeDecimal = mFeeDecimal;
            this.mFeePercent = mFeePercent;
        }

        public String getBook() {
            return mBook;
        }

        public void setBook(String mBook) {
            this.mBook = mBook;
        }

        public BigDecimal getFeeDecimal() {
            return mFeeDecimal;
        }

        public void setFeeDecimal(BigDecimal mFeeDecimal) {
            this.mFeeDecimal = mFeeDecimal;
        }

        public BigDecimal getFeePercent() {
            return mFeePercent;
        }

        public void setFeePercent(BigDecimal mFeePercent) {
            this.mFeePercent = mFeePercent;
        }

        public String toString() {
            return Helpers.fieldPrinter(this, BitsoFee.Fee.class);
        }
    }
}
