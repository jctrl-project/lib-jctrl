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
public class OutputDevice {
    
    public static enum OUTPUT_TYPE {VIDEO, AUDIO}
    
    private String name;
    
    private WIDGET_ICON icon;
    
    private final OUTPUT_TYPE outputType;
    
   
    public OutputDevice(String name, WIDGET_ICON icon, OUTPUT_TYPE outputType ) {
        this.name = name;  
        this.icon = icon;
        this.outputType = outputType;     
    }
       
    public String getName() {
        return name;
    }

    public WIDGET_ICON getIcon(){
        return icon;
    }
    
   
    
    public OUTPUT_TYPE getOutputType(){
        return outputType;
    }
    
    
    
}
