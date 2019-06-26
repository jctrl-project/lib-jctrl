

package com.atea.ictrl.io.network;

/**
 *
 * @author Martin Arnsrud (martin.arnsrud@gmail.com)
 */

import com.atea.ictrl.io.DeviceConnection;
import com.atea.ictrl.io.SafeBufferedReader;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class TcpIpClient extends DeviceConnection {
    private Socket socket;
    private String host;
    private int port;
    public String portName;
    public TcpIpClient(String host, int port) {
        this.host = host;
        this.port = port;              
    }
    public void connectImpl() throws Exception {
        socket = new Socket(host, port);
        socket.setReuseAddress(true);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        rawTx = socket.getOutputStream();
        rawRx = socket.getInputStream();
        connected = true;
    }
    public void disconnectImpl() throws Exception {
        txTime = 0;
        rxTime = 0;                
        if(socket != null) {
            rawTx.close();
            rawRx.close();
            socket.shutdownOutput();
            socket.close();
            connected = false;
        }
    }
    public void setKeepAlive(boolean keepAliveOn) {
        try {
            socket.setKeepAlive(keepAliveOn);
        } catch (SocketException ex) { }
    }
    public boolean isConnected() {
        return connected;
    }
}
