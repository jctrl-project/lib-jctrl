/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.debugger.tools;

/**
 *
 * @author Joseph
 */
public class CommandAddDevice extends DebugMessage {
    
    
    public CommandAddDevice(String d) {
        super(DebugMessage.TYPE_COMMAND_ADD);
        addArgument("device", d);
    }
    
    public String getDevice() {
        return getArgument("device");
    }

    
}
