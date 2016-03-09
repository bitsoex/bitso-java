package com.bitso.websockets;

import java.util.Observable;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebsocketVersion;

public class BitsoWebSocket extends Observable {

    Vertx vertx;
    String host = "ws.bitso.com";
    String uri = "";
    WebsocketVersion v = WebsocketVersion.V00;
    private Buffer textBuf = Buffer.buffer();
    Channels[] channels;
    HttpClient client;

    public BitsoWebSocket(Channels[] channels) {
        vertx = Vertx.vertx();
        this.channels = channels;
        HttpClientOptions httpOptions = new HttpClientOptions().setSsl(true).setTrustAll(true)
                .setVerifyHost(false);
        client = vertx.createHttpClient(httpOptions);
    }

    public void connect() {
        connectSocket();
    }

    public void disconnect() {
        client.close();
    }

    private void connectSocket() {

        client.websocket(443, host, uri, null, v, websocket -> {
            for (Channels chan : channels) {
                String message = "{ \"action\": \"subscribe\", \"book\": \"btc_mxn\", \"type\": \""
                        + chan.toString() + "\" }";
                websocket.writeFinalTextFrame(message);
            }

            websocket.closeHandler(vd -> {
                System.err.println("Websocket Error, Disonnected!...Reconnecting");
                connectSocket();
            });
            websocket.frameHandler(frame -> {
                if (frame.isText()) {
                    // its the first frame
                    textBuf.appendString(frame.textData());
                    if (frame.isFinal()) {
                        super.setChanged();
                        super.notifyObservers(textBuf.toString());
                        textBuf = Buffer.buffer();
                    }
                }

            });

        } , error -> {
            System.err.println("Websocket Error, Disonnected!...Reconnecting;");
            connectSocket();
        });
    }

}
