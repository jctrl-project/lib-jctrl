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
 * @author KAOHM
 */
public class SourceSelector extends Widget {
    
    private ArrayList<Source> sources; 
    private int output;

    @JsonIgnore
    private OutputDevice outputDevice;
    @JsonIgnore
    private Source offSource;
    
    public SourceSelector(String id, int output, OutputDevice outputDevice, WIDGET_TYPE widgetType) {
        super(id, widgetType);
        this.setDisplayname(outputDevice.getName());
        this.offSource =  new Source("Stäng av", "off", 0);
        this.setValue(offSource.getValue());
        this.setIcon(outputDevice.getIcon());
        
        this.sources = new ArrayList<>();
        
        this.outputDevice = outputDevice;
        this.output = output;
       
        this.sources.add(offSource);
    }

    public int getOutput(){
        return output;
    }
    
    /**
     * Add a source to the list of sources
     * @param s 
     */
    public void addSource(Source s) {
        this.sources.add(s);
    }
    
    /**
     * Add a collection of sources to the list of sources
     * @param sources 
     */
    public void addSources(ArrayList<Source> sources) {
        this.sources.addAll(sources);
    }

    public ArrayList<Source> getSources() {
        return sources;
    }
    
    public OutputDevice getOutputDevice(){
        return outputDevice;
    }
    
    
    /**
     * Replace the list of sources with a new list of sources
     * @param sources 
     */
    public void setSources(ArrayList<Source> sources){
        this.sources = sources;
    }
            
  
    
    public Source getSourceFromValueorOff(String value){
        for(Source s : sources){
            if(s.getValue().equalsIgnoreCase(value)){
                return s;
            }  
        }
        return offSource;
    }
    
    public boolean isOffSource(Source source){
        return source.equals(offSource);
    }
    
         

    
}
