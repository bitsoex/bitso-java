package com.bitso;

import static org.junit.Assert.assertEquals;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class BitsoServerTest extends BitsoTest {
    @Before
    public void setUp() throws Exception {
        String secret = System.getenv("bitso_dev_private");
        String key = System.getenv("bitso_dev_public_key");
        mBitso = new Bitso(key, secret, 0, true, false);
    }

    @Test
    public void testUserLedgers() {
        String[] operations = { "trades", "fees", "fundings", "withdrawals" };
        // Global ledger request
        BitsoOperation[] fullLedger = mBitso.getUserLedger(null);
        for (BitsoOperation bitsoOperation : fullLedger) {
            assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
        }

        // Specific operation type request
        for (String operationType : operations) {
            BitsoOperation[] specificLedger = mBitso.getUserLedger(operationType);
            for (BitsoOperation bitsoOperation : specificLedger) {
                assertEquals(true, nullCheck(bitsoOperation, BitsoOperation.class));
            }
        }
    }

    @Test
    public void testUserWithdrawals() {
        // Testing withdrawal ids
        String[] wids = { "65532d428d4c1b2642833b9e78c1b9fd", "d5764355792aff733f31ee7bfc38a832",
                "e7dba07657459c194514d3088d117e18" };
        BitsoWithdrawal[] fullWithdraws = mBitso.getUserWithdrawals();
        for (BitsoWithdrawal bitsoWithdrawal : fullWithdraws) {
            assertEquals(true, nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }

        // Specific withdrawal id
        BitsoWithdrawal[] specificWithdraw = mBitso.getUserWithdrawals(wids[0]);
        for (BitsoWithdrawal bitsoWithdrawal : specificWithdraw) {
            assertEquals(true, nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }

        // Multiple withdrawal ids
        BitsoWithdrawal[] multipleWithdraws = mBitso.getUserWithdrawals(wids);
        for (BitsoWithdrawal bitsoWithdrawal : multipleWithdraws) {
            assertEquals(true, nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }
    }

    @Test
    public void testUserFundings() {
        // Testing funding ids
        String[] fids = { "2ab6b5cccf2be8d1fb8382234203f8e1", "e1b96fe7d22cfbfdb83df51a68eca9b0",
                "1ae6d8af23111799698a4821b8d1d156", "7a68bac79c89af4bc24dd153f535ad54" };
        BitsoFunding[] fullFundings = mBitso.getUserFundings();
        for (BitsoFunding bitsoFunding : fullFundings) {
            assertEquals(true, nullCheck(bitsoFunding, BitsoFunding.class));
        }

        // Specific funding id
        BitsoFunding[] specificFunding = mBitso.getUserFundings(fids[0]);
        for (BitsoFunding bitsoFunding : specificFunding) {
            assertEquals(true, nullCheck(bitsoFunding, BitsoFunding.class));
        }

        // Multiple funding ids
        BitsoFunding[] multipleFundings = mBitso.getUserFundings(fids);
        for (BitsoFunding bitsoFunding : multipleFundings) {
            assertEquals(true, nullCheck(bitsoFunding, BitsoFunding.class));
        }
    }

    @Test
    public void testUserTrades() {
        // Testing trades ids
        String[] tids = { "1431", "1430", "1429", "1428" };
        BitsoTrade[] fullTrades = mBitso.getUserTrades();
        for (BitsoTrade bitsoTrade : fullTrades) {
            assertEquals(true, nullCheck(bitsoTrade, BitsoTrade.class));
        }

        // Specific trade id
        BitsoTrade[] specificTrade = mBitso.getUserTrades(tids[0]);
        for (BitsoTrade bitsoTrade : specificTrade) {
            assertEquals(true, nullCheck(bitsoTrade, BitsoTrade.class));
        }

        // Multiple trade ids
        BitsoTrade[] multipleTrades = mBitso.getUserTrades(tids);
        for (BitsoTrade bitsoTrade : multipleTrades) {
            assertEquals(true, nullCheck(bitsoTrade, BitsoTrade.class));
        }
    }

    @Test
    public void testLookupOrders() {
        String[] values = { "kRrcjsp5n9og98qa", "V4RVg7OJ1jl5O5Om", "4fVvpQrR59M26ojl", "Rhvak2cOOX552s69",
                "n8JvMOl4iO8s22r2" };

        BitsoOrder[] specificOrder = mBitso.lookupOrders(values[0]);
        for (BitsoOrder bitsoOrder : specificOrder) {
            assertEquals(true, nullCheck(bitsoOrder, BitsoOrder.class));
        }

        BitsoOrder[] multipleOrders = mBitso.lookupOrders(values);
        for (BitsoOrder bitsoOrder : multipleOrders) {
            assertEquals(true, nullCheck(bitsoOrder, BitsoOrder.class));
        }
    }

    @Test
    public void testCancelUserOrder() {
        String[] orders = { "pj251R8m6im5lO82", "4nQl95irVlfQRkXp", "vdfVrXVQJ0iJdV6h" };

        String[] cancelParticularOrder = mBitso.cancelOrder(orders[0]);
        assertEquals(true, (cancelParticularOrder != null));

        String[] cancelMultipleOrders = mBitso.cancelOrder(orders);
        assertEquals(true, (cancelMultipleOrders != null));

        String[] cancelAllOrders = mBitso.cancelOrder("all");
        assertEquals(true, (cancelAllOrders != null));
    }

    @Test
    public void testFundingDestination() {
        Map<String, String> btcFundingDestination = mBitso.fundingDestination("btc");
        assertEquals(true, (btcFundingDestination != null));
        assertEquals(true, (btcFundingDestination.containsKey("accountIdentifierName")
                && btcFundingDestination.containsKey("accountIdentifier")));

        Map<String, String> ethFundingDestination = mBitso.fundingDestination("eth");
        assertEquals(true, (ethFundingDestination != null));
        assertEquals(true, (ethFundingDestination.containsKey("accountIdentifierName")
                && ethFundingDestination.containsKey("accountIdentifier")));

        Map<String, String> mxnFundingDestination = mBitso.fundingDestination("mxn");
        assertEquals(true, (mxnFundingDestination != null));
        assertEquals(true, (mxnFundingDestination.containsKey("accountIdentifierName")
                && mxnFundingDestination.containsKey("accountIdentifier")));
    }

    @Test
    public void testSPEIWithdrawal() {
        BitsoWithdrawal speiWithdrawal = mBitso.speiWithdrawal(new BigDecimal("50"), "name", "surname",
                "044180001059660729", "testing reference", "5706");
        assertEquals(true, nullCheck(speiWithdrawal, BitsoWithdrawal.class));
    }
}
