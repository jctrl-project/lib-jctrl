/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.widget;

import com.atea.ictrl.devices.MotorInterface;

/**
 * Extension of Widget that requires implementation of MotorInterface
 * @author ANHAG
 */
public class MotorControl extends Widget {
    
    MotorInterface motor;
    
    public MotorControl(String id, WIDGET_TYPE type, WIDGET_ICON icon, String displayname, MotorInterface motor) {
       super(id, type, icon, displayname, null);
       this.motor = motor;
    }
    
    public void runMotor(int direction) {
        motor.runMotor(direction);
    } 
     
     
}
