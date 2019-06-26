/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.atea.ictrl.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Martin
 */
public abstract class CommandWriter extends Thread implements Runnable {
    public OutputStream rawTx;
    public BufferedWriter tx;
    private LinkedBlockingQueue<Command> txQueue =
            new LinkedBlockingQueue<Command>(); // Commandqueue
    public long txTime = 0; // When latest tx occured
    private long sleepTime; // Time between flushing commands
    public String deviceName = "";
    public boolean runThread;
    public boolean debugMode = false;
    public boolean rawMode; // Use raw I/O-streams.
    public boolean writeError; // Write-thread has been terminated
    public CommandWriter(OutputStream rawTx, BufferedWriter tx) {
        this.rawTx = rawTx;
        this.tx = tx;
        this.setName("CommandWriter");
        setRunThread(true);
    }
    public class Command { // Defines command structure
        private byte[] command;
        private Integer executionTime;
        public Command(byte[] command, int executionTime) {
            this.command = command;
            this.executionTime = executionTime;
        }
        public byte[] getCommand() {
            return command;
        }
        public int getExecutionTime() {
            return executionTime;
        }
    }
    private void sendCommand(Command command) {
        txTime = System.currentTimeMillis();
        try {
            txQueue.put(command);
        } catch (InterruptedException e) {
            // IOexception something has failed during writing to the buffer
        }
    }
    private Command popNextCommand() {
        try {
            return (Command) txQueue.take();
        } catch (InterruptedException e) {
            return null;
            //throw new RuntimeException(e);
        }
    }
    public int getTxQueueSize() {
        if(txQueue != null) {
            return txQueue.size();
        } else {
            return 0;
        }
    }
    public void clearTxQueue() {
        txQueue.clear();
    }
    
    public void sendCommand(byte[] command, int executionTime) {
        Command cmd = new Command(command, executionTime);
        sendCommand(cmd);
    }
    public void sendHexCommand(String hexString, int executionTime) {
        Command cmd = new Command(HexStringToByteArray(hexString),
                executionTime);
        sendCommand(cmd);
    }
    public void sendCommand(String command, int executionTime) {
        Command cmd = new Command(command.getBytes(), executionTime);
        sendCommand(cmd);
    }
    
    public void sendCommandNow(String command) {
        txTime = System.currentTimeMillis();
        try {
            tx.write(command);            
            tx.flush();
            debugMessage("TX: " + command);
        } catch (Exception ex) {}
    }
    public void sendCommandNow(byte[] command) {
        txTime = System.currentTimeMillis();
        try {
            rawTx.write(command);            
            rawTx.flush();
            if(debugMode) {
                System.out.print(deviceName + " TX: ");
                for(int i=0;i<command.length;i++) {
                    System.out.print(Integer.toString((
                            command[i] & 0xff ) +
                            0x100, 16).substring(1).
                            toUpperCase() + " ");
                }
                System.out.print("\n");
            }
        } catch (Exception ex) {}
    }
    // Stop I/O-threads
    public void close() {
        setRunThread(false);
        try {
            //To unlock the write thread
            tx.write("");
        } 
        catch (IOException e) {}
        catch (Exception e) {}
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
    public abstract void writeException(Exception e);

    public static byte[] HexStringToByteArray(String hexString) {
        hexString = hexString.replaceAll(" ", "");
        byte data[] = new byte[hexString.length()/2];
        for(int i=0;i < hexString.length();i+=2) {
            data[i/2] = (Integer.decode("0x"+hexString.charAt(i) +
                    hexString.charAt(i+1))).byteValue();
        }
        return data;
    }
    @Override
    public void run() {
        while (runThread) {
            try {
                Command cmd = popNextCommand();
                sleepTime = cmd.getExecutionTime();
                if(rawMode) { // Raw byte-by-byte write
                    if(debugMode) {
                        System.out.print(deviceName + " TX: ");
                        for(int i=0;i<cmd.getCommand().length;i++) {
                            System.out.print(Integer.toString((
                                    cmd.getCommand()[i] & 0xff ) +
                                    0x100, 16).substring(1).
                                    toUpperCase() + " ");
                        }
                        System.out.print("\n");
                    }
                    rawTx.write(cmd.getCommand());
                    txTime = System.currentTimeMillis();
                    rawTx.flush();
                } else { // Regular line write
                    debugMessage("TX: " + new String(
                        cmd.getCommand()));
                    tx.write(new String(cmd.getCommand()));
                    txTime = System.currentTimeMillis();
                    tx.flush();
                }
            } catch (IOException e) {
                writeError = true;
                debugMessage("Skrivfel: " + e);
                close();
                writeException(e);
                break;
            } catch (NullPointerException e) {
                writeError = true;
                debugMessage("Skrivfel: " + e);
                close();
                writeException(e);
                break;
            }
            try {
                Thread.sleep(sleepTime);
            } catch(Exception e) {
                writeError = true;
                close();
                writeException(e);
                break;
            }
        }
    }
}
