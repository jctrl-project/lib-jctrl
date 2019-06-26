/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.jctrl.uiserver;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;

/**
 *
 * @author MAARN
 */
public class Heartbeat implements Message {
    
    final String messageType = "heartbeat";
    
    public Heartbeat() {
    }
    @JsonProperty("value")
    public String getValue() {
        return new Timestamp(System.currentTimeMillis()).toString();
    }
    @JsonProperty("message_type")
    public String getMessageType() {
        return messageType;
    }
    
}
