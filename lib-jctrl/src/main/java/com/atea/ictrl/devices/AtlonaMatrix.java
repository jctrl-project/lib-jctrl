/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.devices;

import com.atea.ictrl.io.DeviceConnection;
import com.atea.ictrl.io.network.TcpIpClient;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author MAARN
 */
public class AtlonaMatrix extends Device {
      
    private Timer poll;
    public AtlonaMatrix(DeviceConnection dc) {
        super(dc);
        poll = new Timer();
        poll.scheduleAtFixedRate(new ScheduledTask(), 0, 20000);
    }
    public AtlonaMatrix(String host) {
        super(new TcpIpClient(host, 23));
    }
    class ScheduledTask extends TimerTask {

        @Override
        public void run() {
            if (connection.getTxQueueSize() == 0 && connection.connected) {
                sendCommand("VersionX\r", 10);
            }
        }
    }
    public void routeAudioAndVideo(int input, int output) {
        if (output > 0 && input > 0) {
            sendCommand("x" + input + "AVx" + output + "\r", 10);
        } else if(output > 0 && input == 0) {
            sendCommand("x" + output + "$\r", 10);
        }
    }

    public void routeAudioOnly(int input, int output) {
        if (output > 0 && input > 0) {
            sendCommand("x" + input + "AVx" + output + "\r", 10);
        } else if(output > 0 && input == 0) {
            sendCommand("x" + output + "$\r", 10);
        }
    }

    public void routeVideoOnly(int input, int output) {
        if (output > 0 && input > 0) {
            sendCommand("x" + input + "AVx" + output + "\r", 10);
        } else if(output > 0 && input == 0) {
            sendCommand("x" + output + "$\r", 10);
        }
    }
    public void routeToAll(int input) {
        sendCommand("x" + input + "All\r", 10);
    }
}
