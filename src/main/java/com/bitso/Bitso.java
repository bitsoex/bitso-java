package com.bitso;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bitso.BitsoUserTransactions;
import com.bitso.BitsoUserTransactions.SORT_ORDER;
import com.bitso.exchange.BookOrder;
import com.bitso.exchange.OrderBook;
import com.bitso.exchange.BookOrder.STATUS;
import com.bitso.exchange.BookOrder.TYPE;
import com.bitso.helpers.Helpers;
import com.bitso.http.BlockingHttpClient;

public class Bitso {

    private static final String BITSO_BASE_URL = "https://api.bitso.com/v2/";
    private static final long THROTTLE_MS = 1000;

    private String key;
    private String secret;
    private String clientId;
    private int retries;
    private boolean log;

    private BlockingHttpClient client = new BlockingHttpClient(false, THROTTLE_MS);

    public Bitso(String key, String secret, String clientId) {
        this(key, secret, clientId, 0);
    }

    public Bitso(String key, String secret, String clientId, int retries) {
        this(key, secret, clientId, retries, true);
    }

    public Bitso(String key, String secret, String clientId, int retries, boolean log) {
        this.key = key;
        this.secret = secret;
        this.clientId = clientId;
        this.retries = retries;
        this.log = log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    private void logError(String error) {
        if (log) {
            System.err.println(error);
        }
    }

    private void log(String msg) {
        if (log) {
            System.out.println(msg);
        }
    }

    // Public Functions
    public BitsoTicker getTicker() {
        String json = sendGet(BITSO_BASE_URL + "ticker");
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Unable to get Bitso Ticker: " + json);
            return null;
        }
        return new BitsoTicker(o);
    }

    public OrderBook getOrderBook() {
        String json = sendGet(BITSO_BASE_URL + "order_book");
        JSONObject o = Helpers.parseJson(json);
        if (o == null) {
            logError("Unable to get Bitso Order Book");
            return null;
        }
        return new BitsoOrderBook(o);
    }

    // Private Functions
    public BitsoBalance getBalance() {
        String json = sendBitsoPost(BITSO_BASE_URL + "balance");
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting Bitso Balance: " + json);
            return null;
        }
        return new BitsoBalance(o);
    }

    public BitsoUserTransactions getUserTransactions() {
        return getUserTransactions(0, 0, null);
    }

    public BitsoUserTransactions getUserTransactions(int offset, int limit, SORT_ORDER sortOrder) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        if (offset > 0) body.put("offset", offset);
        if (limit > 0) body.put("limit", limit);
        if (sortOrder != null) body.put("sort", sortOrder.getOrder());
        String json = sendBitsoPost(BITSO_BASE_URL + "user_transactions", body);
        JSONArray a = Helpers.parseJsonArray(json);
        if (a == null) {
            logError("Unable to get User Transactions: " + json);
            return null;
        }
        return new BitsoUserTransactions(a);
    }

    public BitsoOpenOrders getOpenOrders() throws Exception {
        return new BitsoOpenOrders(sendBitsoPost(BITSO_BASE_URL + "open_orders"));
    }

    @Deprecated
    public BitsoLookupOrders getLookupOrders(String orderId) {
        return lookupOrder(orderId);
    }

    public BitsoLookupOrders lookupOrder(String orderId) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("id", orderId);
        String json = sendBitsoPost(BITSO_BASE_URL + "lookup_order", body);
        JSONArray a = Helpers.parseJsonArray(json);
        if (a == null) {
            logError("Unable to get Lookup Order" + json);
            return null;
        }
        return new BitsoLookupOrders(a);
    }

    public boolean cancelOrder(String orderId) throws Exception {
        log("Attempting to cancel order: " + orderId);
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("id", orderId);
        String ret = sendBitsoPost(BITSO_BASE_URL + "cancel_order", body);
        if (ret.equals("\"true\"")) {
            log("Cancelled Order: " + orderId);
            return true;
        }
        logError("Unable to cancel order: " + orderId);
        logError(ret);
        return false;
    }

    public BookOrder placeBuyLimitOrder(BigDecimal price, BigDecimal amount) throws Exception {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("amount", amount.toPlainString());
        body.put("price", price.toPlainString());
        log("Placing the following buy order: " + body);
        String json = sendBitsoPost(BITSO_BASE_URL + "buy", body);
        return processBookOrderJSON(json);
    }

    public BigDecimal placeBuyMarketOrder(BigDecimal mxnAmountToSpend) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("amount", mxnAmountToSpend.toPlainString());
        log("Placing the following buy maket order: " + body);
        String json = sendBitsoPost(BITSO_BASE_URL + "buy", body);
        JSONObject o;
        try {
            o = new JSONObject(json);
        } catch (JSONException e) {
            logError("Unable to parse json: " + json);
            e.printStackTrace();
            return null;
        }
        if (o.has("error")) {
            logError("Unable to place Buy Market Order: " + json);
            return null;
        }

        BookOrder order = findMatchingOrders(o);
        if (order != null) {
            return order.major;
        }
        return null;
    }

    public BookOrder placeSellLimitOrder(BigDecimal price, BigDecimal amount) throws Exception {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("amount", amount.toPlainString());
        body.put("price", price.toPlainString());
        log("Placing the following sell order: " + body);
        String json = sendBitsoPost(BITSO_BASE_URL + "sell", body);
        return processBookOrderJSON(json);
    }

    public BigDecimal placeSellMarketOrder(BigDecimal btcAmountToSpend) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("amount", btcAmountToSpend.toPlainString());
        log("Placing the following sell maket order: " + body);
        String json = sendBitsoPost(BITSO_BASE_URL + "sell", body);
        JSONObject o;
        try {
            o = new JSONObject(json);
        } catch (JSONException e) {
            logError("Unable to parse json: " + json);
            e.printStackTrace();
            return null;
        }
        if (o.has("error")) {
            logError("Unable to place Sell Market Order: " + json);
            return null;
        }
        BookOrder order = findMatchingOrders(o);
        if (order != null) {
            return order.minor;
        }
        return null;
    }

    public String getDepositAddress() throws Exception {
        return quoteEliminator(sendBitsoPost(BITSO_BASE_URL + "bitcoin_deposit_address"));
    }

    public boolean withdrawBTC(String address, BigDecimal amount) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("amount", amount.toPlainString());
        body.put("address", address);
        log("Executing the following BTC withdrawal: " + body);
        String ret = sendBitsoPost(BITSO_BASE_URL + "bitcoin_withdrawal", body);
        if (ret != null && ret.equals("\"ok\"")) {
            log("BTC withdrawal executed");
            return true;
        }
        logError("Unable to execute BTC withdrawal");
        logError(ret);
        return false;
    }

    public boolean withdrawMXN(BigDecimal amount, String recipientGivenName, String recipientFamilyName,
            String clabe, String notesRef, String numericRef) throws Exception {
        if (amount.scale() > 2) {
            logError("MXN withdrawal has incorrect scale " + amount);
            return false;
        }
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("amount", amount.toPlainString());
        body.put("recipient_given_names", recipientGivenName);
        body.put("recipient_family_names", recipientFamilyName);
        body.put("clabe", clabe);
        body.put("notes_ref", notesRef);
        body.put("numeric_ref", numericRef);
        log("Executing the following withdrawal: " + body);
        String ret = sendBitsoPost(BITSO_BASE_URL + "spei_withdrawal", body);
        if (ret.equals("\"ok\"")) {
            log("Withdrawal executed");
            return true;
        }
        logError("Unable to execute MXN withdrawal");
        logError(ret);
        return false;
    }

    public BitsoTransferQuote requestQuote(BigDecimal btcAmount, BigDecimal amount, String currency,
            boolean full) throws Exception {
        if (btcAmount != null && amount != null) {
            logError("btcAmount and amount are mutually exclusive!");
            return null;
        }
        HashMap<String, Object> body = new HashMap<String, Object>();
        if (btcAmount != null) body.put("btc_amount", btcAmount.toPlainString());
        if (amount != null) body.put("amount", amount.toPlainString());
        body.put("currency", currency);
        body.put("full", full);
        String ret = sendBitsoPost(BITSO_BASE_URL + "transfer_quote", body);
        JSONObject o = Helpers.parseJson(ret);
        if (o == null || o.has("error")) {
            logError("Unable to request quote: " + ret);
            return null;
        }
        BitsoTransferQuote btq = null;
        try {
            btq = new BitsoTransferQuote(o);
        } catch (JSONException e) {
            e.printStackTrace();
            logError(ret);
        }
        return btq;
    }

    public BitsoTransfer createTransfer(BigDecimal btcAmount, BigDecimal amount, String currency,
            BigDecimal rate, String paymentOutlet, HashMap<String, Object> requiredFields) throws Exception {
        if (btcAmount != null && amount != null) {
            logError("btcAmount and amount are mutually exclusive!");
            return null;
        }
        HashMap<String, Object> body = new HashMap<String, Object>();
        if (btcAmount != null) body.put("btc_amount", btcAmount.toPlainString());
        if (amount != null) body.put("amount", amount.toPlainString());
        body.put("currency", currency);
        body.put("rate", rate.toPlainString());
        body.put("payment_outlet", paymentOutlet);
        if (requiredFields != null) {
            for (Entry<String, Object> e : requiredFields.entrySet()) {
                body.put(e.getKey(), e.getValue());
            }
        }
        String ret = sendBitsoPost(BITSO_BASE_URL + "transfer_create", body);
        JSONObject o = Helpers.parseJson(ret);
        if (o == null || o.has("error")) {
            logError("Unable to request quote: " + ret);
            return null;
        }
        BitsoTransfer bt = null;
        try {
            bt = new BitsoTransfer(o);
        } catch (JSONException e) {
            e.printStackTrace();
            logError(ret);
        }
        return bt;
    }

    public BitsoTransfer getTransferStatus(String transferId) {
        String ret = sendGet(BITSO_BASE_URL + "transfer/" + transferId);
        JSONObject o = Helpers.parseJson(ret);
        if (o == null || o.has("error")) {
            logError("Unable to get transfer status: " + ret);
            return null;
        }
        return new BitsoTransfer(o);
    }

    private String quoteEliminator(String input) {
        if (input == null) {
            logError("input to quoteEliminator cannot be null");
            return null;
        }
        int length = input.length();
        if (input.charAt(0) != '"' || input.charAt(length - 1) != '"') {
            logError("invalid input to quoteEliminator: " + input);
            return null;
        }
        return input.substring(1, length - 1);
    }

    private static String signRequest(String secret, String inputStr) {
        String signature = "";
        byte[] secretBytes = secret.getBytes();
        SecretKeySpec localMac = new SecretKeySpec(secretBytes, "HmacSHA256");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(localMac);
            // Compute the hmac on input data bytes
            byte[] arrayOfByte = mac.doFinal(inputStr.getBytes());
            BigInteger localBigInteger = new BigInteger(1, arrayOfByte);
            signature = String.format("%0" + (arrayOfByte.length << 1) + "x",
                    new Object[] { localBigInteger });
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return signature;
    }

    private String sendBitsoPost(String url, HashMap<String, Object> bodyExtras) {
        long nonce = System.currentTimeMillis();
        String message = nonce + key + clientId;
        String signature = signRequest(secret, message);

        JSONObject json = new JSONObject();
        json.put("key", key);
        json.put("nonce", nonce);
        json.put("signature", signature);

        if (bodyExtras != null) {
            for (Entry<String, Object> e : bodyExtras.entrySet()) {
                json.put(e.getKey(), e.getValue());
            }
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        try {
            return client.sendPost(url, json.toString(), headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String sendBitsoPost(String url) {
        return sendBitsoPost(url, null);
    }

    private String sendGet(String url) {
        int counter = 0;
        String ret = null;
        while (counter++ <= retries) {
            try {
                ret = client.get(url);
                return ret;
            } catch (Exception e) {
                logError("Exception when sending get to: " + url);
                logError(e.getMessage());
                if (counter == retries) {
                    logError("Exceeded number of retries to get: " + url);
                    e.printStackTrace();
                    return null;
                }
            }
            try {
                Thread.sleep(2000 * counter);
            } catch (InterruptedException e) {
                logError("unable to sleep");
                e.printStackTrace();
            }
        }
        return null;
    }

    public BookOrder findMatchingOrders(JSONObject o) {
        BookOrder toRet = null;
        if (o.has("id")) {
            String newOrderId = o.getString("id");
            int offset = 0;
            int limit = 10;
            outer: while (true) {
                BitsoUserTransactions but = getUserTransactions(offset, limit, null);
                if (but == null) {
                    return null;
                }
                // We gone through the entire history and didn't find anything...
                if (but.list.size() == 0) {
                    return null;
                }

                for (int i = 0; i < but.list.size(); i++) {
                    BookOrder order = but.list.get(i);
                    if (order.id.equals(newOrderId)) {
                        if (toRet == null) {
                            toRet = order;
                        } else {
                            toRet.minor = toRet.minor.add(order.minor);
                        }
                    } else if (toRet != null && i < but.list.size()) {
                        break outer;
                    }
                }
                limit *= 2;
                offset += limit;
            }
        }
        if (toRet == null) {
            logError("Unable to find order in recent transactions");
            Helpers.printStackTrace();
        }

        return toRet;
    }

    public static BookOrder processBookOrderJSON(String json) {
        JSONObject o = null;
        try {
            o = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println(json);
            return null;
        }
        if (o.has("error")) {
            String error = o.toString(4);
            System.err.println(error);
            return null;
        }
        BigDecimal price = null, major = null;
        if (o.has("price")) {
            price = new BigDecimal(o.getString("price"));
        }
        if (o.has("amount")) {
            major = new BigDecimal(o.getString("amount"));
        }
        if (price == null || major == null) {
            return null;
        }
        BookOrder order = new BookOrder(price, major);
        if (o.has("id")) {
            order.id = o.getString("id");
        }
        if (o.has("book")) {
            order.book = o.getString("book");
        }
        if (o.has("type")) {
            order.type = TYPE.values()[o.getInt("type")];
        }
        if (o.has("status")) {
            int statusInt = o.getInt("status");
            // Bitso passes CANCELLED as -1. We put CANCELLED in the STATUS enum in the 4th position
            if (statusInt == -1) {
                statusInt = 3;
            }
            order.status = STATUS.values()[statusInt];
        }
        if (o.has("created")) {
            order.created = o.getString("created");
        }
        if (o.has("updated")) {
            order.updated = o.getString("updated");
        }
        if (o.has("datetime")) {
            order.dateTime = o.getString("datetime");
        }
        return order;
    }
}
