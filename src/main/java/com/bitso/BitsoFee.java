package com.bitso;

import java.math.BigDecimal;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoFee {
    private HashMap<String, Fee> mFees;

    public BitsoFee(JSONObject o) {
        mFees = new HashMap<>();
        JSONArray jsonFees = o.getJSONArray("fees");
        int totalElements = jsonFees.length();
        for (int i = 0; i < totalElements; i++) {
            JSONObject fee = jsonFees.getJSONObject(i);
            String book = Helpers.getString(fee, "book");
            Fee currentFee = new Fee(book, Helpers.getBD(fee, "fee_decimal"),
                    Helpers.getBD(fee, "fee_percent"));
            mFees.put(book, currentFee);
        }
    }

    public HashMap<String, Fee> getFees() {
        return mFees;
    }

    public void setmFees(HashMap<String, Fee> fees) {
        this.mFees = fees;
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
