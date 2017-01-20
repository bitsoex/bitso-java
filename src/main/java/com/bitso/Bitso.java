package com.bitso;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONArray;
import org.json.JSONObject;
import com.bitso.exchange.BookInfo;
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

    private static enum CURRENCY_WITHDRAWALS {
        BITCOIN_WITHDRAWAL, ETHER_WITHDRAWAL;

        public String toString() {
            return this.name().toLowerCase();
        }
    }

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
        if(o.has("payload")){
            return new BitsoOrderBook(o.getJSONObject("payload"));
        }
        return null;
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
    public BitsoAccountStatus getUserAccountStatus() {
        String json = sendBitsoGet("/api/v3/account_status");
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting Bitso Account Status: " + json);
            return null;
        }
        return new BitsoAccountStatus(o);
    }

    public BitsoBalance[] getUserAccountBalance(){
        String json =  sendBitsoGet("/api/v3/balance");
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error getting account balance: " + json);
            return null;
        }
        if(o.has("payload")){
            JSONObject payload = o.getJSONObject("payload");
            JSONArray jsonBalances = payload.getJSONArray("balances");
            int totalElements = jsonBalances.length();
            BitsoBalance[] balances = new BitsoBalance[totalElements];
            for(int i=0; i<totalElements; i++){
                balances[i] = new BitsoBalance(jsonBalances.getJSONObject(i));
            }
            return balances;
        }
        return null;
    }

    public BitsoFee[] getUserFees(){
        String json =  sendBitsoGet("/api/v3/fees");
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error getting user fees: " + json);
            return null;
        }
        if(o.has("payload")){
            JSONObject payload = o.getJSONObject("payload");
            JSONArray jsonBalances = payload.getJSONArray("fees");
            int totalElements = jsonBalances.length();
            BitsoFee[] fees = new BitsoFee[totalElements];
            for(int i=0; i<totalElements; i++){
                fees[i] = new BitsoFee(jsonBalances.getJSONObject(i));
            }
            return fees;
        }
        return null;
    }

    public BitsoOperation[] getUserLedger(String specificOperation){
        String request = "/api/v3/ledger";
        if(specificOperation != null){
            request += "/" + specificOperation;
        }
        log(request);
        String json =  sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error getting user ledgers: " + json);
            return null;
        }
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoOperation[] operations = new BitsoOperation[totalElements];
            for(int i=0; i<totalElements; i++){
                operations[i] = new BitsoOperation(payload.getJSONObject(i));
            }
            return operations;
        }
        return null;
    }

    public BitsoWithdrawal[] getUserWithdrawals(String... withdrawalsIds) {
        String request = "/api/v3/withdrawals/";
        request += buildDynamicURLParameters(withdrawalsIds);
        log(request);
        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting user withdrawals: " + json);
            return null;
        }
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoWithdrawal[] withdrawals = new BitsoWithdrawal[totalElements];
            for(int i=0; i<totalElements; i++){
                withdrawals[i] = new BitsoWithdrawal(payload.getJSONObject(i));
            }
            return withdrawals;
        }
        return null;
    }

    public BitsoFunding[] getUserFundings(String... fundingsIds){
        String request = "/api/v3/fundings/";
        request += buildDynamicURLParameters(fundingsIds);
        log(request);
        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error getting user fundings: " + json);
            return null;
        }
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoFunding[] fundings = new BitsoFunding[totalElements];
            for(int i=0; i<totalElements; i++){
                fundings[i] = new BitsoFunding(payload.getJSONObject(i));
            }
            return fundings;
        }
        return null;
    }

    public BitsoTrade[] getUserTrades(String... tradesIds){
        String request = "/api/v3/user_trades/";
        request += buildDynamicURLParameters(tradesIds);
        log(request);
        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error getting user trades: " + json);
            return null;
        }
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoTrade[] trades = new BitsoTrade[totalElements];
            for(int i=0; i<totalElements; i++){
                trades[i] = new BitsoTrade(payload.getJSONObject(i));
            }
            return trades;
        }
        return null;
    }

    public BitsoOrder[] getOpenOrders(){
        String request = "/api/v3/open_orders?book=btc_mxn";
        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error in Open Orders: " + json);
            return null;
        }
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoOrder[] orders = new BitsoOrder[totalElements];
            for(int i=0; i<totalElements; i++){
                orders[i] = new BitsoOrder(payload.getJSONObject(i));
            }
            return orders;
        }
        return null;
    }

    public BitsoOrder[] lookupOrders(String... ordersId){
        String request = "/api/v3/orders/";
        request += buildDynamicURLParameters(ordersId);
        log(request);
        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error in lookupOrders: " + json);
            return null;
        }
        if(o.has("payload")){
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoOrder[] orders = new BitsoOrder[totalElements];
            for(int i=0; i<totalElements; i++){
                orders[i] = new BitsoOrder(payload.getJSONObject(i));
            }
            return orders;
        }
        return null;
    }

    public String placeOrder(BitsoBook book, BitsoOrder.SIDE side, BitsoOrder.TYPE type, BigDecimal major,
            BigDecimal minor, BigDecimal price) {
        String request = "/api/v3/orders";
        JSONObject parameters = new JSONObject();

        if ((major != null && minor != null) || (major == null && minor == null)) {
            log("An order should be specified in terms of major or minor, never both.");
            return null;
        }

        if ((type.compareTo(BitsoOrder.TYPE.LIMIT) == 0) && (price != null)) {
            parameters.put("price", price.toString());
        } else {
            log("Price must be specified on limit orders.");
            return null;
        }

        parameters.put("book", book.toString());
        parameters.put("side", side.toString());
        parameters.put("type", type.toString());

        if (major != null) {
            parameters.put("major", major.toString());
        } else {
            parameters.put("minor", minor.toString());
        }
        String json = sendBitsoPost(request, parameters);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error placing an order: " + json);
            return null;
        }
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            return Helpers.getString(payload, "oid");
        }
        return null;
    }

    public String[] cancelOrder(String... orders) {
        String request = "/api/v3/orders/";
        request += buildDynamicURLParameters(orders);
        log(request);
        String json = sendBitsoDelete(request);
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error cancelling orders: " + json);
            return null;
        }
        return Helpers.parseJSONArray(o.getJSONArray("payload"));
    }

    public Map<String, String> fundingDestination(String fundCurrency){
        String request = "/api/v3/funding_destination?" +
                "fund_currency=" + fundCurrency;
        log(request);
        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error getting funding destination: " + json);
            return null;
        }
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            Map<String, String> fundingDestination =  new HashMap<String, String>();
            fundingDestination.put("accountIdentifierName",
                    Helpers.getString(payload, "account_identifier_name"));
            fundingDestination.put("accountIdentifier",
                    Helpers.getString(payload, "account_identifier"));
            return fundingDestination;
        }
        return null;
    }

    public BitsoWithdrawal bitcoinWithdrawal(BigDecimal amount, String address){
        return currencyWithdrawal(CURRENCY_WITHDRAWALS.BITCOIN_WITHDRAWAL, amount, address);
    }

    public BitsoWithdrawal etherWithdrawal(BigDecimal amount, String address){
        return currencyWithdrawal(CURRENCY_WITHDRAWALS.ETHER_WITHDRAWAL, amount, address);
    }

    public BitsoWithdrawal speiWithdrawal(BigDecimal amount, String recipientGivenNames,
            String recipientFamilyNames, String clabe, String notesReference,
            String numericReference){
        String request = "/api/v3/spei_withdrawal";
        JSONObject parameters = new JSONObject();
        parameters.put("amount", amount.toString());
        parameters.put("recipient_given_names", recipientGivenNames);
        parameters.put("recipient_family_names", recipientFamilyNames);
        // TODO:
        // CLABE is espected in uppercase it should be expected indiferently
        // Check on server side
        parameters.put("clabe", clabe);
        parameters.put("notes_ref", notesReference);
        parameters.put("numeric_ref", numericReference);
        String json = sendBitsoPost(request, parameters);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error executing withdrawal" + json);
            return null;
        }
        // TODO:
        // Error in JSON payload, does not contains "details" key
        // and it should contain it
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            return new BitsoWithdrawal(payload);
        }
        return null;
    }

    public BitsoBank[] getBanks(){
        String request = "/api/v3/mx_bank_codes";
        log(request);
        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if(o == null || o.has("error")){
            logError("Error in lookupOrders: " + json);
            return null;
        }
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoBank[] elements =  new BitsoBank[totalElements];
            for(int i=0; i<totalElements; i++){
                elements[i] = new BitsoBank(payload.getJSONObject(i));
            }
            return elements;
        }
        return null;
    }

    public BitsoWithdrawal debitCardWithdrawal(BigDecimal amount, String recipientGivenNames,
            String recipientFamilyNames, String cardNumber, String bankCode){
        String request = "/api/v3/debit_card_withdrawal";
        JSONObject parameters = new JSONObject();
        parameters.put("amount", amount.toString());
        parameters.put("recipient_given_names", recipientGivenNames);
        parameters.put("recipient_family_names", recipientFamilyNames);
        parameters.put("card_number", cardNumber);
        parameters.put("bank_code", bankCode);
        String json = sendBitsoPost(request, parameters);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error executing withdrawal" + json);
            return null;
        }
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            return new BitsoWithdrawal(payload);
        }
        return null;
    }

    public BitsoWithdrawal phoneWithdrawal(BigDecimal amount, String recipientGivenNames,
            String recipientFamilyNames, String phoneNumber, String bankCode){
        String request = "/api/v3/phone_withdrawal";
        JSONObject parameters = new JSONObject();
        parameters.put("amount", amount.toString());
        parameters.put("recipient_given_names", recipientGivenNames);
        parameters.put("recipient_family_names", recipientFamilyNames);
        parameters.put("phone_number", phoneNumber);
        parameters.put("bank_code", bankCode);
        String json = sendBitsoPost(request, parameters);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error executing withdrawal" + json);
            return null;
        }
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            return new BitsoWithdrawal(payload);
        }
        return null;
    }

    private BitsoWithdrawal currencyWithdrawal(CURRENCY_WITHDRAWALS withdrawal, BigDecimal amount, String address){
        String request = "/api/v3/" + withdrawal.toString();
        JSONObject parameters = new JSONObject();
        parameters.put("amount", amount.toString());
        parameters.put("address", address);
        String json = sendBitsoPost(request, parameters);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error executing withdrawal: " + json);
            return null;
        }
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            return new BitsoWithdrawal(payload);
        }
        return null;
    }
//    public BitsoUserTransactions getUserTransactions() {
//        return getUserTransactions(BitsoBook.BTC_MXN);
//    }
//
//
//    public BitsoUserTransactions getUserTransactions(BitsoBook book) {
//        return getUserTransactions(0, 0, null, book);
//    }
//
//
//    public BitsoUserTransactions getUserTransactions(int offset, int limit, SORT_ORDER sortOrder) {
//        return getUserTransactions(offset, limit, sortOrder, BitsoBook.BTC_MXN);
//    }
//
//    public BitsoUserTransactions getUserTransactions(int offset, int limit, SORT_ORDER sortOrder,
//            BitsoBook book) {
//        HashMap<String, Object> body = new HashMap<String, Object>();
//        if (offset > 0) body.put("offset", offset);
//        if (limit > 0) body.put("limit", limit);
//        if (sortOrder != null) body.put("sort", sortOrder.getOrder());
//        body.put("book", book.toString());
//        String json = null;// sendBitsoPost(baseUrl + "user_transactions", body);
//        JSONArray a = Helpers.parseJsonArray(json);
//        if (a == null) {
//            logError("Unable to get User Transactions: " + json);
//            return null;
//        }
//        return new BitsoUserTransactions(a, book);
//    }
//
//    public BitsoOpenOrders getOpenOrders() {
//        return getOpenOrders(BitsoBook.BTC_MXN);
//    }
//
//    public BitsoOpenOrders getOpenOrders(BitsoBook book) {
//        HashMap<String, Object> body = new HashMap<String, Object>();
//        body.put("book", book.toString());
//        String json = null;// sendBitsoPost(baseUrl + "open_orders", body);
//        JSONArray a = Helpers.parseJsonArray(json);
//        if (a == null) {
//            logError("Unable to get Open Orders: " + json);
//            return null;
//        }
//        return new BitsoOpenOrders(a, book);
//    }
//
//    /**
//     * Used to return a list of orders with the given orderId
//     *
//     * @deprecated use {@link #lookupOrder(String orderId)} instead.
//     */
//
//    @Deprecated
//    public BitsoLookupOrders getLookupOrders(String orderId) {
//        return lookupOrder(orderId);
//    }
//
//    public BitsoLookupOrders lookupOrder(String orderId) {
//        HashMap<String, Object> body = new HashMap<String, Object>();
//        body.put("id", orderId);
//        String json = null;// sendBitsoPost(baseUrl + "lookup_order", body);
//        JSONArray a = Helpers.parseJsonArray(json);
//        if (a == null) {
//            logError("Unable to get Lookup Order" + json);
//            return null;
//        }
//        return new BitsoLookupOrders(a);
//    }

//    public boolean cancelOrder(String orderId) {
//        log("Attempting to cancel order: " + orderId);
//        HashMap<String, Object> body = new HashMap<String, Object>();
//        body.put("id", orderId);
//        String ret = null;// sendBitsoPost(baseUrl + "cancel_order", body);
//        if (ret != null && ret.equals("\"true\"")) {
//            log("Cancelled Order: " + orderId);
//            return true;
//        }
//        logError("Unable to cancel order: " + orderId);
//        logError(ret);
//        return false;
//    }

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
        try {
            return client.sendGet(baseUrl + requestPath, headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String sendBitsoDelete(String requestPath) {
        long nonce = System.currentTimeMillis();
        Entry<String, String> authHeader = buildBitsoAuthHeader(secret, key, nonce, "DELETE", requestPath, null);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put(authHeader.getKey(), authHeader.getValue());
        try {
            return client.sendDelete(baseUrl + requestPath, headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String sendBitsoPost(String requestPath, JSONObject jsonPayload) {
        long nonce = System.currentTimeMillis();
        String jsonString = "";
        if(jsonPayload != null){
            jsonString = jsonPayload.toString();
        }
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

//    private String sendBitsoPost(String requestPath) {
//        long nonce = System.currentTimeMillis();
//        Entry<String, String> authHeader = buildBitsoAuthHeader(secret, key, nonce, "POST", requestPath, null);
//        HashMap<String, String> headers = new HashMap<String, String>();
//        headers.put("Content-Type", "application/json");
//        headers.put(authHeader.getKey(), authHeader.getValue());
//        try {
//            return client.sendPost(baseUrl + requestPath, "", headers);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    public BookOrder findMatchingOrders(String id) {
//        BookOrder toRet = null;
//        int offset = 0;
//        int limit = 10;
//        outer: while (true) {
//            BitsoUserTransactions but = getUserTransactions(offset, limit, null);
//            if (but == null) {
//                return null;
//            }
//            // We gone through the entire history and didn't find anything...
//            if (but.trades.size() == 0) {
//                return null;
//            }
//
//            for (int i = 0; i < but.trades.size(); i++) {
//                BookOrder order = but.trades.get(i);
//                if (order.id.equals(id)) {
//                    if (toRet == null) {
//                        toRet = order;
//                    } else {
//                        toRet.minor = toRet.minor.add(order.minor);
//                    }
//                } else if (toRet != null && i < but.trades.size()) {
//                    break outer;
//                }
//            }
//            offset += limit; // increase the offset by the number of orders we just looked at
//            limit *= 2; // increase the limit scope to make searching faster
//        }
//        return toRet;
//    }
//
//
//    public BookOrder findMatchingOrders(JSONObject o) {
//        BookOrder toRet = null;
//        if (o.has("id")) {
//            toRet = findMatchingOrders(o.getString("id"));
//        }
//        if (toRet == null) {
//            logError("Unable to find order in recent transactions");
//            Helpers.printStackTrace();
//        }
//
//        return toRet;
//    }
//
//
//    public static BookOrder processBookOrderJSON(String json) {
//        JSONObject o = Helpers.parseJson(json);
//        if (o == null || o.has("error")) {
//            System.err.println("Unable to processBookOrderJSON: " + json);
//            return null;
//        }
//        BigDecimal price = null, major = null;
//        if (o.has("price")) {
//            price = new BigDecimal(o.getString("price"));
//        }
//        if (o.has("amount")) {
//            major = new BigDecimal(o.getString("amount"));
//        }
//        if (price == null || major == null) {
//            return null;
//        }
//        BookOrder order = new BookOrder(price, major);
//        if (o.has("id")) {
//            order.id = o.getString("id");
//        }
//        if (o.has("book")) {
//            order.book = BitsoBook.valueOf(o.getString("book"));
//        }
//        if (o.has("type")) {
//            order.type = TYPE.values()[o.getInt("type")];
//        }
//        if (o.has("status")) {
//            int statusInt = o.getInt("status");
//            // Bitso passes CANCELLED as -1. We put CANCELLED in the STATUS enum in the 4th position
//            if (statusInt == -1) {
//                statusInt = 3;
//            }
//            order.status = STATUS.values()[statusInt];
//        }
//        if (o.has("created")) {
//            order.createdAt = o.getString("created");
//        }
//        if (o.has("updated")) {
//            order.updatedAt = o.getString("updated");
//        }
//        if (o.has("datetime")) {
//            order.dateTime = o.getString("datetime");
//        }
//        return order;
//    }

    private String buildDynamicURLParameters(String[] elements){
        int totalIds = elements.length;
        String parameters = "";
        if (totalIds > 0) {
            for (int i = 0; i < totalIds - 1; i++) {
                parameters += elements[i] + "-";
            }
            parameters += elements[totalIds - 1];;
        }
        return parameters;
    }
}
