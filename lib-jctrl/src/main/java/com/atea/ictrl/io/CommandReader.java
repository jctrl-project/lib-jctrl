/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.atea.ictrl.io;

import java.io.IOException;
import java.io.InputStream;
import org.w3c.dom.Element;

/**
 *
 * @author Martin
 */
public abstract class CommandReader extends Thread implements Runnable {
    // Lowlevel i/o-streams
    public InputStream rawRx;
    public SafeBufferedReader rx;
    public String deviceName = "";
    private byte b; // Byte to parse
    private String line; // Line to parse
    private XmlParseHelper xmlParseHelper;
    public long rxTime = 0; // When latest rx occured
    public boolean readError; // Read-thread has been terminated
    public boolean runThread;
    public boolean debugMode = false;
    public boolean rawMode; // Use raw I/O-streams.
    
    public CommandReader(InputStream rawRx, SafeBufferedReader rx) {
        this.rawRx = rawRx;
        this.rx = rx;
        rxTime = System.currentTimeMillis();
        setRunThread(true);
        this.setName("CommandReader");
    }
    // Stop I/O-threads
    public void close() {
        setRunThread(false);
    }
    private void setRunThread(boolean runThread) {
        this.runThread = runThread;
    }
    public void debugMessage(String message) {
        if(debugMode)
            System.out.println(deviceName + "> " + message);
    }
    public void setDebugMode(boolean modeOn) {
        debugMode = modeOn;
    }
    public void setDeviceName(String name) {
        this.deviceName = name;
    }
    public void setRawMode(boolean on) {
        rawMode = on;
    }
    
    public long getRxTime() {return rxTime;}
    private String readXmlDocument(String start) {
        StringBuilder xmlBuffer = new StringBuilder(start);
        String readLine = readLine();
        xmlBuffer.append(readLine);
        while (!"</XmlDoc>".equals(readLine)) {
            readLine = readLine();
            xmlBuffer.append(readLine);
        }
        return xmlBuffer.toString();
    }
    private String readLine() {
        try {
            String read = rx.readLine();
            return read;
        } catch (IOException e) {
            return null;
        }
    }
    public abstract void readException(Exception e);
    public abstract void byteReceived(byte data) throws Exception;
    public abstract void lineReceived(String line) throws Exception;
    public abstract void xmlReceived(Element element) throws Exception;
    @Override
    public void run() {
        while (runThread) {
            if(rawMode) {  // Raw byte-by-byte parsing
                try {
                    b = (byte) rawRx.read();
                    if (b == -1) {
                        //throw new IOException();
                    }
                    try {
                        byteReceived(b);
                    }  catch (Exception e) {
                        debugMessage("Parsingfel: " + e);
                        e.printStackTrace();
                    }
                    if(debugMode) {
                        // Trim debug message line
                        if(System.currentTimeMillis() - rxTime
                                > 100) {
                            System.out.print(
                                    "\n" + deviceName + " RX: ");

                        }
                        System.out.print(Integer.toString((
                                b & 0xff ) +
                                0x100, 16).substring(1).
                                toUpperCase() + " ");
                    }
                    rxTime = System.currentTimeMillis();
                } catch (IOException ex) {
                    debugMessage("I/O läsfel. " + ex);
                    ex.printStackTrace();
                    close();
                    readError = true;
                    break;
                }
            } else {  // Regular line parsing mode
                try {
                    line = rx.readLine();
                    if (line == null) {
                        close();
                        break;
                    }
                    rxTime = System.currentTimeMillis();
                    debugMessage("RX: " + line);                       
                    try {
                        if (line.startsWith("<XmlDoc")) {
                            if(xmlParseHelper == null) {
                                xmlParseHelper = new XmlParseHelper();
                            }
                            String document = readXmlDocument(line);
                            xmlReceived(xmlParseHelper.parseXML(document));
                            
                        }
                        lineReceived(line);
                    } catch (Exception e) {
                        debugMessage("Parsingfel: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    debugMessage("I/O läsfel. " + e);
                    close();
                    readError = true;
                    readException(e);
                    break;
                } catch (NullPointerException e) {
                    debugMessage("I/O läsfel. " + e);
                    close();
                    readError = true;
                    readException(e);
                    break;
                }
            }
        }
    }


}
