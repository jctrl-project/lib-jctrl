/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.jctrl.uiserver;

import com.atea.ictrl.uiserver.widget.Widget;
import com.atea.ictrl.io.debugger.ICtrlDebugger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.atea.ictrl.uiserver.layout.Layout;
import com.atea.ictrl.uiserver.settings.Setting;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.PromiseCombiner;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WebSocket UI server accepts incoming clients on port 9000
 * @author MAARN
 */
public class UIServer {
    ObjectMapper mapper = new ObjectMapper();
    List<Widget> widgets;
    List<Setting> settings;
    ArrayList<UIEventListener> uiEventListeners = new ArrayList<>();
    private Timer heartbeatTimer;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
    
    Layout layout;
    
    /**
     *
     */
    public UIServer() {
        widgets = Collections.synchronizedList(new ArrayList<>());
        settings = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Fires up the WS-server and heartbeats etc.
     */
    public void startWebSocketService() {
        if (heartbeatTimer == null) {
            heartbeatTimer = new Timer();
            heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    //String message = heartbeatString.replace("2019-01-01 15:42:00", new Timestamp(System.currentTimeMillis()).toString());
                    
                    try {
                        String message = mapper.writeValueAsString(new Heartbeat());
                        broadcastMessage(message);
                    } catch (JsonProcessingException ex) {
                        Logger.getLogger(UIServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }, 30000, 60000);
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new BroadcastServerInitializer(UIServer.this));

            Channel ch = b.bind(9000).sync().channel();
            ch.closeFuture().sync();
        } catch (InterruptedException ex) {
            Logger.getLogger(UIServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        
    }

    /**
     * Send message to all connected clients.
     * @param message
     */
    public void broadcastMessage(String message) {
        PromiseCombiner promiseCombiner = new PromiseCombiner();
        WebSocketMessageHandler.getChannelGroup().stream()
                .forEach(c -> {
                    promiseCombiner.add(c.writeAndFlush(new TextWebSocketFrame(message)).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                ICtrlDebugger.out.println("Failed to write to channel: {} " + future.cause());
                            }
                        }
                    }));
                });
    }

    /**
     * Adds Widget w to list of widgets that will sync states
     * @param w
     */
    public void registerWidget(Widget w) {
        widgets.add(w);
    }

    /**
     * Updates the state of Widget w and sends out the new state to all connected clients.
     * @param w
     */
    public void updateWidget(Widget w) {
        try {
            broadcastMessage(mapper.writeValueAsString(w));
        } catch (JsonProcessingException ex) {
            ICtrlDebugger.out.println("Failed to update widget: " + ex);
        }
    }

    /**
     * Adds an eventlistener to the UIServer 
     * @param uiEv
     */
    public void addUIEventListner(UIEventListener uiEv) {
        uiEventListeners.add(uiEv);
    }

    /**
     * Removes the eventlistener from server.
     * @param uiEv
     */
    public void removeUIEventListener(UIEventListener uiEv) {
        uiEventListeners.remove(uiEv);
    }

    /**
     * Returns all registered widgets.
     * @return
     */
    public List<Widget> getWidgets() {
        return widgets;
    }
    
    /**
     * Sets the layout that will be sent out to clients upon opening WS-connection
     * @param layout
     */
    public void setLayout(Layout layout) {
        this.layout = layout;
    }
    
    /**
     * Returns the current layout.
     * @return
     */
    public Layout getLayout() {
        return this.layout;
    }
    
    /**
     * Adds a setting object
     * @param s
     */
    public void addSetting(Setting s){
        settings.add(s);
    }
    
    /**
     * Returns all registered settings
     * @return
     */
    public List<Setting> getSettings(){
        return settings;
    }
    
    /**
     * Overrideable method that runs when power on is received
     */
    public void handlePowerOn() {
        
    }
    
    /**
     * Overrideable method that runs when power off is received
     */
    public void handlePowerOff() {
        
    }
}
