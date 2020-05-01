package com.borsoftlab.oZee;

class Location {
    int line;    // Номер строки           
    int pos;     // Номер символа в строке 
    public void copy(final Location loc){
        this.line = loc.line;
        this.pos  = loc.pos;
    }
 }