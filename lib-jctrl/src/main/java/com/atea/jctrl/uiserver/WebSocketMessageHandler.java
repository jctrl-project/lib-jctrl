/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.jctrl.uiserver;

import com.atea.ictrl.uiserver.widget.Widget;
import com.atea.ictrl.io.debugger.ICtrlDebugger;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.PromiseCombiner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author ameya
 */
public class WebSocketMessageHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final UIServer uiServer;
    private final ObjectMapper mapper = new ObjectMapper();
    public WebSocketMessageHandler(UIServer uiServer) {
        super(false);
        this.uiServer = uiServer;
    }

   

    private static final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static ChannelGroup getChannelGroup() {
        return allChannels;
    }
    private void broadcastMessage(String message) {
        PromiseCombiner promiseCombiner = new PromiseCombiner();
        allChannels.stream()
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
    private void sendMessage(ChannelHandlerContext ctx, String message) {
        PromiseCombiner promiseCombiner = new PromiseCombiner();
        allChannels.stream()
                .filter(c -> c == ctx.channel())
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
    private Widget getWidget(String id) {
        for(Widget w:uiServer.getWidgets()) {
            if(w.getId().equals(id)) {
                return w;
            }
        }
        return null;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            final String text = ((TextWebSocketFrame) frame).text();
            frame.release();
          //  ICtrlDebugger.out.println("Received text frame {} " + text);
                
            JSONObject jo = getJSONObject(text);
            if(jo == null) {
                return;
            }
            
            if (jo.has("message_type")) {
                
                String messageType = jo.optString("message_type");
            
                switch(messageType) {
                    case "action":
                        ICtrlDebugger.out.println("action: " + text);
                        
                        String value = jo.optString("value");
                        String component_id = jo.optString("component_id");
                        
                        Widget w = getWidget(component_id);
                        
                        if (w == null) {
                            ICtrlDebugger.err.println("RECEIVED ACTION WITH UNKNOWN WIDGET ID: " + component_id);
                           
                            break;
                        }
                        
                        w.setValue(value);
                        broadcastMessage(mapper.writeValueAsString(w));
                        uiServer.uiEventListeners.forEach((uiEv) -> {
                            uiEv.UIWidgetEvent(w, ctx);
                        });
                        
                        break;
                    case "update_request":
                        ICtrlDebugger.out.println("update_request: " + text);
                        
                        String objStr = mapper.writeValueAsString(uiServer.getWidgets());   
                        
                        String layoutStr = mapper.writeValueAsString(uiServer.getLayout());
                        
                        String settingsStr = mapper.writeValueAsString(uiServer.getSettings());
                        
                        JSONArray tmpArr = new JSONArray(objStr);
                        JSONObject tmpObj = new JSONObject();
                        tmpObj.put("widgets", tmpArr);
                        
                        JSONObject layoutObj = new JSONObject(layoutStr);
                        tmpObj.put("layout", layoutObj);
                        
                        JSONArray settingsArr = new JSONArray(settingsStr);
                        tmpObj.put("settings", settingsArr);
                        
                        tmpObj.put("message_type", "init_response");
                        
                        sendMessage(ctx, tmpObj.toString());
                        
                        break;
                    case "init":
                        // OLD
                        ICtrlDebugger.out.println("init");
                        sendMessage(ctx, text);
                        break;
                    case "heartbeat":
                        ICtrlDebugger.out.println("heartbeat: " + text);
                        break;
                    case "power_off":
                        ICtrlDebugger.out.println("Received power_off");
                        
                        uiServer.handlePowerOff();
                        
                        JSONObject obj = new JSONObject();                    
                        obj.put("message_type", "power_off");
                        broadcastMessage(obj.toString());
                        break;
                    case "power_on":
                        ICtrlDebugger.out.println("Received power_on");
                        uiServer.handlePowerOn();
                        break;
                    default:
                        ICtrlDebugger.out.println("Other: " + text);
                        break;
                }
                
            }
            /*
            if(rootNode.path("message_type").asText().equals("update_request")) { // Update request, send widget list to client
                JSONArray wArray = new JSONArray();
                wArray.put(uiServer.getWidgets());
                
                sendMessage(ctx, mapper.writeValueAsString(uiServer.getWidgets()));
            } else if(rootNode.path("message_type").asText().equals("action")) { 
                Widget w = getWidget(rootNode.path("id").asText());
                w.setValue(rootNode.path("value").asText());
                broadcastMessage(mapper.writeValueAsString(w));
                for(UIEventListener uiEv : uiServer.uiEventListeners) {
                    uiEv.UIWidgetEvent(w);
                }
            }
           
            */
            

            
           

        } else {
            throw new UnsupportedOperationException("Invalid websocket frame received");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Adding new channel {} to list of channels " + ctx.channel().remoteAddress());
        allChannels.add(ctx.channel());
        uiServer.uiEventListeners.forEach((uiEv) -> {
            uiEv.UIConnected(ctx);
        });
    }
    

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Removing channel {} to list of channels " + ctx.channel().remoteAddress());
        allChannels.remove(ctx.channel());
        uiServer.uiEventListeners.forEach((uiEv) -> {
            uiEv.UIDisconnected(ctx);
        });
    }
    public JSONObject getJSONObject(String test) {
        try {
            return new JSONObject(test);
        } catch (JSONException ex) {                       
            return null;            
        }        
    }
    public JSONArray getJSONArray(String test) {
        try {
            return new JSONArray(test);
        } catch (JSONException ex) {
            return null;
        }
    }
}
