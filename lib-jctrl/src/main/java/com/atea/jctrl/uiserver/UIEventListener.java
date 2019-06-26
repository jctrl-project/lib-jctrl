/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.jctrl.uiserver;

import com.atea.ictrl.uiserver.widget.Widget;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author MAARN
 */
public interface UIEventListener {
    public void UIConnected(ChannelHandlerContext ctx);
    public void UIDisconnected(ChannelHandlerContext ctx);
    public void UIWidgetEvent(Widget w, ChannelHandlerContext ctx);
}
