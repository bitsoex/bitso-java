package com.bitso;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.experimental.theories.Theories;

import com.bitso.exchange.BookInfo;
import com.bitso.helpers.Helpers;
import com.bitso.http.BlockingHttpClient;

public class Bitso {
    private static final String BITSO_BASE_URL_PRODUCTION = "https://bitso.com";
    private static final String BITSO_BASE_URL_DEV = "https://dev.bitso.com";

    public static long THROTTLE_MS = 1000;

    private String key;
    private String secret;
    private boolean log;
    private String baseUrl;

    private BlockingHttpClient client = new BlockingHttpClient(false, THROTTLE_MS);

    private static enum CURRENCY_WITHDRAWALS {
        BITCOIN_WITHDRAWAL, ETHER_WITHDRAWAL;

        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public Bitso(String key, String secret) {
        this(key, secret, 0);
    }

    public Bitso(String key, String secret, int retries) {
        this(key, secret, retries, true);
    }

    public Bitso(String key, String secret, int retries, boolean log) {
        this(key, secret, retries, log, true);
    }

    public Bitso(String key, String secret, int retries, boolean log, boolean production) {
        this.key = key;
        this.secret = secret;
        this.log = log;
        this.baseUrl = production ? BITSO_BASE_URL_PRODUCTION : BITSO_BASE_URL_DEV;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
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
    public BookInfo[] getAvailableBooks() {
        String request = "/api/v3/available_books";

        String json = sendGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Unable to get availableBooks");
            return null;
        }
        if (o.has("payload")) {
            JSONArray array = o.getJSONArray("payload");
            int totalElements = array.length();
            BookInfo[] books = new BookInfo[totalElements];
            for (int i = 0; i < totalElements; i++) {
                books[i] = new BookInfo(array.getJSONObject(i));
            }
            return books;
        }
        return null;
    }

    public BitsoTicker[] getTicker() {
        String request = "/api/v3/ticker";

        String json = sendGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Unable to get Bitso Ticker");
            return null;
        }
        if (o.has("payload")) {
            JSONArray array = o.getJSONArray("payload");
            int totalElements = array.length();
            BitsoTicker[] tickers = new BitsoTicker[totalElements];
            for (int i = 0; i < totalElements; i++) {
                tickers[i] = new BitsoTicker(array.getJSONObject(i));
            }
            return tickers;
        }
        return null;
    }

    public BitsoOrderBook getOrderBook(BitsoBook book, boolean... aggregate) {
        String request = "/api/v3/order_book?book=" + book.toString();
        if (aggregate != null && aggregate.length == 1) {
            if (aggregate[0]) {
                request += "&aggregate=true";
            } else {
                request += "&aggregate=false";
            }
        }

        String json = sendGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Unable to get Bitso Order Book");
            return null;
        }
        if (o.has("payload")) {
            return new BitsoOrderBook(o.getJSONObject("payload"));
        }
        return null;
    }

    public BitsoTransactions getTrades(BitsoBook book, String... queryParameters) {
        String parsedQueryParametes = processQueryParameters("&", queryParameters);
        String request = "/api/v3/trades?book=" + book.toString()
                + ((parsedQueryParametes != null) ? "&" + parsedQueryParametes : "");

        String json = sendGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Unable to get Trades");
            return null;
        }
        if (o.has("payload")) {
            return new BitsoTransactions(o.getJSONArray("payload"));
        }
        return null;
    }

    // Private Functions
    public BitsoAccountStatus getAccountStatus() {
        String request = "/api/v3/account_status";
        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting Bitso Account Status");
            return null;
        }
        if (o.has("payload")) {
            return new BitsoAccountStatus(o.getJSONObject("payload"));
        }
        return null;
    }

    public BitsoBalance getAccountBalance() {
        String request = "/api/v3/balance";
        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting account balance");
            return null;
        }
        if (o.has("payload")) {
            return new BitsoBalance(o.getJSONObject("payload"));
        }
        return null;
    }

    public BitsoFee getFees() {
        String request = "/api/v3/fees";
        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting user fees: " + json);
            return null;
        }
        if (o.has("payload")) {
            return new BitsoFee(o.getJSONObject("payload"));
        }
        return null;
    }

    public BitsoOperation[] getLedger(String specificOperation, String... queryParameters) {
        String request = "/api/v3/ledger";

        if (specificOperation != null && specificOperation.length() > 0) {
            request += "/" + specificOperation;
        }

        String parsedQueryParametes = processQueryParameters("&", queryParameters);
        request += ((parsedQueryParametes != null) ? "?" + parsedQueryParametes : "");

        log(request);

        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting user ledgers");
            return null;
        }
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoOperation[] operations = new BitsoOperation[totalElements];
            for (int i = 0; i < totalElements; i++) {
                operations[i] = new BitsoOperation(payload.getJSONObject(i));
            }
            return operations;
        }
        return null;
    }

    /**
     * The request needs withdrawalsIds or queryParameters, not both. In case both parameters are provided
     * null will be returned
     * 
     * @param withdrawalsIds
     * @param queryParameters
     * @return BitsoWithdrawal[]
     */
    public BitsoWithdrawal[] getWithdrawals(String[] withdrawalsIds, String... queryParameters) {
        String request = "/api/v3/withdrawals";

        if ((withdrawalsIds != null && (queryParameters != null && queryParameters.length > 0))) {
            return null;
        }

        if (withdrawalsIds != null) {
            String withdrawalsIdsParameters = processQueryParameters("-", withdrawalsIds);
            request += ((withdrawalsIdsParameters != null) ? "/" + withdrawalsIdsParameters : "");
        }

        if (queryParameters != null && queryParameters.length > 0) {
            String parsedQueryParametes = processQueryParameters("&", queryParameters);
            request += ((parsedQueryParametes != null) ? "?" + parsedQueryParametes : "");
        }

        log(request);

        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting user withdrawal");
            return null;
        }
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoWithdrawal[] withdrawals = new BitsoWithdrawal[totalElements];
            for (int i = 0; i < totalElements; i++) {
                withdrawals[i] = new BitsoWithdrawal(payload.getJSONObject(i));
            }
            return withdrawals;
        }
        return null;
    }

    /**
     * The request needs fundingssIds or queryParameters, not both. In case both parameters are provided null
     * will be returned
     * 
     * @param fundingssIds
     * @param queryParameters
     * @return
     */
    public BitsoFunding[] getFundings(String[] fundingssIds, String... queryParameters) {
        String request = "/api/v3/fundings";

        if ((fundingssIds != null && (queryParameters != null && queryParameters.length > 0))) {
            return null;
        }

        if (fundingssIds != null) {
            String fundingssIdsParameters = processQueryParameters("-", fundingssIds);
            request += ((fundingssIdsParameters != null) ? "/" + fundingssIdsParameters : "");
        }

        if (queryParameters != null && queryParameters.length > 0) {
            String parsedQueryParametes = processQueryParameters("&", queryParameters);
            request += ((parsedQueryParametes != null) ? "?" + parsedQueryParametes : "");
        }

        log(request);

        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting user withdrawal");
            return null;
        }
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoFunding[] fundings = new BitsoFunding[totalElements];
            for (int i = 0; i < totalElements; i++) {
                fundings[i] = new BitsoFunding(payload.getJSONObject(i));
            }
            return fundings;
        }
        return null;
    }

    /**
     * The request needs tradesIds or queryParameters, not both. In case both parameters are provided null
     * will be returned
     * 
     * @param tradesIds
     * @param queryParameters
     * @return
     */
    public BitsoTrade[] getUserTrades(String[] tradesIds, String... queryParameters) {
        String request = "/api/v3/user_trades";

        if ((tradesIds != null && (queryParameters != null && queryParameters.length > 0))) {
            return null;
        }

        if (tradesIds != null) {
            String fundingssIdsParameters = processQueryParameters("-", tradesIds);
            request += ((fundingssIdsParameters != null) ? "/" + fundingssIdsParameters : "");
        }

        if (queryParameters != null && queryParameters.length > 0) {
            String parsedQueryParametes = processQueryParameters("&", queryParameters);
            request += ((parsedQueryParametes != null) ? "?" + parsedQueryParametes : "");
        }

        log(request);

        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting user trades");
            return null;
        }
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoTrade[] trades = new BitsoTrade[totalElements];
            for (int i = 0; i < totalElements; i++) {
                trades[i] = new BitsoTrade(payload.getJSONObject(i));
            }
            return trades;
        }
        return null;
    }

    public BitsoTrade[] getOrderTrades(String orderId) {
        String request = "/api/v3/order_trades";

        if (orderId == null || orderId.trim().length() == 0) {
            return null;
        }

        request += "/" + orderId;

        log(request);

        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting order trades");
            return null;
        }
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoTrade[] trades = new BitsoTrade[totalElements];
            for (int i = 0; i < totalElements; i++) {
                trades[i] = new BitsoTrade(payload.getJSONObject(i));
            }
            return trades;
        }
        return null;
    }

    public BitsoOrder[] getOpenOrders(BitsoBook book, String... queryParameters) {
        String request = "/api/v3/open_orders";

        request += "?" + "book=" + book.toString();

        String parsedQueryParametes = processQueryParameters("&", queryParameters);
        request += ((parsedQueryParametes != null) ? "&" + parsedQueryParametes : "");

        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error in Open Orders");
            return null;
        }
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoOrder[] orders = new BitsoOrder[totalElements];
            for (int i = 0; i < totalElements; i++) {
                orders[i] = new BitsoOrder(payload.getJSONObject(i));
            }
            return orders;
        }
        return null;
    }

    public BitsoOrder[] lookupOrders(String... ordersId) {
        String request = "/api/v3/orders";

        if (ordersId == null || ordersId.length == 0) {
            return null;
        }

        String ordersIdsParameters = processQueryParameters("-", ordersId);
        request += "/" + ordersIdsParameters;

        log(request);

        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error in lookupOrders");
            return null;
        }
        if (o.has("payload")) {
            JSONArray payload = o.getJSONArray("payload");
            int totalElements = payload.length();
            BitsoOrder[] orders = new BitsoOrder[totalElements];
            for (int i = 0; i < totalElements; i++) {
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
            log("An order should be specified in terms of major or minor, never both or any");
            return null;
        }

        if (type.equals(BitsoOrder.TYPE.MARKET) && (price != null)) {
            log("On market order a price does not need to be specified");
            return null;
        }

        // Filling data for request
        parameters.put("book", book.toString().toLowerCase());
        parameters.put("side", side.toString().toLowerCase());
        parameters.put("type", type.toString().toLowerCase());

        if (type.equals(BitsoOrder.TYPE.LIMIT) && (price != null)) {
            parameters.put("price", price.toString());
        }

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

    public String[] cancelOrder(String... ordersIds) {
        String request = "/api/v3/orders";

        if (ordersIds == null || ordersIds.length == 0) {
            return null;
        }

        String ordersIdsParameters = processQueryParameters("-", ordersIds);
        request += "/" + ordersIdsParameters;

        log(request);
        String json = sendBitsoDelete(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error cancelling orders: " + json);
            return null;
        }
        return Helpers.parseJSONArray(o.getJSONArray("payload"));
    }

    public Map<String, String> fundingDestination(String currencyParameter) {
        String request = "/api/v3/funding_destination";

        if (currencyParameter == null || currencyParameter.trim().length() == 0) {
            return null;
        }

        request += "?" + currencyParameter;

        log(request);

        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error getting funding destination: " + json);
            return null;
        }
        if (o.has("payload")) {
            JSONObject payload = o.getJSONObject("payload");
            Map<String, String> fundingDestination = new HashMap<String, String>();
            fundingDestination.put("account_identifier_name",
                    Helpers.getString(payload, "account_identifier_name"));
            fundingDestination.put("account_identifier", Helpers.getString(payload, "account_identifier"));
            return fundingDestination;
        }
        return null;
    }

    public BitsoWithdrawal bitcoinWithdrawal(BigDecimal amount, String address) {
        return currencyWithdrawal(CURRENCY_WITHDRAWALS.BITCOIN_WITHDRAWAL, amount, address);
    }

    public BitsoWithdrawal etherWithdrawal(BigDecimal amount, String address) {
        return currencyWithdrawal(CURRENCY_WITHDRAWALS.ETHER_WITHDRAWAL, amount, address);
    }

    public BitsoWithdrawal speiWithdrawal(BigDecimal amount, String recipientGivenNames,
            String recipientFamilyNames, String clabe, String notesReference, String numericReference) {
        String request = "/api/v3/spei_withdrawal";
        JSONObject parameters = new JSONObject();
        parameters.put("amount", amount.toString());
        parameters.put("recipient_given_names", recipientGivenNames);
        parameters.put("recipient_family_names", recipientFamilyNames);
        parameters.put("clabe", clabe);
        parameters.put("notes_ref", notesReference);
        parameters.put("numeric_ref", numericReference);
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

    public Map<String, String> getBanks() {
        String request = "/api/v3/mx_bank_codes";

        log(request);

        String json = sendBitsoGet(request);
        JSONObject o = Helpers.parseJson(json);
        if (o == null || o.has("error")) {
            logError("Error in getBanks");
            return null;
        }
        if (o.has("payload")) {
            Map<String, String> banks = new HashMap<String, String>();
            JSONArray payload = o.getJSONArray("payload");
            String currentBankCode = "";
            String currentBankName = "";
            JSONObject currentJSON = null;
            int totalElements = payload.length();
            for (int i = 0; i < totalElements; i++) {
                currentJSON = payload.getJSONObject(i);
                currentBankCode = Helpers.getString(currentJSON, "code");
                currentBankName = Helpers.getString(currentJSON, "name");
                banks.put(currentBankCode, currentBankName);
            }
            return banks;
        }
        return null;
    }

    public BitsoWithdrawal debitCardWithdrawal(BigDecimal amount, String recipientGivenNames,
            String recipientFamilyNames, String cardNumber, String bankCode) {
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
            String recipientFamilyNames, String phoneNumber, String bankCode) {
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

    private BitsoWithdrawal currencyWithdrawal(CURRENCY_WITHDRAWALS withdrawal, BigDecimal amount,
            String address) {
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

    public String getDepositAddress() {
        return quoteEliminator(sendBitsoPost(baseUrl + "bitcoin_deposit_address"));
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

    private String buildBitsoAuthHeader(String requestPath, String httpMethod, String apiKey, String secret) {
        long nonce = System.currentTimeMillis() + System.currentTimeMillis();
        byte[] secretBytes = secret.getBytes();
        byte[] arrayOfByte = null;
        String signature = null;
        BigInteger bigInteger = null;
        Mac mac = null;

        String message = nonce + httpMethod + requestPath;
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretBytes, "HmacSHA256");
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            arrayOfByte = mac.doFinal(message.getBytes());
            bigInteger = new BigInteger(1, arrayOfByte);
            signature = String.format("%0" + (arrayOfByte.length << 1) + "x", new Object[] { bigInteger });
            return String.format("Bitso %s:%s:%s", apiKey, nonce, signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
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

    public String sendGet(String requestedURL) {
        try {
            URL url = new URL(baseUrl + requestedURL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Android");

            if (con.getResponseCode() == 200) {
                int responseCode = con.getResponseCode();
                return convertInputStreamToString(con.getInputStream());
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private String sendBitsoHttpRequest(String requestPath, String method) {
        String response = null;
        String requestURL = baseUrl + requestPath;

        try {
            URL url = new URL(requestURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("Authorization",
                    buildBitsoAuthHeader(requestPath, "GET", key, secret));
            connection.setRequestProperty("User-Agent", "Bitso-java-api");
            connection.setRequestMethod(method);
            if (connection.getResponseCode() == 200) {
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                response = convertInputStreamToString(inputStream);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private String sendBitsoGet(String requestPath) {
        return sendBitsoHttpRequest(requestPath, "GET");
    }

    private String sendBitsoDelete(String requestPath) {
        long nonce = System.currentTimeMillis() + System.currentTimeMillis();
        Entry<String, String> authHeader = buildBitsoAuthHeader(secret, key, nonce, "DELETE", requestPath,
                null);
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

    private String sendBitsoPost(String url) {
        return sendBitsoPost(url, null);
    }

    private String sendBitsoPost(String requestPath, JSONObject jsonPayload) {
        long nonce = System.currentTimeMillis() + System.currentTimeMillis();
        String jsonString = "";
        if (jsonPayload != null) {
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

    private static String convertInputStreamToString(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    private String processQueryParameters(String separator, String... parameters) {
        if (parameters == null) {
            return null;
        }

        int totalParameters = parameters.length;

        if (totalParameters == 0) {
            return null;
        }

        String queryString = "";
        for (int i = 0; i < (totalParameters - 1); i++) {
            String currentParameter = parameters[i].trim();

            if (currentParameter.length() == 0) {
                continue;
            }

            queryString += currentParameter + separator;
        }

        String lastParameter = parameters[totalParameters - 1].trim();
        // Meaning that the last parameter is not empty
        if (lastParameter.length() != 0) {
            queryString += parameters[totalParameters - 1];
            // Remove the separator symbol at the end if query string has it
        } else if (queryString.endsWith(separator)) {
            queryString = queryString.substring(0, (queryString.length() - 1));
        }

        return queryString;
    }
}
