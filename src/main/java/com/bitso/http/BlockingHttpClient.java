package com.bitso.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

public class BlockingHttpClient {

    private static final String USER_AGENT = "Mozilla/5.0"; // TODO: maybe do something more
                                                            // rational here
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
                log("Throttling request for " + (throttleMs - diff));
                Thread.sleep(throttleMs - diff);
            }
            lastCallTime = System.currentTimeMillis();
        } catch (InterruptedException e) {
            log("Error executing throttle");
        }
    }

    public String sendPost(String url, String body, HashMap<String, String> headers) {
        throttle();
        JSONObject errorJson = new JSONObject();
        errorJson.put("success", Boolean.FALSE.toString());
        JSONObject errorDescription = new JSONObject();

        try {
            URL requestURL = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) requestURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Bitso-API");

            // Add request headers
            if (headers != null) {
                for (Entry<String, String> e : headers.entrySet()) {
                    con.setRequestProperty(e.getKey(), e.getValue());
                }
                log("\nHeaders are \n" + headers.toString());
            }

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String input;
            StringBuffer stringBuffer = new StringBuffer();

            while ((input = bufferedReader.readLine()) != null) {
                stringBuffer.append(input);
            }
            bufferedReader.close();
            return stringBuffer.toString();
        } catch (MalformedURLException e) {
            errorDescription.put("message", "Not a Valid URL");
            errorDescription.put("code", 0322);
        } catch (ProtocolException e) {
            errorDescription.put("message", e.toString());
            errorDescription.put("code", 0101);
        } catch (IOException e) {
            errorDescription.put("message", e.toString());
            errorDescription.put("code", 0101);
        }

        errorJson.put("error", errorDescription);
        return errorJson.toString();
    }

    public String sendPost(String url, String body, HashMap<String, String> headers, Charset charset)
            throws Exception {
        return sendPost(url, new StringEntity(body, charset), headers);
    }

    public String sendPost(String url, byte[] body, HashMap<String, String> headers) throws Exception {
        return sendPost(url, new ByteArrayEntity(body), headers);
    }

    private String sendPost(String url, AbstractHttpEntity body, HashMap<String, String> headers)
            throws Exception {
        throttle();

        HttpPost postRequest = new HttpPost(url);
        // add request headers
        if (headers != null) {
            for (Entry<String, String> e : headers.entrySet()) {
                postRequest.addHeader(e.getKey(), e.getValue());
            }
            log("\nHeaders are \n" + headers.toString());
        }

        postRequest.setEntity(body);

        log("\nSending 'POST' request to URL : " + url);
        log("Post parameters : " + body.getContent());

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
