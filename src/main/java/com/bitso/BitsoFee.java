package com.bitso;

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exceptions.BitsoExceptionJSONPayload;
import com.bitso.helpers.Helpers;

public class BitsoFee {
    public BigDecimal mxnBtcFeeDecimal;
    public BigDecimal mxnBtcFeePercent;
    public BigDecimal mxnEthFeeDecimal;
    public BigDecimal mxnEthFeePercent;

    public BitsoFee(JSONObject o) {
        String book = "";
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            JSONArray jsonFees = payload.getJSONArray("fees");
            int totalElements = jsonFees.length();
            for (int i = 0; i < totalElements; i++) {
                JSONObject fee = jsonFees.getJSONObject(i);
                book = Helpers.getString(fee, "book");
                switch (book) {
                    case "btc_mxn":
                        mxnBtcFeeDecimal = Helpers.getBD(fee, "fee_decimal");
                        mxnBtcFeePercent = Helpers.getBD(fee, "fee_percent");
                        break;
                    case "eth_mxn":
                        mxnEthFeeDecimal = Helpers.getBD(fee, "fee_decimal");
                        mxnEthFeePercent = Helpers.getBD(fee, "fee_percent");
                        break;
                    default:
                        System.out.println(book + " is not an expected book");
                }
            }
        } else {
            throw new BitsoExceptionJSONPayload(o.toString() + "does not contains payload key");
        }
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
