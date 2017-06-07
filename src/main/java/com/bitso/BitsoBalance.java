package com.bitso;

import java.math.BigDecimal;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exchange.Ticker;
import com.bitso.helpers.Helpers;

public class BitsoBalance {
    private HashMap<String, Balance> mBalances;

    public BitsoBalance(JSONObject o) {
        mBalances = new HashMap<>();
        JSONArray jsonBalances = o.getJSONArray("balances");
        int totalElements = jsonBalances.length();
        for (int i = 0; i < totalElements; i++) {
            JSONObject balance = jsonBalances.getJSONObject(i);
            String currency = Helpers.getString(balance, "currency");
            Balance currentBalance = new Balance(currency, Helpers.getBD(balance, "total"),
                    Helpers.getBD(balance, "locked"), Helpers.getBD(balance, "available"));
            mBalances.put(currency, currentBalance);
        }
    }

    public HashMap<String, Balance> getBalances() {
        return mBalances;
    }

    public void setBalances(HashMap<String, Balance> mBalances) {
        this.mBalances = mBalances;
    }

    public String toString() {
        return Helpers.fieldPrinter(this, BitsoBalance.class);
    }

    public class Balance {
        private String mCurrency;
        private BigDecimal mTotal;
        private BigDecimal mLocked;
        private BigDecimal mAvailable;

        public Balance(String currency, BigDecimal total, BigDecimal locked, BigDecimal available) {
            mCurrency = currency;
            mTotal = total;
            mLocked = locked;
            mAvailable = available;
        }

        public String getCurrency() {
            return mCurrency;
        }

        public void setCurrency(String mCurrency) {
            this.mCurrency = mCurrency;
        }

        public BigDecimal getTotal() {
            return mTotal;
        }

        public void setTotal(BigDecimal mTotal) {
            this.mTotal = mTotal;
        }

        public BigDecimal getLocked() {
            return mLocked;
        }

        public void setLocked(BigDecimal mLocked) {
            this.mLocked = mLocked;
        }

        public BigDecimal getAvailable() {
            return mAvailable;
        }

        public void setAvailable(BigDecimal mAvailable) {
            this.mAvailable = mAvailable;
        }

        public String toString() {
            return Helpers.fieldPrinter(this, BitsoBalance.Balance.class);
        }
    }
}
