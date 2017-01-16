package com.bitso;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.BitsoUserTransactions.SORT_ORDER;
import com.bitso.exchange.BookInfo;
import com.bitso.exchange.BookOrder;
import com.bitso.exchange.BookOrder.STATUS;
import com.bitso.exchange.BookOrder.TYPE;
import com.bitso.helpers.Helpers;
import com.bitso.http.BlockingHttpClient;

public class Bitso {
    private static final String BITSO_BASE_URL_PRODUCTION = "https://bitso.com";
    private static final String BITSO_BASE_URL_DEV = "https://dev.bitso.com";
    public static long THROTTLE_MS = 1000;

    private String key;
    private String secret;
    private String clientId;
    private int retries;
    private boolean log;
    private String baseUrl;

    private BlockingHttpClient client = new BlockingHttpClient(false, THROTTLE_MS);

    public Bitso(String key, String secret, String clientId) {
        this(key, secret, clientId, 0);
    }

    public Bitso(String key, String secret, String clientId, int retries) {
        this(key, secret, clientId, retries, true);
    }

    public Bitso(String key, String secret, String clientId, int retries, boolean log) {
        this(key, secret, clientId, retries, log, true);
    }

    public Bitso(String key, String secret, String clientId, int retries, boolean log, boolean production) {
        this.key = key;
        this.secret = secret;
        this.clientId = clientId;
        this.retries = retries;
        this.log = log;
        this.baseUrl = production ? BITSO_BASE_URL_PRODUCTION : BITSO_BASE_URL_DEV;
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
    public ArrayList<BookInfo> availableBooks() {
        String json = sendGet("/api/v3/available_books");
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Unable to get Bitso Ticker: " + json);
            return null;
        }
        ArrayList<BookInfo> books = new ArrayList<BookInfo>();
        JSONArray arr = o.getJSONArray("payload");
        for (int i = 0; i < arr.length(); i++) {
            books.add(new BookInfo(arr.getJSONObject(i)));
        }
        return books;
    }

    public BitsoTicker getTicker(BitsoBook book) {
        String json = sendGet("/api/v3/ticker?book=" + book.toString());
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Unable to get Bitso Ticker: " + json);
            return null;
        }
        return new BitsoTicker(o.getJSONObject("payload"));
    }

    public BitsoOrderBook getOrderBook(BitsoBook book) {
        String json = sendGet("/api/v3/order_book?book=" + book.toString());
        JSONObject o = Helpers.parseJson(json);
        if (o == null) {
            logError("Unable to get Bitso Order Book");
            return null;
        }
        return new BitsoOrderBook(o);
    }

    public BitsoTransactions getTransactions(BitsoBook book) {
        String json = sendGet(baseUrl + "trades?book=" + book.toString());
        JSONArray a = Helpers.parseJsonArray(json);
        if (a == null) {
            logError("Unable to get Bitso Transactions");
            return null;
        }
        return new BitsoTransactions(a);
    }

    // Private Functions
    public BitsoBalance getBalance() {
        String json = sendBitsoPost(baseUrl + "balance");
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting Bitso Balance: " + json);
            return null;
        }
        return new BitsoBalance(o);
    }

    public BitsoAccountStatus getUserAccountStatus() {
        String json = sendBitsoGet("/api/v3/account_status");
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting Bitso Account Status: " + json);
            return null;
        }
        return new BitsoAccountStatus(o);
    }

    public BitsoBalance getUserAccountBalance(){
        String json =  sendBitsoGet("/api/v3/balance");
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error getting account balance");
            return null;
        }
        return new BitsoBalance(o);
    }

    public BitsoFee getUserFees(){
        String json =  sendBitsoGet("/api/v3/fees");
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error getting user fees");
            return null;
        }
        return new BitsoFee(o);
    }

    public BitsoLedger getUserLedger(String... specificOperation){
        String request = "/api/v3/ledger";
        if(specificOperation.length == 1){
            request += "/" + specificOperation[0];
        }
        log(request);
        String json =  sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error getting user fees");
            return null;
        }
        return new BitsoLedger(o);
    }

    public BitsoWithdrawal getUserWithdrawals(String... withdrawalsIds){
        String request = "/api/v3/withdrawals";
        if(withdrawalsIds.length > 0){
            request += "/";
            for (String string : withdrawalsIds) {
                request += string + "-";
            }
            request = request.substring(0, request.length() - 1);
        }
        log(request);
        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error getting user withdrawals");
            return null;
        }
        return new BitsoWithdrawal(o);
    }

    public BitsoUserTransactions getUserTransactions() {
        return getUserTransactions(BitsoBook.BTC_MXN);
    }

    public BitsoUserTransactions getUserTransactions(BitsoBook book) {
        return getUserTransactions(0, 0, null, book);
    }

    public BitsoUserTransactions getUserTransactions(int offset, int limit, SORT_ORDER sortOrder) {
        return getUserTransactions(offset, limit, sortOrder, BitsoBook.BTC_MXN);
    }

    public BitsoUserTransactions getUserTransactions(int offset, int limit, SORT_ORDER sortOrder,
            BitsoBook book) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        if (offset > 0) body.put("offset", offset);
        if (limit > 0) body.put("limit", limit);
        if (sortOrder != null) body.put("sort", sortOrder.getOrder());
        body.put("book", book.toString());
        String json = null;// sendBitsoPost(baseUrl + "user_transactions", body);
        JSONArray a = Helpers.parseJsonArray(json);
        if (a == null) {
            logError("Unable to get User Transactions: " + json);
            return null;
        }
        return new BitsoUserTransactions(a, book);
    }

    public BitsoOpenOrders getOpenOrders() {
        return getOpenOrders(BitsoBook.BTC_MXN);
    }

    public BitsoOpenOrders getOpenOrders(BitsoBook book) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("book", book.toString());
        String json = null;// sendBitsoPost(baseUrl + "open_orders", body);
        JSONArray a = Helpers.parseJsonArray(json);
        if (a == null) {
            logError("Unable to get Open Orders: " + json);
            return null;
        }
        return new BitsoOpenOrders(a, book);
    }

    /**
     * Used to return a list of orders with the given orderId
     *
     * @deprecated use {@link #lookupOrder(String orderId)} instead.
     */
    @Deprecated
    public BitsoLookupOrders getLookupOrders(String orderId) {
        return lookupOrder(orderId);
    }

    public BitsoLookupOrders lookupOrder(String orderId) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("id", orderId);
        String json = null;// sendBitsoPost(baseUrl + "lookup_order", body);
        JSONArray a = Helpers.parseJsonArray(json);
        if (a == null) {
            logError("Unable to get Lookup Order" + json);
            return null;
        }
        return new BitsoLookupOrders(a);
    }

    public boolean cancelOrder(String orderId) {
        log("Attempting to cancel order: " + orderId);
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("id", orderId);
        String ret = null;// sendBitsoPost(baseUrl + "cancel_order", body);
        if (ret != null && ret.equals("\"true\"")) {
            log("Cancelled Order: " + orderId);
            return true;
        }
        logError("Unable to cancel order: " + orderId);
        logError(ret);
        return false;
    }

    public BookOrder placeBuyLimitOrder(BigDecimal price, BigDecimal amount) {
        return placeBuyLimitOrder(price, amount, BitsoBook.BTC_MXN);
    }

    public BookOrder placeBuyLimitOrder(BigDecimal price, BigDecimal amount, BitsoBook book) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("amount", amount.toPlainString());
        body.put("price", price.toPlainString());
        body.put("book", book.toString());
        log("Placing the following buy limit order: " + body);
        String json = null;// sendBitsoPost(baseUrl + "buy", body);
        return processBookOrderJSON(json);
    }

    public BigDecimal placeBuyMarketOrder(BigDecimal mxnAmountToSpend) {
        return placeBuyMarketOrder(mxnAmountToSpend, BitsoBook.BTC_MXN);
    }

    public BigDecimal placeBuyMarketOrder(BigDecimal mxnAmountToSpend, BitsoBook book) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("amount", mxnAmountToSpend.toPlainString());
        body.put("book", book.toString());
        log("Placing the following buy maket order: " + body);
        String json = null;// sendBitsoPost(baseUrl + "buy", body);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Unable to place Buy Market Order: " + json);
            return null;
        }

        BookOrder order = findMatchingOrders(o);
        if (order != null) {
            return order.major;
        }
        return null;
    }

    public BookOrder placeSellLimitOrder(BigDecimal price, BigDecimal amount) {
        return placeSellLimitOrder(price, amount, BitsoBook.BTC_MXN);
    }

    public BookOrder placeSellLimitOrder(BigDecimal price, BigDecimal amount, BitsoBook book) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("amount", amount.toPlainString());
        body.put("price", price.toPlainString());
        body.put("book", book.toString());
        log("Placing the following sell limit order: " + body);
        String json = null;// sendBitsoPost(baseUrl + "sell", body);
        return processBookOrderJSON(json);
    }

    public BigDecimal placeSellMarketOrder(BigDecimal btcAmountToSpend) {
        return placeSellMarketOrder(btcAmountToSpend, BitsoBook.BTC_MXN);
    }

    public BigDecimal placeSellMarketOrder(BigDecimal btcAmountToSpend, BitsoBook book) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("amount", btcAmountToSpend.toPlainString());
        body.put("book", book.toString());
        log("Placing the following sell market order: " + body);
        String json = null;// sendBitsoPost(baseUrl + "sell", body);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Unable to place Sell Market Order: " + json);
            return null;
        }
        BookOrder order = findMatchingOrders(o);
        if (order != null) {
            return order.minor;
        }
        return null;
    }

    public String getDepositAddress() {
        return quoteEliminator(sendBitsoPost(baseUrl + "bitcoin_deposit_address"));
    }

    public boolean withdrawBTC(String address, BigDecimal amount) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("amount", amount.toPlainString());
        body.put("address", address);
        log("Executing the following BTC withdrawal: " + body);
        String ret = null;// sendBitsoPost(baseUrl + "bitcoin_withdrawal", body);
        if (ret != null && ret.equals("\"ok\"")) {
            log("BTC withdrawal executed");
            return true;
        }
        logError("Unable to execute BTC withdrawal");
        logError(ret);
        return false;
    }

    public BitsoTransfer getTransferStatus(String transferId) {
        String ret = sendGet(baseUrl + "transfer/" + transferId);
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

    private static Entry<String, String> buildBitsoAuthHeader(String secretKey, String publicKey, long nonce,
            String httpMethod, String requestPath, String jsonPayload) {
        if (jsonPayload == null) jsonPayload = "";
        String message = String.valueOf(nonce) + httpMethod + requestPath + jsonPayload;
        String signature = "";
        byte[] secretBytes = secretKey.getBytes();
        SecretKeySpec localMac = new SecretKeySpec(secretBytes, "HmacSHA256");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(localMac);
            // Compute the hmac on input data bytes
            byte[] arrayOfByte = mac.doFinal(message.getBytes());
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
        String authHeader = String.format("Bitso %s:%s:%s", publicKey, nonce, signature);
        Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>("Authorization",
                authHeader);
        return entry;
    }

    // private static String signRequest(String secret, String inputStr) {
    // String signature = "";
    // byte[] secretBytes = secret.getBytes();
    // SecretKeySpec localMac = new SecretKeySpec(secretBytes, "HmacSHA256");
    // try {
    // Mac mac = Mac.getInstance("HmacSHA256");
    // mac.init(localMac);
    // // Compute the hmac on input data bytes
    // byte[] arrayOfByte = mac.doFinal(inputStr.getBytes());
    // BigInteger localBigInteger = new BigInteger(1, arrayOfByte);
    // signature = String.format("%0" + (arrayOfByte.length << 1) + "x",
    // new Object[] { localBigInteger });
    // } catch (InvalidKeyException e) {
    // e.printStackTrace();
    // } catch (NoSuchAlgorithmException e) {
    // e.printStackTrace();
    // } catch (IllegalStateException e) {
    // e.printStackTrace();
    // }
    // return signature;
    // }

    private String sendBitsoPost(String requestPath, JSONObject jsonPayload) {
        long nonce = System.currentTimeMillis();
        String jsonString = jsonPayload.toString();
        Entry<String, String> header = buildBitsoAuthHeader(secret, key, nonce, "POST", requestPath,
                jsonString);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put(header.getKey(), header.getValue());
        try {
            return client.sendPost(baseUrl + requestPath, jsonString, headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // private String sendBitsoPost(String url, HashMap<String, Object> bodyExtras) {
    // long nonce = System.currentTimeMillis();
    // String message = nonce + key + clientId;
    // String signature = signRequest(secret, message);
    //
    // JSONObject json = new JSONObject();
    // json.put("key", key);
    // json.put("nonce", nonce);
    // json.put("signature", signature);
    //
    // if (bodyExtras != null) {
    // for (Entry<String, Object> e : bodyExtras.entrySet()) {
    // json.put(e.getKey(), e.getValue());
    // }
    // }
    //
    // HashMap<String, String> headers = new HashMap<String, String>();
    // headers.put("Content-Type", "application/json");
    // try {
    // return client.sendPost(url, json.toString(), headers);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // return null;
    // }

    private String sendBitsoPost(String url) {
        return sendBitsoPost(url, null);
    }

    private String sendGet(String requestPath) {
        HashMap<String, String> headers = new HashMap<String, String>();
        // headers.put("Content-Type", "application/json");
        try {
            return client.sendGet(baseUrl + requestPath, headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String sendBitsoGet(String requestPath) {
        long nonce = System.currentTimeMillis();
        Entry<String, String> authHeader = buildBitsoAuthHeader(secret, key, nonce, "GET", requestPath, null);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put(authHeader.getKey(), authHeader.getValue());
        // headers.
        try {
            return client.sendGet(baseUrl + requestPath, headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BookOrder findMatchingOrders(String id) {
        BookOrder toRet = null;
        int offset = 0;
        int limit = 10;
        outer: while (true) {
            BitsoUserTransactions but = getUserTransactions(offset, limit, null);
            if (but == null) {
                return null;
            }
            // We gone through the entire history and didn't find anything...
            if (but.trades.size() == 0) {
                return null;
            }

            for (int i = 0; i < but.trades.size(); i++) {
                BookOrder order = but.trades.get(i);
                if (order.id.equals(id)) {
                    if (toRet == null) {
                        toRet = order;
                    } else {
                        toRet.minor = toRet.minor.add(order.minor);
                    }
                } else if (toRet != null && i < but.trades.size()) {
                    break outer;
                }
            }
            offset += limit; // increase the offset by the number of orders we just looked at
            limit *= 2; // increase the limit scope to make searching faster
        }
        return toRet;
    }

    public BookOrder findMatchingOrders(JSONObject o) {
        BookOrder toRet = null;
        if (o.has("id")) {
            toRet = findMatchingOrders(o.getString("id"));
        }
        if (toRet == null) {
            logError("Unable to find order in recent transactions");
            Helpers.printStackTrace();
        }

        return toRet;
    }

    public static BookOrder processBookOrderJSON(String json) {
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            System.err.println("Unable to processBookOrderJSON: " + json);
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
            order.book = BitsoBook.valueOf(o.getString("book"));
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
            order.createdAt = o.getString("created");
        }
        if (o.has("updated")) {
            order.updatedAt = o.getString("updated");
        }
        if (o.has("datetime")) {
            order.dateTime = o.getString("datetime");
        }
        return order;
    }
}
