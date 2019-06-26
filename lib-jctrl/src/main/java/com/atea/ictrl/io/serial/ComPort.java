/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.atea.ictrl.io.serial;

import com.atea.ictrl.io.DeviceConnection;


import com.atea.ictrl.io.debugger.ICtrlDebugger;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;

/**
 *
 * @author Martin
 */
public abstract class ComPort extends DeviceConnection 
        implements SerialPortEventListener {
    public SerialPort serialPort;
    public CommPort commPort;
    public String portName;
    StringBuilder sb = new StringBuilder("");
    private Byte b;
    private int bInt;
    String lineToParse = "";
    private boolean lookingForLineFeed = false;
    public int baudRate;
    
    public ComPort(String portName, int baudRate) {
        this.portName = portName;
        this.baudRate = baudRate;
    }
    public abstract void parseByte(byte data) throws Exception;
    public abstract void parseLine(String line) throws Exception;

    @Override
    public void connectImpl() throws Exception {
        CommPortIdentifier portIdentifier =
                    CommPortIdentifier.getPortIdentifier(portName);
        if(portIdentifier.isCurrentlyOwned()) {
            debugMessage("Error: Port is currently in use");
        } else {
            
            commPort =
                    portIdentifier.open(this.getClass().getName(), 2000);            
            if ( commPort instanceof SerialPort ) {
                serialPort = (SerialPort) commPort;
                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);
                serialPort.setSerialPortParams(baudRate,
                        SerialPort.DATABITS_8,SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                rawTx = serialPort.getOutputStream();
                rawRx = serialPort.getInputStream();

            } else {}
        }

    }

    @Override
    public void disconnectImpl() throws Exception {
        if(commPort != null && serialPort != null) {
            rawTx.close();
            rawRx.close();
            commPort.close();
            serialPort.close();
            connected = false;
            rawTx = null;
            rawRx = null;
        }
    }
    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void serialEvent(SerialPortEvent spe) {
        if(spe.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            readSerial();
        }
    }
    private void readSerial() {        
        try {
            while(true) {
                bInt = rawRx.read();
                if(bInt == -1) {
                    break;
                }
                b = (byte) bInt;  
                if (rawMode) {  // Raw byte-by-byte parsing                                                                                
                    try {
                        parseByte(b);
                    } catch (Exception e) {
                        debugMessage("Parsingfel: " + e);
                    }                                                               
                    if (debugMode) {
                        // Trim debug message line
                        if (System.currentTimeMillis() - rxTime
                                > 100) {
                            ICtrlDebugger.out.print(
                                    "\n" + deviceName + " RX: ");

                        }
                        ICtrlDebugger.out.print(Integer.toString((b & 0xff)
                                + 0x100, 16).substring(1).
                                toUpperCase() + " ");
                    }
                    rxTime = System.currentTimeMillis();
                    
                } else {  // Regular line parsing mode                    
                    if (bInt == '\n') {
                        if (lookingForLineFeed) {
                            lookingForLineFeed = false;
                            continue;
                        } else {
                            try {
                                lineToParse = sb.toString();
                                sb = new StringBuilder("");
                                parseLine(lineToParse);
                                debugMessage("RX: " + lineToParse);
                                
                            } catch (Exception e) {
                                debugMessage("Parsingfel: " + e);
                            }
                        }
                    } else if (bInt == '\r') {
                        lookingForLineFeed = true;
                        try {
                            lineToParse = sb.toString();
                            sb = new StringBuilder("");
                            parseLine(lineToParse);
                            debugMessage("RX: " + lineToParse);
                            
                        } catch (Exception e) {
                            debugMessage("Parsingfel: " + e);
                        }
                    } else {
                        lookingForLineFeed = false;
                        sb.append((char) bInt);
                    }
                    rxTime = System.currentTimeMillis();                                                            
                }
            }
        } catch (IOException ex) {
            debugMessage("I/O läsfel. " + ex);
            
        } catch (NullPointerException ex) {
            debugMessage("I/O Null läsfel. " + ex);
        }
    }
    @Override
    public void initCommandReader() {
        // not used
    }
}
