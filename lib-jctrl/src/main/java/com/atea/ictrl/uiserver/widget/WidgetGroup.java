/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.widget;

import java.util.ArrayList;

/**
 *
 * @author KAOHM
 */
public class WidgetGroup extends Widget{
    
    public WidgetGroup(String id, String displayname, WIDGET_ICON icon) {
        super(id, WIDGET_TYPE.TYPE_WIDGETGROUP);
        super.setDisplayname(displayname);
        super.setIcon(icon);
    }
    
}
