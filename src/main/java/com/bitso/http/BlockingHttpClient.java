package com.bitso.http;

import java.io.DataOutputStream;
import java.io.IOException;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.bitso.exceptions.BitsoNullException;
import com.bitso.helpers.Helpers;

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
            throws BitsoNullException, IOException {
        throttle();
        HttpsURLConnection connection = null;
        URL requestURL = new URL(url);
        connection = (HttpsURLConnection) requestURL.openConnection();
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

        return Helpers.convertInputStreamToString(connection.getInputStream());
    }

    public String sendPost(String url, String body, HashMap<String, String> headers, Charset charset)
            throws ClientProtocolException, IllegalStateException, IOException, BitsoNullException {
        return sendPost(url, new StringEntity(body, charset), headers);
    }

    public String sendPost(String url, byte[] body, HashMap<String, String> headers)
            throws ClientProtocolException, IllegalStateException, IOException, BitsoNullException {
        return sendPost(url, new ByteArrayEntity(body), headers);
    }

    private String sendPost(String url, AbstractHttpEntity body, HashMap<String, String> headers)
            throws ClientProtocolException, IOException, IllegalStateException, BitsoNullException {
        throttle();

        HttpPost postRequest = new HttpPost(url);
        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                postRequest.addHeader(e.getKey(), e.getValue());
            }
        }

        postRequest.setEntity(body);

        CloseableHttpResponse closeableHttpResponse = HttpClients.createDefault().execute(postRequest);
        String response = Helpers.convertInputStreamToString(closeableHttpResponse.getEntity().getContent());

        return response;
    }

    public String sendDelete(String url, HashMap<String, String> headers)
            throws ClientProtocolException, IOException, IllegalStateException, BitsoNullException {
        throttle();
        HttpDelete deleteURL = new HttpDelete(url);

        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                deleteURL.addHeader(e.getKey(), e.getValue());
            }
        }

        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;

        response = closeableHttpClient.execute(deleteURL);
        return Helpers.convertInputStreamToString(response.getEntity().getContent());
    }
}
