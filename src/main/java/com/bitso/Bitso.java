package com.bitso;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exceptions.BitsoAPIException;
import com.bitso.exchange.BookInfo;
import com.bitso.helpers.Helpers;
import com.bitso.http.BlockingHttpClient;

public class Bitso {
    private static final String BITSO_BASE_URL_PRODUCTION = "https://api.bitso.com";
    private static final String BITSO_BASE_URL_DEV = "https://dev.bitso.com";
    private final String ETHER = "ether";
    private final String BITCOIN = "bitcoin";
    public static long THROTTLE_MS = 1000;

    private String key;
    private String secret;
    private boolean log;
    private String baseUrl;

    private BlockingHttpClient client = new BlockingHttpClient(false, THROTTLE_MS);

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
    public BookInfo[] getAvailableBooks() throws BitsoAPIException {
        String request = "/api/v3/available_books";

        String getResponse = sendGet(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(getResponse);
        int totalElements = payloadJSON.length();
        BookInfo[] books = new BookInfo[totalElements];
        for (int i = 0; i < totalElements; i++) {
            books[i] = new BookInfo(payloadJSON.getJSONObject(i));
        }
        return books;
    }

    public BitsoTicker[] getTicker() throws BitsoAPIException {
        String request = "/api/v3/ticker";

        String getResponse = sendGet(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(getResponse);
        int totalElements = payloadJSON.length();
        BitsoTicker[] tickers = new BitsoTicker[totalElements];
        for (int i = 0; i < totalElements; i++) {
            tickers[i] = new BitsoTicker(payloadJSON.getJSONObject(i));
        }
        return tickers;
    }

    public BitsoOrderBook getOrderBook(String book, boolean... aggregate) throws BitsoAPIException {
        String request = "/api/v3/order_book?book=" + book;

        if (aggregate != null && aggregate.length == 1) {
            if (aggregate[0]) {
                request += "&aggregate=true";
            } else {
                request += "&aggregate=false";
            }
        }

        String getResponse = sendGet(request);
        JSONObject payloadJSON = (JSONObject) getJSONPayload(getResponse);
        return new BitsoOrderBook(payloadJSON);
    }

    public BitsoTransactions getTrades(String book, String... queryParameters) throws BitsoAPIException {
        String parsedQueryParametes = processQueryParameters("&", queryParameters);
        String request = "/api/v3/trades?book=" + book
                + ((parsedQueryParametes != null) ? "&" + parsedQueryParametes : "");

        String getResponse = sendGet(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(getResponse);
        return new BitsoTransactions(payloadJSON);
    }

    // Private Functions
    public BitsoAccountStatus getAccountStatus() throws BitsoAPIException {
        String request = "/api/v3/account_status";

        String getResponse = sendBitsoGet(request);
        JSONObject payloadJSON = (JSONObject) getJSONPayload(getResponse);
        return new BitsoAccountStatus(payloadJSON);
    }

    public BitsoBalance getAccountBalance() throws BitsoAPIException {
        String request = "/api/v3/balance";
        String getResponse = sendBitsoGet(request);
        JSONObject payloadJSON = (JSONObject) getJSONPayload(getResponse);
        return new BitsoBalance(payloadJSON);
    }

    public BitsoFee getFees() throws BitsoAPIException {
        String request = "/api/v3/fees";
        String getResponse = sendBitsoGet(request);
        JSONObject payloadJSON = (JSONObject) getJSONPayload(getResponse);
        return new BitsoFee(payloadJSON);
    }

    public BitsoOperation[] getLedger(String specificOperation, String... queryParameters)
            throws BitsoAPIException {
        String request = "/api/v3/ledger";

        if (specificOperation != null && specificOperation.length() > 0) {
            request += "/" + specificOperation;
        }

        String parsedQueryParametes = processQueryParameters("&", queryParameters);
        request += ((parsedQueryParametes != null) ? "?" + parsedQueryParametes : "");

        String getResponse = sendBitsoGet(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(getResponse);
        int totalElements = payloadJSON.length();
        BitsoOperation[] operations = new BitsoOperation[totalElements];
        for (int i = 0; i < totalElements; i++) {
            operations[i] = new BitsoOperation(payloadJSON.getJSONObject(i));
        }
        return operations;
    }

    /**
     * The request needs withdrawalsIds or queryParameters, not both. In case both parameters are provided
     * null will be returned
     * 
     * @param withdrawalsIds
     * @param queryParameters
     * @return BitsoWithdrawal[]
     * @throws BitsoAPIException
     */
    public BitsoWithdrawal[] getWithdrawals(String[] withdrawalsIds, String... queryParameters)
            throws BitsoAPIException {
        String request = "/api/v3/withdrawals";

        if ((withdrawalsIds != null) && (queryParameters != null && queryParameters.length > 0)) {
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

        String getResponse = sendBitsoGet(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(getResponse);
        int totalElements = payloadJSON.length();
        BitsoWithdrawal[] withdrawals = new BitsoWithdrawal[totalElements];
        for (int i = 0; i < totalElements; i++) {
            withdrawals[i] = new BitsoWithdrawal(payloadJSON.getJSONObject(i));
        }
        return withdrawals;
    }

    /**
     * The request needs fundingssIds or queryParameters, not both. In case both parameters are provided null
     * will be returned
     * 
     * @param fundingssIds
     * @param queryParameters
     * @return
     * @throws BitsoAPIException
     */
    public BitsoFunding[] getFundings(String[] fundingssIds, String... queryParameters)
            throws BitsoAPIException {
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

        String getResponse = sendBitsoGet(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(getResponse);
        int totalElements = payloadJSON.length();
        BitsoFunding[] fundings = new BitsoFunding[totalElements];
        for (int i = 0; i < totalElements; i++) {
            fundings[i] = new BitsoFunding(payloadJSON.getJSONObject(i));
        }
        return fundings;
    }

    /**
     * The request needs tradesIds or queryParameters, not both. In case both parameters are provided null
     * will be returned
     * 
     * @param tradesIds
     * @param queryParameters
     * @return
     * @throws BitsoAPIException
     */
    public BitsoTrade[] getUserTrades(String[] tradesIds, String... queryParameters)
            throws BitsoAPIException {
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

        String getResponse = sendBitsoGet(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(getResponse);
        int totalElements = payloadJSON.length();
        BitsoTrade[] trades = new BitsoTrade[totalElements];
        for (int i = 0; i < totalElements; i++) {
            trades[i] = new BitsoTrade(payloadJSON.getJSONObject(i));
        }
        return trades;
    }

    public BitsoTrade[] getOrderTrades(String orderId) throws BitsoAPIException {
        String request = "/api/v3/order_trades";

        if (orderId == null || orderId.trim().length() == 0) {
            return null;
        }

        request += "/" + orderId;

        String getResponse = sendBitsoGet(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(getResponse);
        int totalElements = payloadJSON.length();
        BitsoTrade[] trades = new BitsoTrade[totalElements];
        for (int i = 0; i < totalElements; i++) {
            trades[i] = new BitsoTrade(payloadJSON.getJSONObject(i));
        }
        return trades;
    }

    public BitsoOrder[] getOpenOrders(String book, String... queryParameters) throws BitsoAPIException {
        String request = "/api/v3/open_orders";

        request += "?" + "book=" + book;

        String parsedQueryParametes = processQueryParameters("&", queryParameters);
        request += ((parsedQueryParametes != null) ? "&" + parsedQueryParametes : "");

        String getResponse = sendBitsoGet(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(getResponse);
        int totalElements = payloadJSON.length();
        BitsoOrder[] orders = new BitsoOrder[totalElements];
        for (int i = 0; i < totalElements; i++) {
            orders[i] = new BitsoOrder(payloadJSON.getJSONObject(i));
        }
        return orders;
    }

    public BitsoOrder[] lookupOrders(String... ordersId) throws BitsoAPIException {
        String request = "/api/v3/orders";

        if (ordersId == null || ordersId.length == 0) {
            return null;
        }

        String ordersIdsParameters = processQueryParameters("-", ordersId);
        request += "/" + ordersIdsParameters;

        String getResponse = sendBitsoGet(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(getResponse);
        int totalElements = payloadJSON.length();
        BitsoOrder[] orders = new BitsoOrder[totalElements];
        for (int i = 0; i < totalElements; i++) {
            orders[i] = new BitsoOrder(payloadJSON.getJSONObject(i));
        }
        return orders;
    }

    public String placeOrder(String book, BitsoOrder.SIDE side, BitsoOrder.TYPE type, BigDecimal major,
            BigDecimal minor, BigDecimal price) throws BitsoAPIException {
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
        parameters.put("book", book);
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

        String postResponse = sendBitsoPost(request, parameters);
        JSONObject payloadJSON = (JSONObject) getJSONPayload(postResponse);
        return Helpers.getString(payloadJSON, "oid");
    }

    public String[] cancelOrder(String... ordersIds) throws BitsoAPIException {
        String request = "/api/v3/orders";

        if (ordersIds == null || ordersIds.length == 0) {
            return null;
        }

        String ordersIdsParameters = processQueryParameters("-", ordersIds);
        request += "/" + ordersIdsParameters;
        log(request);

        String deleteResponse = sendBitsoDelete(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(deleteResponse);
        return Helpers.parseJSONArray(payloadJSON);
    }

    public Map<String, String> fundingDestination(String currencyParameter) throws BitsoAPIException {
        String request = "/api/v3/funding_destination";

        if (currencyParameter == null || currencyParameter.trim().length() == 0) {
            return null;
        }

        request += "?" + currencyParameter;

        String getResponse = sendBitsoGet(request);
        JSONObject payloadJSON = (JSONObject) getJSONPayload(getResponse);
        Map<String, String> fundingDestination = new HashMap<String, String>();
        fundingDestination.put("account_identifier_name",
                Helpers.getString(payloadJSON, "account_identifier_name"));
        fundingDestination.put("account_identifier", Helpers.getString(payloadJSON, "account_identifier"));
        return fundingDestination;
    }

    public BitsoWithdrawal bitcoinWithdrawal(BigDecimal amount, String address) throws BitsoAPIException {
        return currencyWithdrawal(BITCOIN, amount, address);
    }

    public BitsoWithdrawal etherWithdrawal(BigDecimal amount, String address) throws BitsoAPIException {
        return currencyWithdrawal(ETHER, amount, address);
    }

    public BitsoWithdrawal speiWithdrawal(BigDecimal amount, String recipientGivenNames,
            String recipientFamilyNames, String clabe, String notesReference, String numericReference)
            throws BitsoAPIException {
        String request = "/api/v3/spei_withdrawal";
        JSONObject parameters = new JSONObject();
        parameters.put("amount", amount.toString());
        parameters.put("recipient_given_names", recipientGivenNames);
        parameters.put("recipient_family_names", recipientFamilyNames);
        parameters.put("clabe", clabe);
        parameters.put("notes_ref", notesReference);
        parameters.put("numeric_ref", numericReference);
        String postResponse = sendBitsoPost(request, parameters);
        JSONObject payloadJSON = (JSONObject) getJSONPayload(postResponse);
        return new BitsoWithdrawal(payloadJSON);
    }

    public Map<String, String> getBanks() throws BitsoAPIException {
        String request = "/api/v3/mx_bank_codes";
        String getResponse = sendBitsoGet(request);
        JSONArray payloadJSON = (JSONArray) getJSONPayload(getResponse);
        Map<String, String> banks = new HashMap<String, String>();

        String currentBankCode = "";
        String currentBankName = "";
        JSONObject currentJSON = null;
        int totalElements = payloadJSON.length();
        for (int i = 0; i < totalElements; i++) {
            currentJSON = payloadJSON.getJSONObject(i);
            currentBankCode = Helpers.getString(currentJSON, "code");
            currentBankName = Helpers.getString(currentJSON, "name");
            banks.put(currentBankCode, currentBankName);
        }
        return banks;
    }

    public BitsoWithdrawal debitCardWithdrawal(BigDecimal amount, String recipientGivenNames,
            String recipientFamilyNames, String cardNumber, String bankCode) throws BitsoAPIException {
        String request = "/api/v3/debit_card_withdrawal";
        JSONObject parameters = new JSONObject();
        parameters.put("amount", amount.toString());
        parameters.put("recipient_given_names", recipientGivenNames);
        parameters.put("recipient_family_names", recipientFamilyNames);
        parameters.put("card_number", cardNumber);
        parameters.put("bank_code", bankCode);

        String postResponse = sendBitsoPost(request, parameters);
        JSONObject payloadJSON = (JSONObject) getJSONPayload(postResponse);
        return new BitsoWithdrawal(payloadJSON);
    }

    public BitsoWithdrawal phoneWithdrawal(BigDecimal amount, String recipientGivenNames,
            String recipientFamilyNames, String phoneNumber, String bankCode) throws BitsoAPIException {
        String request = "/api/v3/phone_withdrawal";
        JSONObject parameters = new JSONObject();
        parameters.put("amount", amount.toString());
        parameters.put("recipient_given_names", recipientGivenNames);
        parameters.put("recipient_family_names", recipientFamilyNames);
        parameters.put("phone_number", phoneNumber);
        parameters.put("bank_code", bankCode);

        String postResponse = sendBitsoPost(request, parameters);
        JSONObject payloadJSON = (JSONObject) getJSONPayload(postResponse);
        return new BitsoWithdrawal(payloadJSON);
    }

    private BitsoWithdrawal currencyWithdrawal(String currency, BigDecimal amount, String address)
            throws BitsoAPIException {
        String request = "/api/v3/" + currency + "_withdrawal";
        JSONObject parameters = new JSONObject();
        parameters.put("amount", amount.toString());
        parameters.put("address", address);

        String postResponse = sendBitsoPost(request, parameters);
        JSONObject payloadJSON = (JSONObject) getJSONPayload(postResponse);
        return new BitsoWithdrawal(payloadJSON);
    }

    public String getDepositAddress() throws BitsoAPIException {
        String postResponse = sendBitsoPost(baseUrl + "bitcoin_deposit_address");
        return quoteEliminator(postResponse);
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

    public String sendGet(String requestedURL) throws BitsoAPIException {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(baseUrl + requestedURL);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Android");
            return Helpers.convertInputStreamToString(connection.getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new BitsoAPIException(322, "Not a Valid URL", e);
        } catch (ProtocolException e) {
            e.printStackTrace();
            throw new BitsoAPIException(901, "Unsupported HTTP method", e);
        } catch (IOException e) {
            e.printStackTrace();
            return Helpers.convertInputStreamToString(connection.getErrorStream());
        }
    }

    public String sendBitsoGet(String requestPath) throws BitsoAPIException {
        return sendBitsoHttpRequest(requestPath, "GET");
    }

    private String sendBitsoHttpRequest(String requestPath, String method) throws BitsoAPIException {
        String requestURL = baseUrl + requestPath;
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(requestURL);
            connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("Authorization",
                    buildBitsoAuthHeader(requestPath, "GET", key, secret));
            connection.setRequestProperty("User-Agent", "Bitso-java-api");
            connection.setRequestMethod(method);
            return Helpers.convertInputStreamToString(connection.getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new BitsoAPIException(322, "Not a Valid URL", e);
        } catch (ProtocolException e) {
            e.printStackTrace();
            throw new BitsoAPIException(901, "Unsupported HTTP method", e);
        } catch (IOException e) {
            e.printStackTrace();
            return Helpers.convertInputStreamToString(connection.getErrorStream());
        }
    }

    private String sendBitsoDelete(String requestPath) throws BitsoAPIException {
        long nonce = System.currentTimeMillis() + System.currentTimeMillis();
        Entry<String, String> authHeader = buildBitsoAuthHeader(secret, key, nonce, "DELETE", requestPath,
                null);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put(authHeader.getKey(), authHeader.getValue());
        return client.sendDelete(baseUrl + requestPath, headers);
    }

    public String sendBitsoPost(String url) throws BitsoAPIException {
        return sendBitsoPost(url, null);
    }

    public String sendBitsoPost(String requestPath, JSONObject jsonPayload) throws BitsoAPIException {
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

        return client.sendPost(baseUrl + requestPath, jsonString, headers);
    }

    public String processQueryParameters(String separator, String... parameters) {
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

    public Object getJSONPayload(String jsonResponse) throws BitsoAPIException {
        JSONObject o = Helpers.parseJson(jsonResponse);

        if (o.has("error")) {
            JSONObject errorJson = o.getJSONObject("error");
            int errorCode = Helpers.getInt(errorJson, "code");
            String errorMessage = Helpers.getString(errorJson, "message");
            logError("Error response from server " + errorMessage);
            throw new BitsoAPIException(errorCode, errorMessage);
        }

        if (o.has("payload")) {
            return o.get("payload");
        } else {
            throw new BitsoAPIException(101, "Server response does not contain payload");
        }
    }
}
