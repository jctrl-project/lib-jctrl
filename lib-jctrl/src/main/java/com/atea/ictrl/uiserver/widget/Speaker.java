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
public class Speaker extends OutputDevice {
  
    public Speaker(String name, WIDGET_ICON icon ) {
        super(name, icon, OutputDevice.OUTPUT_TYPE.AUDIO);
      
    }

    
    
}
