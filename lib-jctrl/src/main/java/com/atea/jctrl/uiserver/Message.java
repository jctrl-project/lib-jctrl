/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.jctrl.uiserver;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author MAARN
 */
public interface Message {
    @JsonProperty("value")
    public String getValue();
     @JsonProperty("message_type")
    public String getMessageType();
}
