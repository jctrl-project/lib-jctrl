/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.debugger.tools;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author Joseph
 */
public class DebugMessage {
    
    
    public static final int TYPE_COMMAND_ADD = 1;
    public static final int TYPE_COMMAND_SEND = 2;
    public static final int TYPE_TEXT = 3;
    public static final int TYPE_NONE = 4;
    private static int version = 2;
    
    private final HashMap<String, String> args = new HashMap<>();
    
    
    private final int type;
    
    
    public DebugMessage(int t) {
        type = t;
    }
    
    public static void setVersion(int i) {
        version = i;
    }
    
    public int getType() {
        return type;
    }
    
    public final void addArgument(String k, String v) {
        args.put(k, v);
    }
    
    public final String getArgument(String k) {
        if (args.containsKey(k)) {
            return args.get(k);
        }
        else {
            return "";
        }
    }
    
    protected String xmlEscape(String s) {
        String escaped = s;
        escaped = escaped.replaceAll("&", "&amp;");
        escaped = escaped.replaceAll("\"", "&quot;");
        escaped = escaped.replaceAll("'", "&apos;");
        escaped = escaped.replaceAll("<", "&lt;");
        escaped = escaped.replaceAll(">", "&gt;");
        return escaped;
    }
    
    @Override
    public String toString() {
        if (version >= 2) {
            String typeS;
            switch(type) {
                case TYPE_COMMAND_ADD:
                    typeS = "add-device";
                    break;
                case TYPE_COMMAND_SEND:
                    typeS = "send-to-device";
                    break;
                case TYPE_TEXT:
                    typeS = "message";
                    break;
                default:
                    typeS = "unknown";

            }
            StringBuilder sb = new StringBuilder("<debug><version>2</version><type>");
            sb.append(typeS).append("</type>").append("<arguments>");
            for (Entry e : args.entrySet()) {
                sb.append("<").append(xmlEscape((String) e.getKey())).append(">");
                sb.append(xmlEscape((String) e.getValue()));
                sb.append("</").append(xmlEscape((String) e.getKey())).append(">");
            }
            sb.append("</arguments></debug>");
            return sb.toString();
        }
        else {
            switch(type) {
                case (TYPE_TEXT):
                    return "imsg1;-;" + getArgument("timestamp") + ";-;iendts;-;" + getArgument("message");
                case (TYPE_COMMAND_ADD):
                    return "icmd1;-;" + type + ";-;" + getArgument("device");
                case (TYPE_COMMAND_SEND):
                    return "icmd1;-;" + type + ";-;" + getArgument("device") + ";-;" + getArgument("message");
                default:
                    return "";
            }
            
        }
    }
    
}
