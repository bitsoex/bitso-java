package com.bitso.websockets;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Observable;

import javax.net.ssl.SSLException;

import com.bitso.exceptions.BitsoWebSocketExeption;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;

public class BitsoWebSocket extends Observable{
    private final String URL = "wss://ws.bitso.com";
    private final int PORT = 443;

    private URI mUri;
    private SslContext mSslContext;
    private Channel mChannel;
    private EventLoopGroup mGroup;
    private String mMessageReceived;
    private Boolean mConnected;
    
    public BitsoWebSocket() throws SSLException,
        URISyntaxException{
        mUri = new URI(URL);
        mSslContext = SslContextBuilder.forClient().
                trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        mGroup = new NioEventLoopGroup();
        mMessageReceived = "";
        mConnected = Boolean.FALSE;
    }
    
    public void setConnected(Boolean connected){
        mConnected = connected;
        setChanged();
        notifyObservers(mConnected);
    }
    
    public void setMessageReceived(String messageReceived){
        mMessageReceived = messageReceived;
        setChanged();
        notifyObservers(mMessageReceived);
    }

    public void openConnection() throws InterruptedException{
        Bootstrap bootstrap = new Bootstrap();

        final WebSocketClientHandler handler =
                new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                mUri, WebSocketVersion.V08, null, false,
                                new DefaultHttpHeaders()));

        bootstrap.group(mGroup)
        .channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel socketChannel){
                        ChannelPipeline channelPipeline =
                                socketChannel.pipeline();
                        channelPipeline.addLast(mSslContext.newHandler(
                                socketChannel.alloc(),
                                mUri.getHost(),
                                PORT));
                        channelPipeline.addLast(new HttpClientCodec(),
                                new HttpObjectAggregator(8192),
                                handler);
                    }
                });

        mChannel = bootstrap.connect(mUri.getHost(), PORT).sync().channel();
        handler.handshakeFuture().sync();
        setConnected(Boolean.TRUE);
    }
    
    public void subscribeBitsoChannel(String channel){
        if(mConnected){
            String frameMessage = "{ \"action\": \"subscribe\", \"book\": \"btc_mxn\", \"type\": \""
                        + channel + "\" }";
            mChannel.writeAndFlush(new TextWebSocketFrame(frameMessage));
        }else{
            String message = "Subscription to any channel is not possible while web socket is not connected";
            throw new BitsoWebSocketExeption(message);
        }
    }
    
    public void closeConnection() throws InterruptedException{
        mChannel.writeAndFlush(new CloseWebSocketFrame());
        mChannel.closeFuture().sync();
        mGroup.shutdownGracefully();
    }

    public class WebSocketClientHandler extends ChannelInboundHandlerAdapter {
        private final WebSocketClientHandshaker mHandshaker;
        private ChannelPromise mHandshakeFuture;

        public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
            mHandshaker = handshaker;
        }

        public ChannelFuture handshakeFuture() {
            return mHandshakeFuture;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            mHandshakeFuture = ctx.newPromise();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx){
            mHandshaker.handshake(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {}

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg)
                throws Exception {
            Channel channel = ctx.channel();

            if(!mHandshaker.isHandshakeComplete()) {
                mHandshaker.finishHandshake(channel, (FullHttpResponse) msg);
                mHandshakeFuture.setSuccess();
                return;
            }

            if (msg instanceof FullHttpResponse) {
                FullHttpResponse response = (FullHttpResponse) msg;
                throw new Exception("Unexpected FullHttpResponse (getStatus=" + response.getStatus() + ", content="
                        + response.content().toString(CharsetUtil.UTF_8) + ')');
            }

            WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof TextWebSocketFrame) {
                TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                setMessageReceived(textFrame.text());
            }
            
            if(frame instanceof CloseWebSocketFrame){
                setConnected(Boolean.FALSE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            if (!mHandshakeFuture.isDone()) {
                mHandshakeFuture.setFailure(cause);
            }
            ctx.close();
        }
    }
}