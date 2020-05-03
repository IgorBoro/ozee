package com.borsoftlab.ozee;

public class Location {
    public int line;    // Номер строки           
    public int pos;     // Номер символа в строке 
    public void copy(final Location loc){
        this.line = loc.line;
        this.pos  = loc.pos;
    }
 }