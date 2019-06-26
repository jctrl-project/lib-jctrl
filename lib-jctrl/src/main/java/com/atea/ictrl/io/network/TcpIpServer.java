package com.atea.ictrl.io.network;

/* 
*/

import com.atea.ictrl.io.DeviceConnection;
import com.atea.ictrl.io.DeviceConnectionListener;
import com.atea.ictrl.io.debugger.ICtrlDebugger;
import java.io.*;
import java.net.*;
import java.util.Timer;
import org.w3c.dom.Element;




public class TcpIpServer extends DeviceConnection {
    private boolean connect = false;
    public Timer scheduler;
    protected ServerSocket listen_socket;
    protected Socket client_socket;
    protected int port;
    public final static int DEFAULT_PORT = 6789;

    public TcpIpServer (int port) {
        this.port = port;
        connect = true;
        try {
            listen_socket = new ServerSocket(port);
        } catch (IOException ex) {
            ICtrlDebugger.err.println("Failed to bind server." + ex);
        } catch (Exception ex) {
            ICtrlDebugger.err.println("Failed to bind server." + ex);
        }
        enterListeningMode();
    }
    private static void main(String[] args) {
        
        final TcpIpServer t = new TcpIpServer(5678);
        t.addDeviceConnectionListener(new DeviceConnectionListener() {
            @Override
            public void parseBytes(byte[] bytes) throws Exception {
                t.sendCommandNow(bytes);
            }

            @Override
            public void parseByte(byte b) throws Exception {
                
            }

            @Override
            public void parseLine(String line) throws Exception {
                
            }
            
            @Override
            public void onConnect() throws Exception {
                System.out.println("onConnect");
            }

            @Override
            public void onDisconnect() throws Exception {
                System.out.println("onDisconnect");
            }
        });
        t.setDebugMode(true);
    }
    private void acceptClient(Socket s) throws Exception {
        setRunThreads(false);
        //To unlock the write thread
        sendCommand("", 0);
        Thread.sleep(1000);
        client_socket = s;
        debugMessage("Client accepted: " + client_socket.getInetAddress());
        rawTx = client_socket.getOutputStream();
        rawRx = client_socket.getInputStream();        
        initCommandWriter();
        initCommandReader();
        onConnect();
        for (DeviceConnectionListener dcl : listeners) {
            try {
                dcl.onConnect();
            } catch (Exception ex) {
                debugMessage("Error while running onConnect-event: " + ex);
            }
        }
    }
    private void enterListeningMode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {                                        
                    while(connect) {
                        debugMessage("Listening for incoming connections...");
                        Socket newSocket = listen_socket.accept();     
                        if(client_socket != null) {
                            debugMessage(newSocket.getInetAddress() + " / " + client_socket.getInetAddress());                        
                        }
                        if(client_socket == null || !runThreads) {
                            acceptClient(newSocket);
                        } else if(newSocket.getInetAddress().equals(client_socket.getInetAddress())) { // new connection from same client. Close previous connection!
                            client_socket.close();
                            acceptClient(newSocket);
                        } else { // deny other clients connection
                            debugMessage("Denied connection for: " + newSocket.getInetAddress());
                            newSocket.getOutputStream().write(("Server is busy serving " + client_socket.getInetAddress() + "\n").getBytes());
                            newSocket.close();                            
                        }                        
                                     
                    }
                    
                } catch (IOException e) {
                    debugMessage("An IO-exception occured while starting the server.");
                    e.printStackTrace();
                } catch (Exception e) {
                    debugMessage("An exception occured while starting the server.");
                    e.printStackTrace();
                }
            }
        }).start();
    }
   

    @Override
    public void stopReadAndWrite() {    
        //To unlock the write thread
        setRunThreads(false);
        sendCommand("", 0);
        for (DeviceConnectionListener dcl : listeners) {            
            try {
                dcl.onDisconnect();
            } catch (Exception ex) {
                debugMessage("Error while running onDisconnect-event: " + ex);
            }
        }
    }
   
    @Override
    public void connect() {
        if(connect == false) {
            connect = true;        
            enterListeningMode();
        }        
    }   
  
   

   
    /**
     * Attempts to disconnect the device until connect() is called
     * @param
     */
    public void disconnect() {
        connect = false;
        try {
            debugMessage("Stänger anslutningen.");
            client_socket.close();
        } catch (IOException ex) {}
    }
    
    @Override
    public void connectImpl() throws Exception {
    
    }
    

    @Override
    public void disconnectImpl() throws Exception {

    }

   
}
