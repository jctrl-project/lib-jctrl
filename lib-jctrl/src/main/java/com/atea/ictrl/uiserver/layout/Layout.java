/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.layout;

import java.util.ArrayList;

/**
 *
 * @author KAOHM
 */
public class Layout {
    
    private String layoutName;
    private ArrayList<LayoutSection> sections = new ArrayList();
    
    private String backgroundB64;
    
    private String logoB64;
    
    private String helpHtml;
    
    private String helpPdf;
    
    private Theme theme;
    
    
    public Layout(String name) {
        this.layoutName = name;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    public ArrayList<LayoutSection> getSections() {
        return sections;
    }

    public void setSections(ArrayList<LayoutSection> sections) {
        this.sections = sections;
    }
    
    public void addSection(LayoutSection section) {
        this.sections.add(section);
    }

    public String getLogoB64() {
        return logoB64;
    }

    public void setLogoB64(String logoB64) {
        this.logoB64 = logoB64;
    }

    public String getBackgroundB64() {
        return backgroundB64;
    }

    public void setBackgroundB64(String backgroundB64) {
        this.backgroundB64 = backgroundB64;
    }

    /**
    * @deprecated Use {@link #getHelpPdf} instead.  
    */
    public String getHelpHtml() {
        return helpHtml;
    }

    /**
    * @deprecated Use {@link #setHelpPdf} instead.  
    */
    public void setHelpHtml(String helpHtml) {
        this.helpHtml = helpHtml;
    }

    public String getHelpPdf() {
        return helpPdf;
    }

    public void setHelpPdf(String helpPdf) {
        this.helpPdf = helpPdf;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }
    
    
}
