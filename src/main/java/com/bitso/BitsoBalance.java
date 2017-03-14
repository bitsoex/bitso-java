package com.bitso;

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exceptions.BitsoExceptionJSONPayload;
import com.bitso.helpers.Helpers;

public class BitsoBalance {
    protected BigDecimal mxnTotal;
    protected BigDecimal ethTotal;
    protected BigDecimal btcTotal;
    protected BigDecimal mxnLocked;
    protected BigDecimal ethLocked;
    protected BigDecimal btcLocked;
    protected BigDecimal mxnAvailable;
    protected BigDecimal ethAvailable;
    protected BigDecimal btcAvailable;

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

	public BigDecimal getMxnTotal() {
		return mxnTotal;
	}

	public void setMxnTotal(BigDecimal mxnTotal) {
		this.mxnTotal = mxnTotal;
	}

	public BigDecimal getEthTotal() {
		return ethTotal;
	}

	public void setEthTotal(BigDecimal ethTotal) {
		this.ethTotal = ethTotal;
	}

	public BigDecimal getBtcTotal() {
		return btcTotal;
	}

	public void setBtcTotal(BigDecimal btcTotal) {
		this.btcTotal = btcTotal;
	}

	public BigDecimal getMxnLocked() {
		return mxnLocked;
	}

	public void setMxnLocked(BigDecimal mxnLocked) {
		this.mxnLocked = mxnLocked;
	}

	public BigDecimal getEthLocked() {
		return ethLocked;
	}

	public void setEthLocked(BigDecimal ethLocked) {
		this.ethLocked = ethLocked;
	}

	public BigDecimal getBtcLocked() {
		return btcLocked;
	}

	public void setBtcLocked(BigDecimal btcLocked) {
		this.btcLocked = btcLocked;
	}

	public BigDecimal getMxnAvailable() {
		return mxnAvailable;
	}

	public void setMxnAvailable(BigDecimal mxnAvailable) {
		this.mxnAvailable = mxnAvailable;
	}

	public BigDecimal getEthAvailable() {
		return ethAvailable;
	}

	public void setEthAvailable(BigDecimal ethAvailable) {
		this.ethAvailable = ethAvailable;
	}

	public BigDecimal getBtcAvailable() {
		return btcAvailable;
	}

	public void setBtcAvailable(BigDecimal btcAvailable) {
		this.btcAvailable = btcAvailable;
	}
}
