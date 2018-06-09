package com.bitso;

import com.bitso.helpers.Helpers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;

public class BitsoFee {
    private HashMap<String, Fee> mTradeFees;
    private HashMap<String, String> mWithdrawalFees;

    public BitsoFee(JSONObject o) {
        processTradeFees(o);
        processWithdrawalFees(o);
    }

    private void processTradeFees(JSONObject o) {
        mTradeFees = new HashMap<String, Fee>();
        JSONArray jsonFees = o.getJSONArray("fees");
        int totalElements = jsonFees.length();
        for (int i = 0; i < totalElements; i++) {
            JSONObject fee = jsonFees.getJSONObject(i);
            String book = Helpers.getString(fee, "book");
            Fee currentFee = new Fee(book, Helpers.getBD(fee, "fee_decimal"),
                    Helpers.getBD(fee, "fee_percent"),
                    Helpers.getBD(fee, "taker_fee_decimal"),
                    Helpers.getBD(fee, "taker_fee_percent"),
                    Helpers.getBD(fee, "maker_fee_decimal"),
                    Helpers.getBD(fee, "maker_fee_percent")
            );
            mTradeFees.put(book, currentFee);
        }
    }

    private void processWithdrawalFees(JSONObject o) {
        mWithdrawalFees = new HashMap<String, String>();
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
        @Deprecated
        private BigDecimal mFeeDecimal;
        @Deprecated
        private BigDecimal mFeePercent;
        private BigDecimal mTakerFeeDecimal;
        private BigDecimal mTakerFeePercent;
        private BigDecimal mMakerFeeDecimal;
        private BigDecimal mMakerFeePercent;

        public Fee(String mBook, BigDecimal mFeeDecimal, BigDecimal mFeePercent, BigDecimal mTakerFeeDecimal,
                   BigDecimal mTakerFeePercent, BigDecimal mMakerFeeDecimal, BigDecimal mMakerFeePercent) {
            super();
            this.mBook = mBook;
            this.mFeeDecimal = mFeeDecimal;
            this.mFeePercent = mFeePercent;
            this.mTakerFeeDecimal = mTakerFeeDecimal;
            this.mTakerFeePercent = mTakerFeePercent;
            this.mMakerFeeDecimal = mMakerFeeDecimal;
            this.mMakerFeePercent = mMakerFeePercent;
        }

        public String getBook() {
            return mBook;
        }

        public void setBook(String mBook) {
            this.mBook = mBook;
        }

        @Deprecated
        public BigDecimal getFeeDecimal() {
            return mFeeDecimal;
        }

        @Deprecated
        public void setFeeDecimal(BigDecimal mFeeDecimal) {
            this.mFeeDecimal = mFeeDecimal;
        }

        @Deprecated
        public BigDecimal getFeePercent() {
            return mFeePercent;
        }

        @Deprecated
        public void setFeePercent(BigDecimal mFeePercent) {
            this.mFeePercent = mFeePercent;
        }

        public String toString() {
            return Helpers.fieldPrinter(this, BitsoFee.Fee.class);
        }

        public BigDecimal getTakerFeePercent() {
            return mTakerFeePercent;
        }

        public BigDecimal getTakerFeeDecimal() {
            return mTakerFeeDecimal;
        }

        public BigDecimal getMakerFeePercent() {
            return mMakerFeePercent;
        }

        public BigDecimal getMakerFeeDecimal() {
            return mMakerFeeDecimal;
        }

    }
}
