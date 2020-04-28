package com.borsoftlab.oZee;

public class OzUtils{

    static public void storeIntValueToMemory(final byte[] mem, int addr, final int value){
        mem[addr++] = (byte)  (value & 0x000000FF);
        mem[addr++] = (byte) ((value & 0x0000FF00) >>  8);
        mem[addr++] = (byte) ((value & 0x00FF0000) >> 16);
        mem[addr++] = (byte) ((value & 0xFF000000) >> 24);
    }

    static public int fetchIntValueFromMemory(final byte[] mem, final int addr){
        return  (mem[addr+3] << 24) & 0xFF000000 |
                (mem[addr+2] << 16) & 0x00FF0000 |
                (mem[addr+1] <<  8) & 0x0000FF00 |
                (mem[addr]        ) & 0x000000FF;
    }

}