package com.bitso.exchange;

import java.math.BigDecimal;

public class BalanceUpdate {

	private BigDecimal amount;
	private String currency;

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public BalanceUpdate() {
	}

	/**
	 * 
	 * @param amount
	 * @param currency
	 */
	public BalanceUpdate(BigDecimal amount, String currency) {
		this.amount = amount;
		this.currency = currency;
	}

	/**
	 * 
	 * @return The amount
	 */
	public BigDecimal getAmount() {
		return amount;
	}

	/**
	 * 
	 * @param amount
	 * 
	 */
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	/**
	 * 
	 * @return The currency
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * 
	 * @param currency
	 * 
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.amount != null) {
			sb.append("\n====Balance Update====\nAmount: ");
			sb.append(this.amount);

		}
		if (this.currency != null) {
			sb.append(" Currency:");
			sb.append(this.currency);
		}
		return sb.toString();
	}
}