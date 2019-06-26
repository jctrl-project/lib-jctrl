/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.widget;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;

/**
 *
 * @author ANHAG
 */
public class AudioSelector extends SourceSelector {

    private ArrayList<Source> sources; 

    @JsonIgnore
    private Speaker speaker;
    @JsonIgnore
    private Source offSource;

    
    public AudioSelector(String id, int output, Speaker speaker) {
        super(id,output,speaker, WIDGET_TYPE.TYPE_AUDIOSOURCE);
        this.speaker = speaker;
    }
    
    public Speaker getSpeaker(){
        return speaker;
    }
      
}
