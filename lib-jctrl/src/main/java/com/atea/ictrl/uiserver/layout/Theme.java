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
public class Theme {
    
    private String primary = "#ffffff";
    private String secondary = "#ffffff";
    private String tertiary = "#ffffff";
    private String quaternary = "#ffffff";
    
    public Theme() {
        
    }
    
    public Theme(String primary, String secondary, String tertiary, String quaternary) {
        this.primary = primary;
        this.secondary = secondary;
        this.tertiary = tertiary;
        this.quaternary = quaternary;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getSecondary() {
        return secondary;
    }

    public void setSecondary(String secondary) {
        this.secondary = secondary;
    }

    public String getTertiary() {
        return tertiary;
    }

    public void setTertiary(String tertiary) {
        this.tertiary = tertiary;
    }

    public String getQuaternary() {
        return quaternary;
    }

    public void setQuaternary(String quaternary) {
        this.quaternary = quaternary;
    }
    
    
}
