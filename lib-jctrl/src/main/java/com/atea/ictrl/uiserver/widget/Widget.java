/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.widget;


import com.atea.jctrl.uiserver.Message;
import java.util.ArrayList;



/**
 *
 * @author MAARN
 */
public class Widget implements Message {

    
    public static enum WIDGET_TYPE {TYPE_BUTTON, TYPE_SLIDER, TYPE_SWITCH, TYPE_AUDIOSOURCE, TYPE_VIDEOSOURCE, TYPE_WIDGETGROUP, TYPE_MOTORCONTROL}
    public static enum WIDGET_ICON {MICROPHONE, SCREEN, PROJECTOR, SPEAKER, VOLUME}
    
    public static final String VALUE_PRESSED = "pressed";
    public static final String VALUE_RELEASED = "released";
    
    private final String id;
    private String value;
    private final WIDGET_TYPE type;
    private boolean isVisible;
    private boolean isEnabled;
    
    private String displayname;
    private WIDGET_ICON icon;
    
    private ArrayList<String> flags = new ArrayList<>();
    private ArrayList<WidgetHandler> handlers = new ArrayList<WidgetHandler>();
    private WidgetGroup group;
    
    /**
     * Create a basic Widget with id and type.
     * @param id
     * @param type 
     */
    public Widget(String id, WIDGET_TYPE type) {
        this.id = id;
        this.type = type;
        this.isVisible = true;
        this.isEnabled = true;
    }
    
    /**
     * Create a basic Widget. handleAction should be overridden
     * @param id
     * @param type
     * @param icon
     * @param displayname
     * @param defaultValue 
     */
    public Widget(String id, WIDGET_TYPE type, WIDGET_ICON icon, String displayname, String defaultValue) {
        this(id, type);
        this.icon = icon;
        this.displayname = displayname;
        this.value = defaultValue;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean aFlag) {
        this.isVisible = aFlag;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
    @Override
    public String getMessageType() {
        return "update";
    }
    @Override
    public String getValue() {
        return value;
    }
    public WIDGET_TYPE getType() {
        return type;
    }
    public String getId() {
        return id;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public void setEnabled(boolean aFlag) {
        this.isEnabled = aFlag;
    }
    
    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getIcon() {
        if (icon != null) {
            return icon.toString();
        }
        return "";
    }

    public void setIcon(WIDGET_ICON icon) {
        this.icon = icon;
    }
    
    public WidgetGroup getGroup() {
        return this.group;
    }
    
    public void setGroup(WidgetGroup group) {
        this.group = group;
    }
    
    public void addWidgetHandler(WidgetHandler wh) {
        handlers.add(wh);
        
    }
    public void removeWidgetHandler(WidgetHandler wh) {
        handlers.remove(wh);
        
    }

    public void handleAction(String value) {
        for(WidgetHandler wh:handlers) {
            wh.handleAction(value);
        }
    }

    public ArrayList<String> getFlags() {
        return flags;
    }

    public void setFlags(ArrayList<String> flags) {
        this.flags = flags;
    }
    
    public void addFlag(String flag) {
        this.flags.add(flag);
    }
    
    
}
