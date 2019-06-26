/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.widget;

import com.atea.ictrl.uiserver.widget.Widget.WIDGET_ICON;
import com.atea.ictrl.devices.Projector;

/**
 *
 * @author ANHAG
 */
public class Display extends OutputDevice{
    
    private Projector projector;
    private MotorControl motorControl;
    private int input;

    public Display(String name, int input, Projector projector, MotorControl motorControl, WIDGET_ICON icon ) {
        this(name, input, projector, icon);
        this.motorControl = motorControl;
    }
    
    public Display(String name, int input, Projector projector, WIDGET_ICON icon ) {
        super(name, icon, OutputDevice.OUTPUT_TYPE.VIDEO);    
        this.projector = projector;
        this.input = input;
        
    }

    public int getInput() {
        return input;
    }

    public void setInput(int input) {
        this.input = input;
    }
    
    
    public Projector getProjector() {
        return projector;
    }
  
    public boolean hasMotor(){
        return motorControl != null;
    }
    
    public MotorControl getMotorControl(){
        return motorControl;
    }
    
    
}
