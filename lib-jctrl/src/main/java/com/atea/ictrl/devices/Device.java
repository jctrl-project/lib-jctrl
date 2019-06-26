/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.devices;

import com.atea.ictrl.io.DeviceConnection;

/**
 *
 * @author Joseph
 */
public class Device {
    
    
    protected DeviceConnection connection;
    
    
    protected Device(DeviceConnection c) {
        connection = c;
        connection.connect();
    }
    
    public void sendCommand(String s, int i) {
        connection.sendCommand(s, i);
    }
    
    public void sendCommand(byte[] b, int i) {
        connection.sendCommand(b, i);
    }
    
    public void sendHexCommand(String h, int i) {
        connection.sendHexCommand(h, i);
    }
    
    public void sendCommandNow(String s) {
        connection.sendCommandNow(s);
    }
    
    public void sendCommandNow(byte[] b) {
        connection.sendCommandNow(b);
    }
    
    public DeviceConnection getConnection() {
        return connection;
    } 
    
}
