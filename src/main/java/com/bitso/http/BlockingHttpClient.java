package com.bitso.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

public class BlockingHttpClient {

    private static final String USER_AGENT = "Mozilla/5.0"; // TODO: maybe do something more
                                                            // rational here
    private boolean log = false;
    private long throttleMs = -1;
    private long lastCallTime = 0;

    public BlockingHttpClient() {
        new BlockingHttpClient(false);
    }

    public BlockingHttpClient(boolean log) {
        new BlockingHttpClient(log, -1);
    }

    public BlockingHttpClient(boolean log, long throttleMs) {
        this.log = log;
        this.throttleMs = throttleMs;
    }

    private void log(Object msg) {
        if (log) System.out.println(msg);
    }

    private void throttle() throws InterruptedException {
        if (throttleMs <= 0) {
            return;
        }
        long time = System.currentTimeMillis();
        long diff = time - lastCallTime;

        if (diff < throttleMs) {
            log("Throttling request for " + (throttleMs - diff));
            Thread.sleep(throttleMs - diff);
        }
        lastCallTime = System.currentTimeMillis();
    }

    public String get(String url, HashMap<String, String> headers) throws Exception {
        throttle();
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");

        // add request headers
        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                con.setRequestProperty(e.getKey(), e.getValue());
            }
            log("\nHeaders are \n" + headers.toString());
        }
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        log("\nSending 'GET' request to URL : " + url);
        log("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public String sendGet(String url, HashMap<String, String> headers) throws Exception {
        throttle();
        HttpGet getRequest = new HttpGet(url);

        // add request headers
        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                getRequest.addHeader(e.getKey(), e.getValue());
            }
            log("\nHeaders are \n" + headers.toString());
        }

        log("\nSending 'GET' request to URL : " + url);
        CloseableHttpResponse response = HttpClients.createDefault().execute(getRequest);

        log("Response Code : " + response.getStatusLine().getStatusCode());
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        String inputLine;
        StringBuffer responseBody = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            responseBody.append(inputLine);
        }
        in.close();
        log(response);
        log(responseBody);

        return responseBody.toString();
    }

    public String get(String url) throws Exception {
        return get(url, null);
    }

    public String sendPost(String url, String body, HashMap<String, String> headers) throws Exception {
        throttle();
        HttpPost postRequest = new HttpPost(url);
        // add request headers
        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                postRequest.addHeader(e.getKey(), e.getValue());
            }
            log("\nHeaders are \n" + headers.toString());
        }

        postRequest.setEntity(new StringEntity(body));
        log("\nSending 'POST' request to URL : " + url);
        log("Post parameters : " + body);

        CloseableHttpResponse response = HttpClients.createDefault().execute(postRequest);
        log("Response Code : " + response.getStatusLine().getStatusCode());
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        String inputLine;
        StringBuffer responseBody = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            responseBody.append(inputLine);
        }
        in.close();
        log(response);
        log(responseBody);

        return responseBody.toString();
    }

    public String sendDelete(String url, HashMap<String, String> headers) throws Exception {
        throttle();
        HttpDelete deleteRequest = new HttpDelete(url);
        // add request headers
        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                deleteRequest.addHeader(e.getKey(), e.getValue());
            }
            log("\nHeaders are \n" + headers.toString());
        }

        log("\nSending 'DELETE' request to URL : " + url);
        CloseableHttpResponse response = HttpClients.createDefault().execute(deleteRequest);
        log("Response Code : " + response.getStatusLine().getStatusCode());
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        String inputLine;
        StringBuffer responseBody = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            responseBody.append(inputLine);
        }
        in.close();
        log(response);
        log(responseBody);

        return responseBody.toString();
    }

}
