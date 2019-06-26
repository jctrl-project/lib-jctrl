/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.atea.ictrl.devices;

/**
 *
 * @author Martin
 */
public interface Projector {
    public static final int InputVga1 = 1;
    public static final int InputVga2 = 2;
    public static final int InputDVI = 3;
    public static final int InputHDMI = 4;
    public static final int InputCVBS = 5;
    public static final int InputSVideo = 6;
    public static final int InputComponent = 7;
    public static final int InputRgbsVideo = 8;
    public static final int InputSocket1 = 9;
    public static final int InputSocket2 = 10;
    public static final int InputDisplayPort = 11;
    public static final int InputHDMI2 = 12;
    public static final int InputHDMI3 = 13;
    public static final int InputHDBaseT = 14;
    public static final int InputHDBaseT2 = 15;    
    public static final int InputHDBaseT3 = 16;  
    public static final int InputNA = 0;
    public static final boolean PowerOn = true;
    public static final boolean PowerOff = false;
    public static final boolean PictureMuteOn = true;
    public static final boolean PictureMuteOff = false;
    
    public void setPower(boolean powerOn);
    public void setInput(int input);
    public void setPictureMute(boolean muteOn);   
    public boolean getPower();
    public int getInput();
    public boolean getPictureMute();
    public void autoAdjust();    
}
