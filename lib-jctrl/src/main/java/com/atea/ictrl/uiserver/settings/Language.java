/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.settings;

/**
 *
 * @author ANHAG03
 */
public class Language extends Setting {
    
    public static enum LANGUAGES {SWEDISH, ENGLISH}
    private String[] language =  {"sv","en"};  
    
    public Language(LANGUAGES lang){
        super("language", "sv");
        this.setValue(language[lang.ordinal()]);
    }
}
