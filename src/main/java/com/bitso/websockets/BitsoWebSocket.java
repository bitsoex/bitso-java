package com.bitso.websockets;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Observable;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
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
    private final String HOST = "ws.bitso.com";
    private final String URL = "wss://ws.bitso.com";
    private final int PORT = 443;

    private URI mUri;
    private SslContext mSslContext;
    private Channel mChannel;
    private EventLoopGroup mGroup;
    private Channels[] mBitsoChannels;
    private String mMessageReceived;
    
    public BitsoWebSocket(Channels[] bitsoChannels) throws SSLException,
        URISyntaxException{
        mUri = new URI(URL);
        mSslContext = SslContextBuilder.forClient().
                trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        mGroup = new NioEventLoopGroup();
        mBitsoChannels = bitsoChannels;
        mMessageReceived = "";
    }

    public void open() throws InterruptedException{
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
                        channelPipeline.addLast(mSslContext.newHandler(socketChannel.alloc(),
                                mUri.getHost(),
                                PORT));
                        channelPipeline.addLast(new HttpClientCodec(),
                                new HttpObjectAggregator(8192),
                                handler);
                    }
                });

        System.out.println("WebSocket Client connecting");
        mChannel = bootstrap.connect(HOST, PORT).sync().channel();
        handler.handshakeFuture().sync();
        System.out.println("WebSocket Client connected");
    }
    
    public void connectBitsoChannels(){
        int totalBitsoChannels = mBitsoChannels.length;
        String frameMessage = "";
        for (int i=0; i<totalBitsoChannels; i++) {
            frameMessage = "{ \"action\": \"subscribe\", \"book\": \"btc_mxn\", \"type\": \""
                    + mBitsoChannels[i].toString() + "\" }";
            mChannel.writeAndFlush(new TextWebSocketFrame(frameMessage));
        }
    }
    
    public void connect() throws InterruptedException{
        open();
        connectBitsoChannels();
    }

    public void close() throws InterruptedException{
        System.out.println("WebSocket Client sending close");
        mChannel.writeAndFlush(new CloseWebSocketFrame());
        mChannel.closeFuture().sync();
        mGroup.shutdownGracefully();
    }

    public void setMessageReceived(String messageReceived){
        mMessageReceived = messageReceived;
        setChanged();
        notifyObservers(mMessageReceived);
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
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("WebSocket Client disconnected!");
        }

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