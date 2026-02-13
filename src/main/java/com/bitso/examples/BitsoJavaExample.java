package com.bitso.examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
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
        signature = String.format("%0" + (arrayOfByte.length << 1) + "x", new Object[] { localBigInteger });

        // Build the request parameters
        JSONObject o = new JSONObject();
        o.put("key", bitsoKey);
        o.put("nonce", nonce);
        o.put("signature", signature);
        String body = o.toString();
        String url = "https://api.bitso.com/v2/balance";

        // Send request
        HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader("Content-Type", "application/json");
        postRequest.setEntity(new StringEntity(body));

        CloseableHttpResponse response = HttpClients.createDefault().execute(postRequest);
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        String inputLine;
        StringBuffer responseBody = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            responseBody.append(inputLine);
        }
        in.close();

        System.out.println(responseBody.toString());
    }
}
