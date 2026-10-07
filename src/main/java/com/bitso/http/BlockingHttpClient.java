package com.bitso.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import com.bitso.exceptions.BitsoAPIException;

import com.bitso.helpers.Helpers;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public class BlockingHttpClient {
    public static final String CONTENT_TYPE = "Content-Type";
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
            Thread.currentThread().interrupt();
        }
    }

    public String sendPost(String url, String body, Map<String, String> headers)
            throws BitsoAPIException {
        return send(url, "POST", body, headers);
    }

    public String sendPatch(String url, String body, Map<String, String> headers)
            throws BitsoAPIException {
        return send(url, "PATCH", body, headers);
    }

    private String send(String url, String method, String body, Map<String, String> headers) {
        throttle();
        HttpsURLConnection connection = null;

        try {
            URL requestURL = new URL(url);
            connection = (HttpsURLConnection) requestURL.openConnection();
            connection.setRequestMethod(method);
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
            throw new BitsoAPIException(322, "Not a Valid URL", e);
        } catch (ProtocolException e) {
            throw new BitsoAPIException(901, "Unsupported HTTP method", e);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return Helpers.convertInputStreamToString(connection.getErrorStream());
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

        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse response = closeableHttpClient.execute(deleteURL)) {
            return Helpers.convertInputStreamToString(response.getEntity().getContent());
        } catch (ClientProtocolException e) {
            throw new BitsoAPIException(901, "Usupported HTTP method", e);
        } catch (IOException e) {
            throw new BitsoAPIException(101, "Connection Aborted", e);
        }
    }
}
