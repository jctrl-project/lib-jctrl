/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.io.debugger;

import static com.atea.ictrl.io.debugger.ICtrlDebugger.getTimeStamp;
import com.atea.ictrl.debugger.tools.TextMessage;
import io.netty.channel.group.ChannelGroup;


/**
 *
 * @author MAARN
 */
public class DebuggerStream {

    /**
     *
     */
    public static final int ModeOUT = 1;

    /**
     *
     */
    public static final int ModeLOG = 2;

    /**
     *
     */
    public static final int ModeERR = 3;
    private int mode = ModeOUT;
    private static final String delimiter = System.getProperty("line.separator");
    private final ChannelGroup clients;
    private StringBuilder buffer = new StringBuilder();
    private boolean targetMachine = false;
    /**
     *
     * @param mode
     * @param clients
     */
    public DebuggerStream(int mode, ChannelGroup clients) {        
        this.mode = mode;
        this.clients = clients;
        targetMachine = System.getProperty("os.name").toLowerCase().contains("linux");        
    }
   
    /**
     *
     * @param string
     */
    public void print(String string) {
        buffer.append(string);        
    }

    /**
     *
     * @param i
     */
    public void print(int i) {
       buffer.append(String.valueOf(i));
    }

    /**
     *
     * @param string
     */
    public  synchronized void println(String string) {        
        buffer.append(string);
        println();
    }
    /**
     *
     * @param string
     */
    public void println() {
        TextMessage tm = new TextMessage(getTimeStamp(),
                buffer.toString().replaceAll("\n", "").replaceAll("\r", ""));
        if (mode == ModeOUT) {
            clients.writeAndFlush(tm.toString() + delimiter);
            if(!targetMachine) {
                System.out.println(tm.getTimeStamp() + " | " + tm.getMessage());
            }
        } else if (mode == ModeLOG) {
            System.out.println(tm.getTimeStamp() + " | " + tm.getMessage());
        } else if (mode == ModeERR) {
            System.err.print(tm.getTimeStamp() + " | " + tm.getMessage());
        }
        buffer.setLength(0); // empty buffer
    }

    /**
     *
     * @param o
     */
    public void println(Object o) {
        println(o.toString());
    }
    
}
