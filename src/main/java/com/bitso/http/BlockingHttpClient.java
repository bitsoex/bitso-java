package com.bitso.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

public class BlockingHttpClient {
    private boolean log = false;
    private long throttleMs = -1;
    private long lastCallTime = 0;

    public BlockingHttpClient() {
        this(false);
    }

    public BlockingHttpClient(boolean log) {
        this(log, -1);
    }

    public BlockingHttpClient(boolean log, long throttleMs) {
        this.log = log;
        this.throttleMs = throttleMs;
    }

    private void log(Object msg) {
        if (log) System.out.println(msg);
    }

    private void throttle() {
        if (throttleMs <= 0) {
            return;
        }

        long time = System.currentTimeMillis();
        long diff = time - lastCallTime;

        try {
            if (diff < throttleMs) {
                long throttleDifference = throttleMs - diff;
                log("Throttling request for " + throttleDifference);
                Thread.sleep(throttleDifference);
            }
            lastCallTime = System.currentTimeMillis();
        } catch (InterruptedException e) {
            log("Error executing throttle");
        }
    }

    public String sendPost(String url, String body, HashMap<String, String> headers)
            throws MalformedURLException, ProtocolException, IOException {
        throttle();

        URL requestURL = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) requestURL.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Bitso-API");

        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                connection.setRequestProperty(e.getKey(), e.getValue());
            }
        }

        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(body);
        wr.flush();
        wr.close();

        String response = getStringFromStream(connection.getInputStream());
        log(response);

        return response;
    }

    public String sendPost(String url, String body, HashMap<String, String> headers, Charset charset)
            throws ClientProtocolException, IOException {
        return sendPost(url, new StringEntity(body, charset), headers);
    }

    public String sendPost(String url, byte[] body, HashMap<String, String> headers)
            throws ClientProtocolException, IOException {
        return sendPost(url, new ByteArrayEntity(body), headers);
    }

    private String sendPost(String url, AbstractHttpEntity body, HashMap<String, String> headers)
            throws ClientProtocolException, IOException {
        throttle();

        HttpPost postRequest = new HttpPost(url);
        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                postRequest.addHeader(e.getKey(), e.getValue());
            }
        }

        postRequest.setEntity(body);

        CloseableHttpResponse closeableHttpResponse = HttpClients.createDefault().execute(postRequest);
        String response = getStringFromStream(closeableHttpResponse.getEntity().getContent());

        return response;
    }

    public String sendDelete(String url, HashMap<String, String> headers)
            throws ClientProtocolException, IOException {
        throttle();
        HttpDelete deleteRequest = new HttpDelete(url);

        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                deleteRequest.addHeader(e.getKey(), e.getValue());
            }
        }

        CloseableHttpResponse closeableHttpResponse = HttpClients.createDefault().execute(deleteRequest);
        String response = getStringFromStream(closeableHttpResponse.getEntity().getContent());

        return response;
    }

    public String getStringFromStream(InputStream inputStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuffer stringBuffer = new StringBuffer();
        String input = null;

        while ((input = bufferedReader.readLine()) != null) {
            stringBuffer.append(input);
        }

        bufferedReader.close();
        return stringBuffer.toString();
    }
}
