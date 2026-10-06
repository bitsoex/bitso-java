package com.bitso;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;

@Slf4j
public class BitsoServerTest extends BitsoTest {
    @BeforeEach
    public void setUp() throws Exception {
        String secret = System.getenv("BITSO_DEV_PRIVATE");
        String key = System.getenv("BITSO_DEV_PUBLIC_KEY");

        // If BITSO_DEV_PRIVATE and BITSO_DEV_PUBLIC_KEY
        // environment variables are set, tests will be executed
        // normally, otherwise, they will be ignored.
        Assumptions.assumeTrue(secret != null && key != null, "API key and secret not present");

        mBitso = new Bitso(key, secret, Target.development);
    }

    @AfterEach
    public void tearDown() {
        BitsoTest.throttlePrivate();
    }

    @Override
    public void testTrading() {
        System.out.println("This test is overridden in BitsoServerTest");
    }

    @Override
    public void testOrderTrades() {
        System.out.println("This test is overridden in BitsoServerTest");
    }
}
