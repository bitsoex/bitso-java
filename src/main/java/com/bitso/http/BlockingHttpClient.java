package com.bitso.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import com.bitso.exceptions.BitsoAPIException;

import com.bitso.helpers.Helpers;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class BlockingHttpClient {
    private boolean log = false;
    private long throttleMs = -1;
    private long lastCallTime = 0;

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
            throws BitsoAPIException {
        throttle();
        HttpsURLConnection connection = null;

        try {
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

    public String sendPost(String url, String body, HashMap<String, String> headers, Charset charset)
            throws ClientProtocolException, IOException {
        return sendPost(url, new StringEntity(body, charset), headers);
    }

    public String sendPost(String url, byte[] body, HashMap<String, String> headers)
            throws IOException {
        return sendPost(url, new ByteArrayEntity(body, 0, body.length, ContentType.APPLICATION_JSON), headers);
    }

    private String sendPost(String url, AbstractHttpEntity body, HashMap<String, String> headers)
            throws IOException {
        throttle();

        HttpPost postRequest = new HttpPost(url);
        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                postRequest.addHeader(e.getKey(), e.getValue());
            }
        }

        postRequest.setEntity(body);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            CloseableHttpResponse response = client.execute(postRequest);
            return Helpers.convertInputStreamToString(response.getEntity().getContent());
        }
    }

    public String sendDelete(String url, Map<String, String> headers) throws BitsoAPIException {
        throttle();
        HttpDelete deleteURL = new HttpDelete(url);

        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                deleteURL.addHeader(e.getKey(), e.getValue());
            }
        }

        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = closeableHttpClient.execute(deleteURL);
            return Helpers.convertInputStreamToString(response.getEntity().getContent());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            throw new BitsoAPIException(901, "Usupported HTTP method", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BitsoAPIException(101, "Connection Aborted", e);
        }
    }
}
