package com.bitso;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.mockito.runners.MockitoJUnitRunner;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.mockito.MockitoAnnotations;

@RunWith(MockitoJUnitRunner.class)
public class BitsoTest {
	@InjectMocks
	Bitso bitso = new Bitso("", "", "");

	@Mock
	BitsoLedgerEntry ledger;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ledger = mock(BitsoLedgerEntry.class);
	}

	@Test
	public void testGetLedger() {
		setUp();
		Assert.assertEquals(bitso.getLedger().getEntryId(), "");
		Assert.assertEquals(bitso.getLedger().getOperation(), "funding");
		Assert.assertEquals(bitso.getLedger().getBalanceUpdates().get(0).getAmount(), new BigDecimal(10));
		Assert.assertEquals(bitso.getLedger().getBalanceUpdates().get(0).getCurrency(), "btc");
	}

}
