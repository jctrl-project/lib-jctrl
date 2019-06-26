/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.widget;

import com.atea.ictrl.uiserver.widget.Widget;

/**
 *
 * @author KAOHM
 */
public class ToggleButton extends Widget {
    
    private String displayname;
    private String icon;
    
    public ToggleButton(String id, String icon, String displayname, String defaultValue) {
        super(id, WIDGET_TYPE.TYPE_SWITCH);
        
        this.setDisplayname(displayname);
        this.setValue(defaultValue);
        this.setIcon(icon);
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    
}
