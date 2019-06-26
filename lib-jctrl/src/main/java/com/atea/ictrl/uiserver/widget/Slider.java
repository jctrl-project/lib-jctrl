/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.widget;

/**
 * Extension of Widget that includes properties for min and max value
 * @author KAOHM
 */
public class Slider extends Widget {
    
    int min;
    int max;
    
    public Slider(String id, WIDGET_ICON icon, String displayname, String defaultValue, int min, int max) {
        super(id, WIDGET_TYPE.TYPE_SLIDER, icon, displayname, defaultValue);
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
      
}
