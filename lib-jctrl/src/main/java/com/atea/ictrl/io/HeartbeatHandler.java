/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.io;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;


/**
 *
 * @author Martin
 */
@Sharable
public class HeartbeatHandler extends ChannelDuplexHandler {
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (null != e.state()) switch (e.state()) {
                case READER_IDLE:
                    System.out.println("Reader idle, closing channel");
                    
                    ctx.writeAndFlush("heartbeat-reader_idle");
                    break;
                case WRITER_IDLE:
                    System.out.println("Writer idle, sending heartbeat");
                    ctx.writeAndFlush("heartbeat-writer_idle");
                    break;
                case ALL_IDLE:
                    System.out.println("All idle, sending heartbeat");
                    ctx.writeAndFlush("heartbeat-all_idle");
                    break;
                default:
                    break;
            }
            ctx.close();
        }
    } 
}
