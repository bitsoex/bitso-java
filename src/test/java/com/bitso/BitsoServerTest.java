package com.bitso;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

public class BitsoServerTest extends BitsoTest {
    @Before
    public void setUp() throws Exception {
        String secret = System.getenv("BITSO_DEV_PRIVATE");
        String key = System.getenv("BITSO_DEV_PUBLIC_KEY");

        // If BITSO_DEV_PRIVATE and BITSO_DEV_PUBLIC_KEY
        // environment variables are set, tests will be executed
        // normally, otherwise, they will be ignored.
        Assume.assumeTrue((secret != null && key != null));

        mBitso = new Bitso(key, secret, 0, true, true);
    }

    @After
    public void tearDown() {
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
