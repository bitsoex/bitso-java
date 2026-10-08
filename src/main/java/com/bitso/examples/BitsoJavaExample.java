package com.bitso.examples;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

public class BitsoJavaExample {
    public static void main(String[] args) throws Exception {
        String bitsoKey = "";
        String bitsoSecret = "";
        String bitsoClientId = "";
        long nonce = System.currentTimeMillis();

        // Create the signature
        String message = nonce + bitsoKey + bitsoClientId;
        String signature = "";
        byte[] secretBytes = bitsoSecret.getBytes();
        SecretKeySpec localMac = new SecretKeySpec(secretBytes, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(localMac);
        byte[] arrayOfByte = mac.doFinal(message.getBytes());
        BigInteger localBigInteger = new BigInteger(1, arrayOfByte);
        signature = String.format("%0" + (arrayOfByte.length << 1) + "x", localBigInteger);

        // Build the request parameters
        JSONObject o = new JSONObject();
        o.put("key", bitsoKey);
        o.put("nonce", nonce);
        o.put("signature", signature);
        String body = o.toString();
        String url = "https://api.bitso.com/v2/balance";

        // Send request
        var request = HttpRequest.newBuilder().uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        var client = HttpClient.newHttpClient();
        String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        System.out.println(response);
    }
}
