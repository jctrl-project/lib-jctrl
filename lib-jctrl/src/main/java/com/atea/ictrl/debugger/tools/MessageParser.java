/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.debugger.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Joseph
 */
public class MessageParser {

    
    private static String getText(Document doc, String s) {
        return doc.getElementsByTagName(s).item(0).getTextContent();
    }
    
    public static DebugMessage parseMessage(String s) {
        DebugMessage m = null;
        try {
            Document doc = getXMLFromString(s);
            String version = getText(doc, "version");
            DebugMessage.setVersion(Integer.parseInt(version));
            if (version.equals("2")) {
                String type = getText(doc, "type");
                if (type.equals("add-device")) {
                    String device = getText(doc, "device");
                    m = new CommandAddDevice(device);
                }
                else if (type.equals("send-to-device")) {
                    String device = getText(doc, "device");
                    String msg = getText(doc, "message");
                    m = new CommandSendToDevice(device, msg);
                }
                else if (type.equals("message")) {
                    String timeStamp = getText(doc, "timestamp");
                    String msg = getText(doc, "message");
                    m = new TextMessage(timeStamp, msg);
                }
                else {
                    m = new TextMessage(s);
                }
            }
            
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            m = parseMessageOld(s);
        } finally {
            return m;
        }
    }
    
    /**
     * @deprecated
     **/
    private static DebugMessage parseMessageOld(String s) {
        DebugMessage.setVersion(1);
        DebugMessage m = null;
        
        Pattern message = Pattern.compile("imsg(\\d+);-;(.+);-;iendts;-;(.+)");
        Pattern command = Pattern.compile("icmd(\\d+);-;(\\d);-;((?:(?!;-;).)+)(?:;-;(.+))?");
        Matcher msg = message.matcher(s);
        Matcher cmd = command.matcher(s);
        
        if(msg.find()) {
            m = new TextMessage(msg.group(2), msg.group(3));
        }
        else if (cmd.find()) {
            int type = Integer.parseInt(cmd.group(2));
            if (type == DebugMessage.TYPE_COMMAND_ADD) {
                m = new CommandAddDevice(cmd.group(3));
            }
            else if (type == DebugMessage.TYPE_COMMAND_SEND) {
                m = new CommandSendToDevice(cmd.group(3), cmd.group(4));
            }
        }
        else {
            m = new TextMessage(s);
        }
        return m;
    }
    
    
    
    public static byte[] format(String s) {
        StringBuilder sb = new StringBuilder(s);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        while(sb.length() > 0) {
            if (sb.toString().matches("(\\\\)((x[0-9a-fA-F]{2})|(\\\\)|n|r).*")) {
                if (sb.charAt(1) == '\\') {
                    bytes.write('\\');
                    sb.delete(0, 2);
                }
                else if (sb.charAt(1) == 'n') {
                    bytes.write('\n');
                    sb.delete(0, 2);
                } 
                else if (sb.charAt(1) == 'r') {
                    bytes.write('\r');
                    sb.delete(0, 2);
                }
                else if (sb.substring(1, 4).matches("^x[0-9a-fA-F]{2}$")) {
                    int c = Integer.parseInt(sb.substring(2, 4), 16);
                    bytes.write(c);
                    sb.delete(0, 4);
                }
                else {
                    bytes.write('\\');
                    sb.delete(0, 1);
                }
            }
            else {
                bytes.write(sb.toString().getBytes()[0]);
                sb.delete(0, 1);
            }
        }
        return bytes.toByteArray();
    }
    
    
    private static Document getXMLFromString(String xml) throws SAXParseException, IOException, SAXException, ParserConfigurationException {
        Document doc = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        doc = builder.parse(new InputSource(new StringReader(xml)));
        return doc;
    }
}
