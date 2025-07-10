package com.bitso.websockets;

/** Defines the channels that are available in the public websockets.
 */
public enum BitsoChannels {
    TRADES("trades"),
    DIFF_ORDERS("diff-orders"),
    ORDERS("orders"),
    PROCESSED_ORDERS("processed-orders"),
    KA("ka");

    private final String id;

    BitsoChannels(String id) {
        this.id = id;
    }

    public String toString() {
        return this.id;
    }

    public static BitsoChannels getBitsoChannel(String data) {
        switch (data) {
            case "trades":
               return BitsoChannels.TRADES;
            case "diff-orders":
                return DIFF_ORDERS;
            case "orders":
                return ORDERS;
            case "ka":
                return KA;
            case "processed-orders":
                return PROCESSED_ORDERS;
        }
        return null;
    }

}
