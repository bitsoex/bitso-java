package com.bitso;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import com.bitso.exchange.BookOrder;
import com.bitso.exchange.OrderBook;
import com.bitso.exchange.BookOrder.STATUS;
import com.bitso.exchange.BookOrder.TYPE;
import com.bitso.http.BlockingHttpClient;

public class Bitso {

    private static final String BITSO_BASE_URL = "https://api.bitso.com/v2/";
    private static final long THROTTLE_MS = 1000;

    private String key;
    private String secret;
    private String clientId;

    private BlockingHttpClient client = new BlockingHttpClient(false, THROTTLE_MS);

    public Bitso(String key, String secret, String clientId) {
        this.key = key;
        this.secret = secret;
        this.clientId = clientId;
    }

    public OrderBook getOrderBook() throws Exception {
        return new BitsoOrderBook(client.get(BITSO_BASE_URL + "order_book"));
    }

    public BitsoBalance getBalance() throws Exception {
        return new BitsoBalance(sendBitsoPost(BITSO_BASE_URL + "balance"));
    }

    public BitsoOpenOrders getOpenOrders() throws Exception {
        return new BitsoOpenOrders(sendBitsoPost(BITSO_BASE_URL + "open_orders"));
    }

    public BitsoTicker getTicker() throws Exception {
        return new BitsoTicker(client.get(BITSO_BASE_URL + "ticker"));
    }

    public BitsoLookupOrders getLookupOrders(String orderId) throws Exception {
        // TODO: idea of when sending just one ID
        // ArrayList<String> ids = new ArrayList<String>(1);
        // ids.add(id);
        // return getLookupOrders(ids);
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("id", orderId);
        return new BitsoLookupOrders(sendBitsoPost(BITSO_BASE_URL + "lookup_order", body));
    }

    public boolean cancelOrder(String orderId) throws Exception {
        System.out.println("Attempting to cancel order: " + orderId);
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("id", orderId);
        String ret = sendBitsoPost(BITSO_BASE_URL + "cancel_order", body);
        if (ret.equals("\"true\"")) {
            System.out.println("Cancelled Order: " + orderId);
            return true;
        }
        System.err.println("Unable to cancel order: " + orderId);
        System.err.println(ret);
        return false;
    }

    public BigDecimal placeBuyMarketOrder(BigDecimal mxnAmountToSpend) throws Exception {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("amount", mxnAmountToSpend.toPlainString());
        System.out.println("Placing the following buy maket order: " + body);
        String json = sendBitsoPost(BITSO_BASE_URL + "buy", body);
        JSONObject o;
        try {
            o = new JSONObject(json);
        } catch (JSONException e) {
            System.err.println("Unable to parse json: " + json);
            e.printStackTrace();
            return null;
        }
        return new BigDecimal(o.getString("amount"));
    }

    public BigDecimal placeSellMarketOrder(BigDecimal btcAmountToSpend) throws Exception {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("amount", btcAmountToSpend.toPlainString());
        System.out.println("Placing the following sell maket order: " + body);
        String json = sendBitsoPost(BITSO_BASE_URL + "sell", body);
        JSONObject o;
        try {
            o = new JSONObject(json);
        } catch (JSONException e) {
            System.err.println("Unable to parse json: " + json);
            e.printStackTrace();
            return null;
        }
        return new BigDecimal(o.getString("amount"));
    }

    public BookOrder placeBuyLimitOrder(BigDecimal price, BigDecimal amount) throws Exception {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("amount", amount.toPlainString());
        body.put("price", price.toPlainString());
        System.out.println("Placing the following buy order: " + body);
        String json = sendBitsoPost(BITSO_BASE_URL + "buy", body);
        return processBookOrderJSON(json);
    }

    public BookOrder placeSellLimitOrder(BigDecimal price, BigDecimal amount) throws Exception {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("amount", amount.toPlainString());
        body.put("price", price.toPlainString());
        System.out.println("Placing the following sell order: " + body);
        String json = sendBitsoPost(BITSO_BASE_URL + "sell", body);
        return processBookOrderJSON(json);
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
        BigDecimal price = null, amount = null;
        if (o.has("price")) {
            price = new BigDecimal(o.getString("price"));
        }
        if (o.has("amount")) {
            amount = new BigDecimal(o.getString("amount"));
        }
        if (price == null || amount == null) {
            return null;
        }
        BookOrder order = new BookOrder(price, amount);
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

    public boolean withdrawBTC(String address, BigDecimal amount) throws Exception {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("amount", amount.toPlainString());
        body.put("address", address);
        System.out.println("Executing the following BTC withdrawal: " + body);
        String ret = sendBitsoPost(BITSO_BASE_URL + "bitcoin_withdrawal", body);
        System.out.println(ret);
        if (ret.equals("\"ok\"")) {
            System.out.println("BTC withdrawal executed");
            return true;
        }
        System.err.println("Unable to execute BTC withdrawal");
        System.err.println(ret);
        return false;
    }

    public boolean withdrawMXN(BigDecimal amount, String recipientGivenName,
            String recipientFamilyName, String clabe, String notesRef, String numericRef)
            throws Exception {
        if (amount.scale() > 2) {
            System.err.println("MXN withdrawal has incorrect scale " + amount);
            return false;
        }
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("amount", amount.toPlainString());
        body.put("recipient_given_names", recipientGivenName);
        body.put("recipient_family_names", recipientFamilyName);
        body.put("clabe", clabe);
        body.put("notes_ref", notesRef);
        body.put("numeric_ref", numericRef);
        System.out.println("Executing the following withdrawal: " + body);
        String ret = sendBitsoPost(BITSO_BASE_URL + "spei_withdrawal", body);
        if (ret.equals("\"ok\"")) {
            System.out.println("Withdrawal executed");
            return true;
        }
        System.err.println("Unable to execute MXN withdrawal");
        System.err.println(ret);
        return false;
    }

    public String getDepositAddress() throws Exception {
        return quoteEliminator(sendBitsoPost(BITSO_BASE_URL + "bitcoin_deposit_address"));
    }

    private static String quoteEliminator(String input) {
        if (input == null) {
            throw new IllegalStateException("input to quoteEliminator cannot be null");
        }
        int length = input.length();
        if (input.charAt(0) != '"' || input.charAt(length - 1) != '"') {
            throw new IllegalStateException("input to quoteEliminator must begin and end with '\"'");
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

    private String sendBitsoPost(String url, HashMap<String, String> bodyExtras) throws Exception {
        long nonce = System.currentTimeMillis();
        String message = nonce + key + clientId;
        String signature = signRequest(secret, message);

        JSONObject json = new JSONObject();
        json.put("key", key);
        json.put("nonce", nonce);
        json.put("signature", signature);

        if (bodyExtras != null) {
            for (Entry<String, String> e : bodyExtras.entrySet()) {
                json.put(e.getKey(), e.getValue());
            }
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        return client.sendPost(url, json.toString(), headers);
    }

    private String sendBitsoPost(String url) throws Exception {
        return sendBitsoPost(url, null);
    }
}
