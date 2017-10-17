package com.bitso.websockets;

public enum BitsoChannels {
    TRADES("trades"), DIFF_ORDERS("diff-orders"), ORDERS("orders"), KA("ka");

    private final String id;

    private BitsoChannels(String id) {
        this.id = id;
    }

    public String toString() {
        return this.id;
    }

    public static BitsoChannels getBitsoChannel(String data) {
        BitsoChannels channel = null;
        if (data.equals("trades")) channel = BitsoChannels.TRADES;
        if (data.equals("diff-orders")) channel = DIFF_ORDERS;
        if (data.equals("orders")) channel = ORDERS;
        if (data.equals("ka")) channel = KA;
        return channel;
    }

}
