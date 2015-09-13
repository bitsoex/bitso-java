package com.bitso;

import java.math.BigDecimal;

import org.json.JSONObject;

public class BitsoBalance {

    public BigDecimal btcBalance; // BTC balance
    public BigDecimal btcAvailable; // BTC available for trading
    public BigDecimal btcReserved; // BTC reserved in open orders

    public BigDecimal mxnBalance; // MXN balance
    public BigDecimal mxnAvailable; // MXN available for trading
    public BigDecimal mxnReserved; // MXN reserved in open order

    public BigDecimal fee; // customer trading fee

    public BitsoBalance(JSONObject obj) {
        btcBalance = new BigDecimal(obj.getString("btc_balance"));
        btcAvailable = new BigDecimal(obj.getString("btc_available"));
        btcReserved = new BigDecimal(obj.getString("btc_reserved"));

        mxnBalance = new BigDecimal(obj.getString("mxn_balance"));
        mxnAvailable = new BigDecimal(obj.getString("mxn_available"));
        mxnReserved = new BigDecimal(obj.getString("mxn_reserved"));

        fee = new BigDecimal(obj.getString("fee"));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("====BTC====\nAvailable: ");
        sb.append(btcAvailable.toPlainString());
        sb.append("\n Reserved: ");
        sb.append(btcReserved.toPlainString());
        sb.append("\n  Balance: ");
        sb.append(btcBalance.toPlainString());
        sb.append("\n====MXN====\nAvailable: ");
        sb.append(mxnAvailable.toPlainString());
        sb.append("\n Reserved: ");
        sb.append(mxnReserved.toPlainString());
        sb.append("\n  Balance: ");
        sb.append(mxnBalance.toPlainString());
        sb.append("\n===OTHER===\nFee: ");
        sb.append(fee.toPlainString());
        return sb.toString();
    }
}
