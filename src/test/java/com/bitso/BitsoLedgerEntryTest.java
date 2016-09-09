package com.bitso;

import static org.junit.Assert.fail;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class BitsoLedgerEntryTest {
	@InjectMocks
	static BitsoLedgerEntry ledger;

	@Mock
	JSONObject json;

	@Before
	public void setUp() {
		ledger = new BitsoLedgerEntry(json);
	}

	@Test
	public static boolean testGetLedgerEntry() {
		if (ledger == null) {
			fail("LedgerEntry shouldn´t be null.");
			return false;
		}
		return true;
	}

	public static void main(String[] args){
		System.out.println(BitsoLedgerEntryTest.testGetLedgerEntry() ? "pass" : "fail");
	}
}
