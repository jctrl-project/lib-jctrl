/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.settings;

/**
 *
 * @author ANHAG03
 */

    

public class Setting {
    
    private final String id;
    private String value;
   
    public Setting(String id, String value){
        this.id = id;
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

     public String getId() {
        return id;
    }
    
   
    
    
    
    
}
