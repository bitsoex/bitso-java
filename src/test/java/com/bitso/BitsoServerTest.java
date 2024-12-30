package com.bitso;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;

public class BitsoServerTest extends BitsoTest {
    @BeforeEach
    public void setUp() throws Exception {
        String secret = System.getenv("BITSO_DEV_PRIVATE");
        String key = System.getenv("BITSO_DEV_PUBLIC_KEY");

        // If BITSO_DEV_PRIVATE and BITSO_DEV_PUBLIC_KEY
        // environment variables are set, tests will be executed
        // normally, otherwise, they will be ignored.
        Assumptions.assumeTrue((secret != null && key != null));

        mBitso = new Bitso(key, secret, 0, true, true);
    }

    @AfterEach
    public void tearDown() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void testTrading() {
        System.out.println("This test is overriden in BitsoServerTest");
    }

    @Override
    public void testOrderTrades() {
        System.out.println("This test is overriden in BitsoServerTest");
    }
}
