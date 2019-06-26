/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.widget;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author ANHAG
 */
public class VideoSelector extends SourceSelector {

    @JsonIgnore
    private Display display;

    
    public VideoSelector(String id, int output, Display display) {
        super(id,output,display, WIDGET_TYPE.TYPE_VIDEOSOURCE);
        this.display = display;    
    }
    
    public Display getDisplay(){
        return display;
    }
    
}
