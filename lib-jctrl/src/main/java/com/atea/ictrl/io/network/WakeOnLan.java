/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.io.network;

import com.atea.ictrl.io.debugger.ICtrlDebugger;
import java.net.*;

public class WakeOnLan {

    public static final int PORT = 9;

    public static void send(final String broadcastAddress, final String macAddress) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    byte[] macBytes = getMacBytes(macAddress);
                    byte[] bytes = new byte[6 + 16 * macBytes.length];
                    for (int i = 0; i < 6; i++) {
                        bytes[i] = (byte) 0xff;
                    }
                    for (int i = 6; i < bytes.length; i += macBytes.length) {
                        System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
                    }

                    InetAddress address = InetAddress.getByName(broadcastAddress);
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
                    DatagramSocket socket = new DatagramSocket();
                    socket.send(packet);
                    socket.close();

                    ICtrlDebugger.out.println("Wake-on-LAN packet sent on " + broadcastAddress + " for device " + macAddress);
                } catch (Exception e) {
                    ICtrlDebugger.out.println("Failed to send Wake-on-LAN packet: + e");
                }
            }
        }).start();
        

    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }

}
