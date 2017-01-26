package com.bitso;

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exceptions.BitsoExceptionJSONPayload;
import com.bitso.exceptions.BitsoExceptionNotExpectedCurrency;
import com.bitso.helpers.Helpers;

public class BitsoBalance {
    public BigDecimal mxnTotal;
    public BigDecimal ethTotal;
    public BigDecimal btcTotal;
    public BigDecimal mxnLocked;
    public BigDecimal ethLocked;
    public BigDecimal btcLocked;
    public BigDecimal mxnAvailable;
    public BigDecimal ethAvailable;
    public BigDecimal btcAvailable;

    public BitsoBalance(JSONObject o) {
        String currency = "";
        if(o.has("payload")){
            JSONObject payload = o.getJSONObject("payload");
            JSONArray jsonBalances = payload.getJSONArray("balances");
            int totalElements = jsonBalances.length();
            for(int i=0; i<totalElements; i++){
                JSONObject balance = jsonBalances.getJSONObject(i);
                currency = Helpers.getString(balance, "currency");
                switch (currency) {
                    case "mxn":
                        mxnTotal = Helpers.getBD(balance, "total");
                        mxnLocked = Helpers.getBD(balance, "locked");;
                        mxnAvailable = Helpers.getBD(balance, "available");
                        break;
                    case "btc":
                        btcTotal = Helpers.getBD(balance, "total");
                        btcLocked = Helpers.getBD(balance, "locked");;
                        btcAvailable = Helpers.getBD(balance, "available");
                        break;
                    case "eth":
                        ethTotal = Helpers.getBD(balance, "total");
                        ethLocked = Helpers.getBD(balance, "locked");;
                        ethAvailable = Helpers.getBD(balance, "available");
                        break;
                    default:
                        System.out.println(currency +
                                " is not an expected currency");
                }
            }
        }else{
            throw new BitsoExceptionJSONPayload(o.toString() +
                    "does not contains payload key");
        }
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}
