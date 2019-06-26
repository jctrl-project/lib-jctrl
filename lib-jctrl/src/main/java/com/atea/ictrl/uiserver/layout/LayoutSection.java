/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.layout;

/**
 *
 * @author KAOHM
 */
public class LayoutSection {
    
    public static enum LAYOUT_SECTION_TYPE {HEADER, FOOTER, BODY};
    
    private String name;
    private LAYOUT_SECTION_TYPE type;
    
    
    public LayoutSection(String name, LAYOUT_SECTION_TYPE type) {
        this.name = name;
        this.type = type;
        
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LAYOUT_SECTION_TYPE getType() {
        return type;
    }

    public void setType(LAYOUT_SECTION_TYPE type) {
        this.type = type;
    }
    
    
    
}
