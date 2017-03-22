package com.bitso;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import com.bitso.helpers.Helpers;

public class BitsoOperation {
    protected String entryId;
    protected String operationDescription;
    protected Date operationDate;
    protected BalanceUpdate[] afterOperationBalances;
    protected JSONObject details;

    public BitsoOperation(JSONObject o) {
        entryId = Helpers.getString(o, "eid");
        operationDescription = Helpers.getString(o, "operation");
        operationDate = Helpers.getZonedDatetime(o, "created_at");
        afterOperationBalances = getOperationBalances(o.getJSONArray("balance_updates"));
        details = o.getJSONObject("details");
    }

    private BalanceUpdate[] getOperationBalances(JSONArray array){
        int totalBalances =  array.length();
        BalanceUpdate[] balances =  new BalanceUpdate[totalBalances];
        for(int i=0; i<totalBalances; i++){
            balances[i] =  new BalanceUpdate(array.getJSONObject(i));
        }
        return balances;
    }

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }

    public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getOperationDescription() {
		return operationDescription;
	}

	public void setOperationDescription(String operationDescription) {
		this.operationDescription = operationDescription;
	}

	public Date getOperationDate() {
		return operationDate;
	}

	public void setOperationDate(Date operationDate) {
		this.operationDate = operationDate;
	}

	public BalanceUpdate[] getAfterOperationBalances() {
		return afterOperationBalances;
	}

	public void setAfterOperationBalances(BalanceUpdate[] afterOperationBalances) {
		this.afterOperationBalances = afterOperationBalances;
	}

	public JSONObject getDetails() {
		return details;
	}

	public void setDetails(JSONObject details) {
		this.details = details;
	}

	public class BalanceUpdate{
        String currency;
        BigDecimal amount;

        public BalanceUpdate(JSONObject o) {
            this.currency = Helpers.getString(o, "currency");
            this.amount = Helpers.getBD(o, "amount");
        }

		public String getCurrency() {
			return currency;
		}

		public void setCurrency(String currency) {
			this.currency = currency;
		}

		public BigDecimal getAmount() {
			return amount;
		}

		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}
    }
}
