/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.uiserver.widget;

/**
 *
 * @author KAOHM
 */
public class Source {
    
    private String name;
    private String value;
    private int input;

    public Source(String name, String value, int input) {
        this.name = name;
        this.value = value;
        this.input = input;
    }
    
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getInput() {
        return input;
    }

    public void setInput(int input) {
        this.input = input;
    }
    
}
