package com.borsoftlab.ozee;

public class OzUtils{


    static public void storeIntToByteArray(final byte[] mem, final int addr, final int value){
        // little-endian
        mem[addr]     = (byte)  ( value & 0x000000FF         );
        mem[addr + 1] = (byte) (( value & 0x0000FF00 ) >>  8 );
        mem[addr + 2] = (byte) (( value & 0x00FF0000 ) >> 16 );
        mem[addr + 3] = (byte) (( value & 0xFF000000 ) >> 24 );
    }

    static public int fetchIntFromByteArray(final byte[] mem, final int addr){
        // little-endian
        return  ( mem[addr + 3] << 24 ) & 0xFF000000 |
                ( mem[addr + 2] << 16 ) & 0x00FF0000 |
                ( mem[addr + 1] <<  8 ) & 0x0000FF00 |
                ( mem[addr]         ) & 0x000000FF;
    }

}