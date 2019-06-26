/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.jctrl.uiserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

/**
 * @author ameya
 */
public class BroadcastServerInitializer extends ChannelInitializer<SocketChannel> {
    private final UIServer uiServer;
    public BroadcastServerInitializer(UIServer uiServer) {
        this.uiServer = uiServer;
    }
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new WebSocketServerCompressionHandler())
                .addLast(new WebSocketServerProtocolHandler("/", null, true))
                .addLast(new WebSocketMessageHandler(uiServer));
    }
}
