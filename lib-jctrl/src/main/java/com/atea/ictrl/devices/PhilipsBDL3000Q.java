/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.devices;

import com.atea.ictrl.io.DeviceConnection;
import com.atea.ictrl.io.DeviceConnectionListener;
import com.atea.ictrl.io.network.TcpIpClient;
import com.atea.ictrl.io.network.WakeOnLan;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Martin
 */
public class PhilipsBDL3000Q extends Device implements Projector {
    public static final int DEFAULT_PORT = 5000;
    private boolean setPower = false;
    private int checkInterval = 5000;
    private long lastVolumeChange = 0;
    private String macAddress = "";
    private Timer poll;
    private boolean getPower = false;
    
    private int timeout = 0;
    private long pingTime = 0;
    private long pongTime = 0;
    public PhilipsBDL3000Q(String ip, int port) {
        super(new TcpIpClient(ip, port));
        startJob();
    }
    public PhilipsBDL3000Q(String ip, String macAddress) {
        super(new TcpIpClient(ip, DEFAULT_PORT));
        this.macAddress = macAddress;
        startJob();
    }
    
    
    class ScheduledTask extends TimerTask {

        @Override
        public void run() {            
            if (getConnection().getTxQueueSize() == 0 && getConnection().connected) {
                sendHexCommand("05 01 00 19 1D", 10);                
                pingTime = System.currentTimeMillis();                
            }
        }
    }
    private void startJob() {
        poll = new Timer();
        poll.scheduleAtFixedRate(new ScheduledTask(), 20000, checkInterval);
        this.getConnection().addDeviceConnectionListener(new DeviceConnectionListener() {
            
            @Override
            public void parseBytes(byte[] bytes) throws Exception {                
                if(DeviceConnection.ByteArrayToHexString(bytes).equalsIgnoreCase("06 01 01 19 01 1E")) {
                    getPower = true;
                } else if(DeviceConnection.ByteArrayToHexString(bytes).equalsIgnoreCase("06 01 01 19 02 1D")) {
                    getPower = false;
                } 
                pongTime = System.currentTimeMillis();
            }

            @Override
            public void parseByte(byte b) throws Exception {}

            @Override
            public void parseLine(String line) throws Exception {}

          
            @Override
            public void onConnect() throws Exception {
                getConnection().clearTxQueue();
                timeout = 0;
            }

            @Override
            public void onDisconnect() throws Exception {
                timeout = 0;
            }
        });
    }
    public void setPower(boolean aFlag) {
        this.getConnection().clearTxQueue();
        setPower = aFlag;
        if(aFlag) {
            if(macAddress.length() > 0) {
                WakeOnLan.send("255.255.255.255", macAddress);
                sendHexCommand("06 01 00 18 02 1D", 5000);
            }
            sendHexCommand("06 01 00 18 02 1D", 5000);
        } else { 
            sendHexCommand("06 01 00 18 01 1E", 5000);
            sendHexCommand("06 01 00 18 01 1E", 5000); 
        }            
    }
    public void setInput(int input) {
        this.getConnection().clearTxQueue();
        if(!setPower) {
            setPower(Projector.PowerOn);
        }
        switch(input) {
            case Projector.InputDVI:
                sendHexCommand("09 01 00 AC 0E 09 01 00 A2", 100);
                break;
            case Projector.InputVga1:
                sendHexCommand("09 01 00 AC 05 09 01 00 A9", 100);
                break;
            case Projector.InputHDMI:
                sendHexCommand("09 01 00 AC 0D 09 01 00 A1", 100);
                break;
            case Projector.InputHDMI2:
                sendHexCommand("09 01 00 AC 06 09 01 00 AA", 100);
                break;
            case Projector.InputHDMI3:
                sendHexCommand("09 01 00 AC 0F 09 01 00 A3", 100);
                break;
            case Projector.InputCVBS:
                sendHexCommand("09 01 00 AC 01 09 01 00 AD", 100);
                break;
            case Projector.InputComponent:
                sendHexCommand("09 01 00 AC 03 09 01 00 AF", 100);
                break;
        }
    }

    public void setPictureMute(boolean muteOn) {
        
    }
    public void setVolume(int level) {      
        // && System.currentTimeMillis()-lastVolumeChange > 10
        if(level >= 0 && level <= 100) {
            int checkSum = 0x07^0x01^0x00^0x44^level^level;     
            String hexLevel = Integer.toHexString(level);
            if(hexLevel.length() == 1)
                hexLevel = "0" + hexLevel;
            String hexCheckSum = Integer.toHexString(checkSum);
            if(hexCheckSum.length() == 1)
                hexCheckSum = "0" + hexCheckSum;
            sendHexCommand("07 01 00 44 " + hexLevel + " " + hexLevel + " "+
                    hexCheckSum, 20);
            lastVolumeChange = System.currentTimeMillis();
        }
    }
    public void setLineVolume(int level) {
        // && System.currentTimeMillis()-lastVolumeChange > 10
        if (level >= 0 && level <= 100) {
            int checkSum = 0x07 ^ 0x01 ^ 0x00 ^ 0x44 ^ 0x00 ^ level;
            String hexLevel = Integer.toHexString(level);
            if (hexLevel.length() == 1) {
                hexLevel = "0" + hexLevel;
            }
            String hexCheckSum = Integer.toHexString(checkSum);
            if (hexCheckSum.length() == 1) {
                hexCheckSum = "0" + hexCheckSum;
            }
            sendHexCommand("07 01 00 44 00 " + hexLevel + " "
                    + hexCheckSum, 20);
            lastVolumeChange = System.currentTimeMillis();
        }
    }
    public boolean getPower() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getInput() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean getPictureMute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void autoAdjust() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
}
