package com.borsoftlab.ozee;

public class OzLocation {
    public int line; // Номер строки
    public int pos; // Номер символа в строке

    public OzLocation() {
    };

    public OzLocation(final OzLocation loc) {
        copy(loc);
    };

    public void copy(final OzLocation loc) {
        this.line = loc.line;
        this.pos  = loc.pos;
    }
 }