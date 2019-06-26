/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.io.network;

import com.atea.ictrl.io.DeviceConnection;
import static com.atea.ictrl.io.DeviceConnection.ByteArrayToHexString;
import static com.atea.ictrl.io.DeviceConnection.isReadable;
import com.atea.ictrl.io.DeviceConnectionListener;
import com.atea.ictrl.io.debugger.ICtrlDebugger;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Martin Arnsrud (martin.arnsrud@gmail.com)
 */
public class UdpClient extends DeviceConnection {

    private DatagramSocket socket;
    private String defaultHost;
    private int port;
    private int localPort;
    private long sleepTime = 3000;
    private DatagramPacket txPacket;
    private DatagramPacket rxPacket;
    private StringBuffer lineToParse = new StringBuffer("");
    private byte[] rxBuffer = new byte[1500];
    private LinkedBlockingQueue<UdpCommand> txQueue =
            new LinkedBlockingQueue<UdpCommand>(); // Commandqueue

    public UdpClient(String defaultHost, int port) {
        this.defaultHost = defaultHost;
        this.port = port;
        this.localPort = port;
    }
     public UdpClient(String defaultHost, int port, int localPort) {
        this.defaultHost = defaultHost;
        this.port = port;
        this.localPort = localPort;
    }
    
    class UdpCommand {
        private Command command;
        private String destinationIpAddress;
        public UdpCommand(Command command, String destinationIpAddress) {
            this.command = command;
            this.destinationIpAddress = destinationIpAddress;            
            
        }
        public int getExecutionTime() {return command.getExecutionTime();}
        public byte[] getCommand() {return command.getCommand();}
        public String getDestinationIpAddress() {return destinationIpAddress;}
    }

    
    private UdpCommand popNextUdpCommand() {
        try {
            return (UdpCommand) txQueue.take();
        } catch (InterruptedException e) {
            return null;
            //throw new RuntimeException(e);
        }
    }
    public void bind(String ipAddress, int port) throws SocketException {
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(ipAddress, port));
    }
    @Override
    public void clearTxQueue() {
        txQueue.clear();
    }

    @Override
    public int getTxQueueSize() {
        if (txQueue != null) {
            return txQueue.size();
        } else {
            return 0;
        }
    }
    @Override
    public void connectImpl() throws Exception {
        socket = new DatagramSocket(localPort);                   
        rxPacket = new DatagramPacket(rxBuffer, rxBuffer.length);        
    }

    @Override
    public void disconnectImpl() throws Exception {
        stopReadAndWrite();
        socket.disconnect();
        socket.close();
        socket = null;
        rxPacket = null;
    }

    public void setReceiveBufferSize(int size) {
        rxBuffer = new byte[size];
    }
    

    private void sendCommand(UdpCommand command) {        
        txTime = System.currentTimeMillis();
        try {
            txQueue.put(command);
        } catch (InterruptedException e) {
            // IOexception something has failed during writing to the buffer
        }
    }
    public void parseByte(byte data, InetAddress sender) throws Exception {};
    @Override
    public void sendCommand(byte[] command, int executionTime) {        
        UdpCommand cmd = new UdpCommand(new Command(command, executionTime), defaultHost);
        sendCommand(cmd);
    }

    @Override
    public void sendHexCommand(String hexString, int executionTime) {        
        sendCommand(HexStringToByteArray(hexString),
                executionTime);
    }

    @Override
    public void sendCommand(String command, int executionTime) {
        sendCommand(command.getBytes(), executionTime);
    }
    public void sendCommand(byte[] command, int executionTime, String destinationIpAddress) {
        UdpCommand cmd = new UdpCommand(new Command(command, executionTime), destinationIpAddress);
        sendCommand(cmd);
    }
    public void sendHexCommand(String hexCommand, int executionTime, String destinationIpAddress) {
        sendCommand(HexStringToByteArray(hexCommand), executionTime, destinationIpAddress);
    }
    @Override
    public void sendCommandNow(String command) {
        try {
            txPacket = new DatagramPacket(command.getBytes(),
                    command.getBytes().length, InetAddress.getByName(defaultHost), port);
            socket.send(txPacket);
            debugMessage(deviceName + "> TX: " + command);
            writeError = false;
        } catch (Exception ex) {
            writeError = true;
            debugMessage("An error occured while writing to device: " + ex);
        }
    }

    @Override
    public void sendCommandNow(byte[] command) {
        try {
            txPacket = new DatagramPacket(command,
                    command.length, InetAddress.getByName(defaultHost), port);
            socket.send(txPacket);
            debugMessage(deviceName + "> TX: ", command);
            writeError = false;
        } catch (Exception ex) {
            writeError = true;
            debugMessage("An error occured while writing to device: " + ex);
        }

    }

    @Override
    public void initCommandWriter() {
        setRunThreads(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (runThreads) {
                    try {
                        UdpCommand cmd = popNextUdpCommand();
                        sleepTime = cmd.getExecutionTime();
                        if (debugMode) {
                            ICtrlDebugger.out.print(deviceName + "> TX: ");
                            if (isReadable(cmd.getCommand())) {
                                ICtrlDebugger.out.println(new String(cmd.getCommand()));
                            } else {
                                ICtrlDebugger.out.println(ByteArrayToHexString(cmd.getCommand()));
                            }
                        }                    
                        txPacket = new DatagramPacket(cmd.getCommand(),
                                cmd.getCommand().length,
                                InetAddress.getByName(cmd.getDestinationIpAddress()), port);                        
                        socket.send(txPacket);
                        txTime = System.currentTimeMillis();  
                        writeError = false;
                    } catch (IOException e) {
                        writeError = true;
                        debugMessage("An error occured while writing to device: " + e);
                    } catch (Exception e) {
                        writeError = true;
                        debugMessage("An error occured while writing to device: " + e);
                    }
                    try {
                        Thread.sleep(sleepTime);
                    } catch (Exception e) {
                        stopReadAndWrite();
                        break;
                    }
                }
            }
        }, "writeThread").start();
    }

    @Override
    public void initCommandReader() {
        setRunThreads(true);
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (runThreads) {
                    try {
                        socket.receive(rxPacket);                        
                        rxTime = System.currentTimeMillis();
                        InetAddress senderAddress = rxPacket.getAddress();
                        byte[] tempBytes = rxPacket.getData();                        
                        if(lineBuffer.size() > 1024) { 
                            lineBuffer.reset(); // this is probably not a line based protocol ?
                        }                               
                        byte[] bytesToParse = new byte[rxPacket.getLength()-rxPacket.getOffset()];
                        int j = 0;
                        for (int i = rxPacket.getOffset(); i < rxPacket.getLength(); i++) {                            
                            bytesToParse[j] = tempBytes[i];
                            j++;
                            try {
                                for (DeviceConnectionListener dcl : listeners) {
                                    dcl.parseByte(tempBytes[i]);
                                }                                
                                parseByte(tempBytes[i], senderAddress);
                            } catch (Exception e) {
                                debugMessage("Byte parsing error: " + e.getMessage());
                                e.printStackTrace();
                            }   
                            if(!rawMode) {
                                char c = (char) tempBytes[i];
                                if (c == '\n') {
                                    if (lookingForLineFeed) {
                                        lookingForLineFeed = false;
                                        continue;
                                    } else {
                                        lineDetected();
                                    }
                                } else if (c == '\r') {
                                    lookingForLineFeed = true;
                                    lineDetected();
                                } else {
                                    lookingForLineFeed = false;
                                    lineBuffer.write(tempBytes[i]);
                                }
                            }
                        }
                        for (DeviceConnectionListener dcl : listeners) {
                            dcl.parseBytes(bytesToParse);
                        }
                        debugMessage("RX: ", bytesToParse);                                                                        
                    } catch (IOException ex) {
                        debugMessage("I/O error while reading: " + ex);
                        stopReadAndWrite();
                        readError = true;
                        break;
                    } catch (Exception ex) {
                        debugMessage("I/O error while reading: " + ex);
                        stopReadAndWrite();
                        readError = true;
                        break;
                    }

                }
            }
        }, "readThread").start();
    }
}
