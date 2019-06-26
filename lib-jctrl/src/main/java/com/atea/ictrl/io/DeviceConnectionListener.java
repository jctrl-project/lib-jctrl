/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.io;



/**
 *
 * @author MAARN
 */
public interface DeviceConnectionListener {
    public void parseBytes(byte[] bytes) throws Exception;
    public void parseByte(byte b) throws Exception;
    public void parseLine(String line) throws Exception;    
    public void onConnect() throws Exception;
    public void onDisconnect() throws Exception;
}
