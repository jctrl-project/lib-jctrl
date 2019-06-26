/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.atea.ictrl.devices;
import com.atea.ictrl.io.debugger.ICtrlDebugger;
import com.atea.ictrl.io.network.TcpIpClient;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;


/**
 *
 * @author Martin
 */

public class PJLinkProjector extends TcpIpClient implements Projector {
    public static final int DefaultPort = 4352;
    private static final String MD5 = "MD5";
    private int checkInterval = 18000;
    private boolean setPower = false;
    private int setInput = -1;
    private boolean getPower = false;
    private int getInput = 0;
    private boolean noReply = false;
    private boolean getNoSignal = false;
    private int lampHours = 0;
    private boolean getMute = false;
    private boolean setMute = false;
    private boolean powerStatusRead = false;
    private boolean authenticated = false;
    private Timer poll;
    private String password = "";
    private String passwordDigest = "";
    private int absoluteInput = 0;
    public PJLinkProjector(String host, int port) {
        super(host, port);        
        connect();
        poll = new Timer();
        poll.scheduleAtFixedRate(new ScheduledTask(), 0, checkInterval);
    }
    public PJLinkProjector(String host) {
        super(host, DefaultPort); 
        connect();
        poll = new Timer();
        poll.scheduleAtFixedRate(new ScheduledTask(), 0, checkInterval);
    }
    public PJLinkProjector(String host, int port, String password) {
        super(host, DefaultPort); 
        this.password = password;        
        connect();
        poll = new Timer();
        poll.scheduleAtFixedRate(new ScheduledTask(), 0, checkInterval);
    }

    public void autoAdjust() {}
    
    class ScheduledTask extends TimerTask {
        @Override
        public void run() {
            if(getTxQueueSize() == 0 && connected) {
                requestCheck();
            }
        }
    }
    

    @Override
    public void sendCommand(String command, int executionTime) {
        command = passwordDigest +  command;
        super.sendCommand(command, executionTime); 
    }
    private void requestCheck() {
        if(authenticated) {
            sendCommand("%1POWR ?\r",2000);
            sendCommand("%1INPT ?\r",2000);
            sendCommand("%1AVMT ?\r",2000);
            sendCommand("%1LAMP ?\r",2000);
        }
    }
    public void projectorPowerStatusChange(boolean power) {
        
    }
    @Override
    public void setPower(boolean powerOn) {
        clearTxQueue();
        setPower = powerOn;
        if(powerOn) {
            sendCommand("%1POWR 1\r", 20000);
        } else {
            sendCommand("%1POWR 0\r", 20000);
        }
    }
    
    
    public void setPower(boolean powerOn, int speed) {
        clearTxQueue();
        setPower = powerOn;
        if(powerOn) {
            sendCommand("%1POWR 1\r", speed);
        } else {
            sendCommand("%1POWR 0\r", speed);
        }
    }
    @Override
    public void setInput(int input) {
        clearTxQueue();
        setInput = input;
        if (setPower == PowerOff || getPower == PowerOff) {
            setPower(PowerOn);
        }
        if (setInput != getInput) {
            switch(input) {
                case Projector.InputCVBS:
                    sendCommand("%1INPT 21\r", 2000);
                    break;
                case Projector.InputVga1:
                    sendCommand("%1INPT 11\r", 2000);
                    break;
                case Projector.InputVga2:
                    sendCommand("%1INPT 12\r", 2000);
                    break;
                case Projector.InputDVI:
                    sendCommand("%1INPT 32\r", 2000);
                    break;
                case Projector.InputHDMI:
                    sendCommand("%1INPT 31\r", 2000);
                    break;
                case Projector.InputHDMI2:
                    sendCommand("%1INPT 37\r", 2000);
                    break;    
                case Projector.InputSVideo:
                    sendCommand("%1INPT 22\r", 2000);
                    break;
                case Projector.InputHDBaseT:
                    sendCommand("%1INPT 33\r", 2000);
                    break; 
                case Projector.InputHDBaseT2:
                    sendCommand("%1INPT 34\r", 2000);
                    break;
                case Projector.InputHDMI3:
                    sendCommand("%1INPT 35\r", 2000);
                    break;
                case Projector.InputHDBaseT3:
                    sendCommand("%1INPT 36\r", 2000);
                    break;
                default:
                    sendCommand("%1INPT " + String.valueOf(input) + "\r", 2000);
                    break;
            }
        }
    }

    @Override
    public void setPictureMute(boolean muteOn) {
        setMute = muteOn;        
        if(setPower && getPower) {
            if(muteOn) {
                sendCommandNow("%1AVMT 31\r");
            } else {
                sendCommandNow("%1AVMT 30\r");
            }
        }
    }

    
    @Override
    public boolean getPower() {
        return getPower;
    }

    @Override
    public int getInput() {
        return getInput;
    }
    public boolean getNoReply() {
        return noReply;
    }
    public boolean getNoSignal() {
        return getNoSignal;
    }
    public int getLampHours() {
        return lampHours;
    }
    @Override
    public boolean getPictureMute() {
        return getMute;
    }
    private void fixPower() {
        if (!powerStatusRead) {
            setPower = getPower;
            powerStatusRead = true;
        }
        if (getPower != setPower && powerStatusRead) {
            setPower(setPower);
        }
    }
    private void fixInput() {
        if (getInput != setInput && setInput != -1 && setPower) {
            setInput(setInput);
        } else if (setInput == -1) {
            setInput = getInput;
        }
    }
    private void fixMute() {
        if (getMute != setMute) {
            setPictureMute(setMute);
        }
    }
    @Override
    public void parseLine(String line) {
        if(line.equals("%1POWR=0") || line.equals("%1POWR=2")) {
//            if(getPower == true) {
//                projectorPowerStatusChange(false);
//            }
            getPower = false;                        
            fixPower();
        } else if(line.equals("%1POWR=1") || line.equals("%1POWR=3")) {
//            if(getPower == false) {
//                projectorPowerStatusChange(true);
//            }
            getPower = true;
            //projectorPowerStatusChange(getPower);
            fixPower();                        
        } else if(line.equals("%1INPT=11")) {
            getInput = Projector.InputVga1;
            fixInput();
        } else if(line.equals("%1INPT=12")) {
            getInput = Projector.InputVga2;
            fixInput();
        } else if(line.equals("%1INPT=21")) {
            getInput = Projector.InputCVBS;
            fixInput();
        } else if(line.equals("%1INPT=22")) {
            getInput = Projector.InputSVideo;
            fixInput();
        } else if(line.equals("%1INPT=31")) {
            getInput = Projector.InputHDMI;                                
            fixInput();     
        } else if(line.equals("%1INPT=32")) {            
            getInput = Projector.InputDVI;            
            fixInput();     
        } else if(line.equals("%1INPT=33")) {            
            getInput = Projector.InputHDBaseT;            
            fixInput();     
        } else if(line.equals("%1INPT=37")) {            
            getInput = Projector.InputHDMI2;            
            fixInput();     
        } else if(line.equals("%1INPT=34")) {            
            getInput = Projector.InputHDBaseT2;            
            fixInput();     
        } else if(line.equals("%1INPT=35")) {            
            getInput = Projector.InputHDMI3;            
            fixInput();     
        } else if(line.equals("%1INPT=36")) {            
            getInput = Projector.InputHDBaseT3;            
            fixInput();     
        } else if(line.equals("%1INPT=OK")) {            
            // do nothing
        } else if(line.startsWith("%1INPT=ERR")) {            
            // do nothing
        } else if(line.startsWith("%1INPT=")) {            
            getInput = Integer.valueOf(line.substring(7));
            fixInput();     
        } else if(line.equals("%1AVMT=30")) {
            getMute = false;
            fixMute();
        } else if(line.equals("%1AVMT=31")) {
            getMute = true;
            fixMute();
        } else if(line.equals("%1LAMP=")) {
            int marker1 = line.indexOf("=") + 1;
            int marker2 = line.indexOf(" ", marker1);
            if(marker2 == -1)
                marker2 = line.length();
            try {
                lampHours = Integer.parseInt(
                        line.substring(marker1, marker2));
            } catch(NumberFormatException e) {}            
        } else if(line.startsWith("PJLINK 1")) {
            String[] tokens = line.split(" ");
            if(tokens.length < 3) {
                return;
            } else {                
                passwordDigest = GetPasswordDigest(password, tokens[2]);
            }
            authenticated = true;
        } else if(line.startsWith("PJLINK 0")) {           
            authenticated = true;
        } else if(line.startsWith("PJLINK ERRA")) {
            authenticated = false;
            disconnect();
            connect();
        }
        
    }


    @Override
    public void disconnectImpl() throws Exception {
        super.disconnectImpl(); 
        authenticated = false;
    }
    // takes a password and the random string and returns the digest

    public static String GetPasswordDigest(String password, String randomString) {
        // initialize the return value
        String pwDigest = "";

        try {
            // get the MD5 digest
            MessageDigest digest = MessageDigest.getInstance(MD5);

            // append the password to the random string
            String passwordString = randomString + password;

            // get the digest in bytes and format it as hexidecimal then convert it to a string
            digest.update(passwordString.getBytes());
            byte[] bytes = digest.digest();
            BigInteger bi = new BigInteger(1, bytes);
            pwDigest = String.format("%0" + (bytes.length << 1) + "x", bi);
        } // couldn't find the MD5 algorithm
        catch (NoSuchAlgorithmException e) {
            ICtrlDebugger.out.println("PJLINK NO SUCH ALGORITHM");
            pwDigest = "NO SUCH ALGORITHM";
        }

        return pwDigest;

    }
    @Override
    public void parseByte(byte data) {}


    @Override
    public void initialize() throws Exception {
        clearTxQueue();
    }

    
}
