/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.debugger.tools;

/**
 *
 * @author Joseph
 */
public class TextMessage extends DebugMessage {
    
   
    
    public TextMessage(String s) {
        super(DebugMessage.TYPE_NONE);
        addArgument("timestamp", "00:00:00.000");
        addArgument("message", s);
    }
    
    public TextMessage(String ts, String m) {
        super(DebugMessage.TYPE_TEXT);
        addArgument("timestamp", ts);
        addArgument("message", m);
    }
    
    public String getMessage() {
        return getArgument("message");
    }

    public String getTimeStamp() {
        return getArgument("timestamp");
    }
    
    
    public TextMessage getWrapped(int size) {
        StringBuilder sb = new StringBuilder(getMessage());
        StringBuilder ts = new StringBuilder(getTimeStamp());
        int msgSize = getMessage().length();
        for (int i = 1; i*size < msgSize; i = i+1) {
            sb.insert(i*size, "\n");
            ts.append("\n");
        }
        return new TextMessage(ts.toString(), sb.toString());
    }
    
}
