/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.io.debugger;

import com.atea.ictrl.devices.Device;
import com.atea.ictrl.debugger.tools.CommandAddDevice;
import com.atea.ictrl.debugger.tools.CommandSendToDevice;
import com.atea.ictrl.debugger.tools.DebugMessage;
import com.atea.ictrl.debugger.tools.MessageParser;
import com.atea.ictrl.io.DeviceConnection;
import com.atea.ictrl.io.HeartbeatHandler;
import com.atea.ictrl.io.TimeoutHandler;
import com.atea.ictrl.io.network.TelnetServerInitializer;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Martin
 */
public enum ICtrlDebugger {

    /**
     *
     */
    INSTANCE;
    static ChannelGroup clients = new DefaultChannelGroup("DebuggerChannels", GlobalEventExecutor.INSTANCE);
    static EventLoopGroup group = new NioEventLoopGroup();
    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();
    /**
     *
     */
    public static DebuggerStream out = new DebuggerStream(DebuggerStream.ModeOUT, clients);

    /**
     *
     */
    public static DebuggerStream err = new DebuggerStream(DebuggerStream.ModeERR, clients);

    /**
     *
     */
    public static DebuggerStream log = new DebuggerStream(DebuggerStream.ModeLOG, clients);
    
    private static final String delimiter = System.getProperty("line.separator");
    private static final Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static long lastPrintOut = 0;
    private static long lastLogPrintOut = 0;
    private static boolean debugMode = false;
    private static HeartbeatHandler heartbeatHandler = new HeartbeatHandler();
    private static TimeoutHandler timeoutHandler = new TimeoutHandler(20);
    static HashMap<String, DeviceConnection> devices = new HashMap<>();

    /*
     * Netty Server
     */
    private static ServerBootstrap server;    

    
    static boolean clientsEmpty = true;

    /**
     * Will execute debugging engine.
     */
    public static void getInstance() {
        //execute();
        try {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    try {
                        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
                        EventLoopGroup workerGroup = new NioEventLoopGroup();
                        DebugServerHandler serverHandler = new DebugServerHandler();
                        ServerBootstrap serverBootstrap = new ServerBootstrap();
                        //serverBootstrap.group(group);
                        serverBootstrap.group(bossGroup, workerGroup)
                                .channel(NioServerSocketChannel.class)                                
                                .childHandler(serverHandler);

                        serverBootstrap.localAddress(new InetSocketAddress("localhost", 9999));

                        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                
                                
                                // Add the text line codec combination first,
                                pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                                // the encoder and decoder are static as these are sharable
                                pipeline.addLast(DECODER);
                                pipeline.addLast(ENCODER);
                                pipeline.addLast("idleStateHandler", new TimeoutHandler(20));
                                pipeline.addLast("heartbeatHandler", new HeartbeatHandler()); // heartbeat
                                // and then business logic.
                                socketChannel.pipeline().addLast(serverHandler);
                            }
                        });
                        ChannelFuture channelFuture = serverBootstrap.bind().sync();
                        channelFuture.channel().closeFuture().sync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            group.shutdownGracefully().sync();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ICtrlDebugger.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }


                }
            }, 0);
        } catch (Exception ex) {
            System.err.println("Error while threading debugger server! " + ex);
        } 

        
    }
   




    /**
     * Debug mode is automatically set depending on host OS. Windows = debugMode on
     * @param aFlag
     */
    @Deprecated
    public static void setDebugMode(boolean aFlag) {
        debugMode = aFlag;
    }

    /**
     * Returns a human readable current time stamp
     * @return
     */
    public static String getTimeStamp() {
        return formatter.format(new Date());
    }

    /**
     * Returns a human readable time stamp
     * @param currentTime
     * @return
     */
    public static String getTimeStamp(long currentTime) {
        return formatter.format(currentTime);
    }

    /**
     * Adds a DeviceConnection to debugging engine
     * @param dc
     * @param name - name describing the device e g "NEC projector"
     */
    public static void addDevice(DeviceConnection dc, String name) {
        devices.put(name, dc);
        dc.setDebugMode(true);
        dc.setDeviceName(name);
    }
    
    /**
     * Adds a device to debugging engine
     * @param dc
     * @param name - name describing the device e g "NEC projector"
     */
    public static void addDevice(Device dc, String name) {
        addDevice(dc.getConnection(), name);
    }
    @Sharable
    static class DebugServerHandler extends SimpleChannelInboundHandler<String> {


        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            
            if (clients.size() > 100) {
                ctx.write("The server is busy, please try again later.");
                ctx.write(delimiter);
                ctx.write("Good bye!");
                ctx.write(delimiter);
                ctx.close();
                log.println("Denied connection request from " + ctx.channel().remoteAddress());
            } else {
                clients.add(ctx.channel());
                System.out.println("Clients=" + clients.size());
                clientsEmpty = false;
                log.println("Accepted connection request from " + ctx.channel().remoteAddress());
                System.out.println("hehiopp1");
                // Send greeting.          
                ctx.channel().write("Atea iCtrl debug console");
                ctx.write(delimiter);
                ctx.write("martin.arnsrud@atea.se");
                ctx.write(delimiter);
                clients.writeAndFlush("hehiopp2");
                for (Map.Entry ent : devices.entrySet()) {
                    ctx.write(new CommandAddDevice((String) ent.getKey()) + delimiter);
                }
                ctx.flush();
                ctx.channel().flush();
                System.out.println("hehiopp3");
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.println("Client disconnected: " + ctx.channel().remoteAddress());
            clients.remove(ctx.channel());
            if (clients.isEmpty()) {
                clientsEmpty = true;
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf inBuffer = (ByteBuf) msg;

            String line = inBuffer.toString(CharsetUtil.UTF_8);

            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("bye")) {
                ctx.disconnect();
                clients.remove(ctx);
                if (clients.size() == 0) {
                    clientsEmpty = true;
                }
            } else if (line.equalsIgnoreCase("restart")) {
                log.println("Debugger requested the application to exit.");
                System.exit(0);
            } else if (line.equalsIgnoreCase("keep-alive")) {
                // do nothing
            } else if (line.equalsIgnoreCase("status")) {
                System.out.println("Status");
                ctx.write("Timestamp: " + getTimeStamp());
                ctx.write(delimiter);
                ctx.write("Number of connected clients: " + clients.size());
                ctx.write(delimiter);
            } else if (line.startsWith("<debug>")) {
                DebugMessage debugMsg = MessageParser.parseMessage(line);
                if (debugMsg.getType() == DebugMessage.TYPE_COMMAND_SEND) {
                    CommandSendToDevice cmdstd = (CommandSendToDevice) debugMsg;
                    devices.get(cmdstd.getDevice()).sendCommand(cmdstd.getMessageFormatted(), 10);
                }
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
//                    .addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.err.println("Hej hopp");
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext chc, String i) throws Exception {
            
        }
    }
}
