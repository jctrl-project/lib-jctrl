/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.debugger.tools;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Joseph
 */
public class CommandSendToDevice extends DebugMessage {
    
    
    public CommandSendToDevice(String d, String m) {
        super(DebugMessage.TYPE_COMMAND_SEND);
        addArgument("device", d);
        addArgument("message", m);
    }
    
    public String getDevice() {
        return getArgument("device");
    }
    
    public String getMessage() {
        return getArgument("message");
    }
    
    public byte[] getMessageFormatted() {
        return MessageParser.format(getMessage());
    }
    
    
    
}
