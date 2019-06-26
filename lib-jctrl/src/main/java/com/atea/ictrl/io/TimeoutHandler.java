/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.io;


import com.atea.ictrl.io.debugger.ICtrlDebugger;
import com.jcraft.jsch.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;


/**
 * {@link IdleStateHandler} implementation which disconnect the {@link Channel} after a configured
 * idle timeout. Be aware that this handle is not thread safe so it can't be shared across pipelines
 *
 */

public class TimeoutHandler extends IdleStateHandler {

    public TimeoutHandler(int readerIdleTimeSeconds) {
        super(readerIdleTimeSeconds, 0, 0);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.err.println("hej" + evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
          //  if (e.state() == IdleState.READER_IDLE) {
                ICtrlDebugger.log.println("Removed idle client: " + ctx.channel().remoteAddress());
                ctx.close();
          //  } else if (e.state() == IdleState.WRITER_IDLE) {
          //      ctx.writeAndFlush(new PingMessage());
          //  }
        }
    }    
}