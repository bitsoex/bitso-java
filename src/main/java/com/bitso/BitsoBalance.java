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

    public BigDecimal ethBalance; // MXN balance
    public BigDecimal ethAvailable; // MXN available for trading
    public BigDecimal ethReserved; // MXN reserved in open order

    /**
     * Customer trading fee, in percentage
     *
     * @deprecated use {@link #feePercent} or {@link #feeDecimal} instead.
     */
    @Deprecated
    public BigDecimal fee;
    public BigDecimal feePercent; // customer trading fee, in percentage
    public BigDecimal feeDecimal; // customer trading fee, in decimal

    public BitsoBalance(JSONObject obj) {
        btcBalance = new BigDecimal(obj.getString("btc_balance"));
        btcAvailable = new BigDecimal(obj.getString("btc_available"));
        btcReserved = new BigDecimal(obj.getString("btc_reserved"));

        mxnBalance = new BigDecimal(obj.getString("mxn_balance"));
        mxnAvailable = new BigDecimal(obj.getString("mxn_available"));
        mxnReserved = new BigDecimal(obj.getString("mxn_reserved"));

        ethBalance = new BigDecimal(obj.getString("eth_balance"));
        ethAvailable = new BigDecimal(obj.getString("eth_available"));
        ethReserved = new BigDecimal(obj.getString("eth_reserved"));

        feePercent = new BigDecimal(obj.getString("fee"));
        feeDecimal = feePercent.divide(new BigDecimal("100"), 8, BigDecimal.ROUND_UP);
        fee = feePercent;
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
        sb.append("\n====ETH====\nAvailable: ");
        sb.append(ethAvailable.toPlainString());
        sb.append("\n Reserved: ");
        sb.append(ethReserved.toPlainString());
        sb.append("\n  Balance: ");
        sb.append(ethBalance.toPlainString());
        sb.append("\n===OTHER===\nFee: ");
        sb.append(fee.toPlainString());
        sb.append("\nFee (Decimal): ");
        sb.append(feeDecimal.toPlainString());
        sb.append("\nFee (Percent): ");
        sb.append(feePercent.toPlainString());
        return sb.toString();
    }
}
