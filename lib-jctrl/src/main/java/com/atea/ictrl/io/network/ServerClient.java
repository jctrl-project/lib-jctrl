package com.atea.ictrl.io.network;



/*
*/



import com.atea.ictrl.io.CommandReader;
import com.atea.ictrl.io.CommandWriter;
import com.atea.ictrl.io.SafeBufferedReader;
import com.atea.ictrl.io.debugger.ICtrlDebugger;
import java.io.*;
import java.net.*;
import java.util.Timer;
import org.w3c.dom.Element;


public class ServerClient implements Runnable {    
  // Lowlevel i/o-streams
    public InputStream rawRx;
    public OutputStream rawTx;
    // High-level i/o-streams
    public SafeBufferedReader rx;
    public BufferedWriter tx;
    public long txTime = 0; // When latest tx occured
    public long rxTime = 0; // When latest rx occured
    public String deviceName = "";
    public boolean connected; // Connection established.
    public boolean debugMode = false;
    public boolean rawMode; // Use raw I/O-streams.
    public boolean readError; // Read-thread has been terminated
    public boolean writeError; // Write-thread has been terminated
    public Timer scheduler;
    protected ServerSocket listen_socket;
    protected Socket client_socket;
    private long timeOfCreation = 0;
    private boolean isConnected = false;
    private CommandWriter commandWriter;
    private CommandReader commandReader;
    private Socket clientSocket;
    private String ipAddress;
    //private ServerClient client;
    private TcpIpMultiServer serv;
    private String charset;
    public ServerClient (Socket clientSocket, TcpIpMultiServer server) {
        this.clientSocket = clientSocket;
        this.serv = server;
        this.charset = "UTF-8";
    }
    public ServerClient(Socket clientSocket, TcpIpMultiServer server, String charset) {
        this.clientSocket = clientSocket;
        this.serv = server;
        this.charset = charset;
        if(clientSocket.getInetAddress().isLoopbackAddress()) {
            ipAddress = "127.0.0.1";
        } else {
            ipAddress = clientSocket.getInetAddress().getHostAddress();
        }
    }
    
    public Socket getClientSocket() {
        return this.clientSocket;
    }
    public long getTimeOfCreation() { return timeOfCreation;}
    @Override
    public void run() {
        try {
            timeOfCreation = System.currentTimeMillis();
            rawTx = clientSocket.getOutputStream();
            rawRx = clientSocket.getInputStream();
            rx = new SafeBufferedReader(
                    new InputStreamReader(rawRx, charset));
            tx = new BufferedWriter(
                    new OutputStreamWriter(rawTx, charset));
            initCommandWriter();
            initCommandReader();
            isConnected = true;
            serv.onConnect(this);
            holdThread();
            serv.removeClient(this);
            ICtrlDebugger.log.print(getIpAddress() + " was stopped successfully");
        } catch (Exception e) {
            ICtrlDebugger.log.print(getIpAddress() + " failed to stop successfully: " + e);
        }
    }
    public boolean isConnected() {return isConnected;}
    public synchronized void holdThread() throws InterruptedException {
        this.wait();
    }
    public int getTxQueueSize() {
        return commandWriter.getTxQueueSize();
    }
    public synchronized void stopClient() {
        commandWriter.close();
        commandReader.close();
        commandWriter = null;
        commandReader = null;
        try {
            rawTx.close();
            rawRx.close();
            rawTx = null;
            rawRx = null;
            clientSocket.close();
            clientSocket = null;
            serv.onDisconnect(ServerClient.this);    
            isConnected = false;
        } catch (IOException ex) {
        } catch (Exception ex) {
        } finally {
            this.notify();
        }        
    }
    private void initCommandWriter() {
        commandWriter = new CommandWriter(rawTx, tx) {
            @Override
            public void writeException(Exception e) {
                try {                    
                    ICtrlDebugger.log.print("Error while writing to client. " + e);
                    stopClient();
                } catch (Exception ex) {}
            }
        };        
        commandWriter.setDeviceName(this.getIpAddress());
        commandWriter.setDebugMode(debugMode);
        commandWriter.setRawMode(rawMode);
        commandWriter.start();
    }
    private void initCommandReader() {
        commandReader = new CommandReader(rawRx, rx) {
            @Override
            public void readException(Exception e) {
                try {
                    ICtrlDebugger.log.print("Error while reading from client. " + e);
                    stopClient();
                } catch (Exception ex) {}
            }
            @Override
            public void byteReceived(byte data) throws Exception {
                serv.parseByte(data, ServerClient.this);
            }
            @Override
            public void lineReceived(String line) throws Exception {
                serv.parseLine(line, ServerClient.this);
            }

            @Override
            public void xmlReceived(Element element) throws Exception {
                serv.parseXml(element, ServerClient.this);
            }

        };
        commandReader.setDeviceName(this.getIpAddress());
        commandReader.setDebugMode(debugMode);
        commandReader.setRawMode(rawMode);
        commandReader.start();
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void sendCommand(byte[] command, int executionTime) {
        commandWriter.sendCommand(command, executionTime);
    }
    public void sendHexCommand(String hexString, int executionTime) {
        commandWriter.sendHexCommand(hexString, executionTime);
    }
    public void sendCommand(String command, int executionTime) {
        commandWriter.sendCommand(command, executionTime);
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
    public void debugMessage(String message) {
        if(debugMode)
            System.out.println(deviceName + " " + this.getIpAddress() +"> " + message);
    }

    // Gets


    public boolean getDebugMode() {
        return debugMode;
    }

    public long getTxTime() {
        return commandWriter.txTime;
    }
    public long getRxTime() {
        return commandReader.getRxTime();
    }
    public String getDeviceName() {
        return deviceName;
    }

    // Sets
    public void setDebugMode(boolean modeOn) {
        debugMode = modeOn;
    }
    public void setRawMode(boolean on) {
        rawMode = on;
    }
    public void setDeviceName(String name) {
        this.deviceName = name;
    }

}
