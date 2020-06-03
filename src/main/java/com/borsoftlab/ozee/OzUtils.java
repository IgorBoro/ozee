package com.borsoftlab.ozee;

import java.util.List;

import com.borsoftlab.ozee.OzParser.ByteArray;

public class OzUtils {


    static public void storeIntToByteArray(final byte[] mem, final int addr, final int value){
        // little-endian
        mem[addr]     = (byte)  ( value & 0x000000FF         );
        mem[addr + 1] = (byte) (( value & 0x0000FF00 ) >>  8 );
        mem[addr + 2] = (byte) (( value & 0x00FF0000 ) >> 16 );
        mem[addr + 3] = (byte) (( value & 0xFF000000 ) >> 24 );
    }

    static public void storeByteToByteArray(final byte[] mem, final int addr, final int value){
        // little-endian
        mem[addr]     = (byte)  ( value & 0x000000FF         );
    }

    static public void storeShortToByteArray(final byte[] mem, final int addr, final int value){
        // little-endian
        mem[addr]     = (byte)  ( value & 0x000000FF         );
        mem[addr + 1] = (byte) (( value & 0x0000FF00 ) >>  8 );
    }

    static public void addIntToByteArray(final ByteArray mem, final int value){
        // little-endian
        mem.add( (byte)  ( value & 0x000000FF         ));
        mem.add( (byte) (( value & 0x0000FF00 ) >>  8 ));
        mem.add( (byte) (( value & 0x00FF0000 ) >> 16 ));
        mem.add( (byte) (( value & 0xFF000000 ) >> 24 ));
    }

    static public int fetchIntFromByteArray(final byte[] mem, final int addr){
        // little-endian
        return  ( mem[addr + 3] << 24 ) & 0xFF000000 |
                ( mem[addr + 2] << 16 ) & 0x00FF0000 |
                ( mem[addr + 1] <<  8 ) & 0x0000FF00 |
                ( mem[addr]         ) & 0x000000FF;
    }

    static public float fetchFloatFromByteArray(final byte[] mem, final int addr){
        return  Float.intBitsToFloat(fetchIntFromByteArray(mem, addr));
    }

    static public int fetchByteFromByteArray(final byte[] mem, final int addr){
        // little-endian
        return  ( mem[addr]   & 0x000000FF );
    }

    static public int fetchShortFromByteArray(final byte[] mem, final int addr){
        // little-endian
        return  ( mem[addr + 1] <<  8 ) & 0x0000FF00 |
                ( mem[addr]         ) & 0x000000FF;
    }

    static public void addIntToByteArray(final List<Byte> mem, final int value){
        // little-endian
        mem.add((byte)  ( value & 0x000000FF         ));
        mem.add((byte) (( value & 0x0000FF00 ) >>  8 ));
        mem.add((byte) (( value & 0x00FF0000 ) >> 16 ));
        mem.add((byte) (( value & 0xFF000000 ) >> 24 ));
    }

    static public int fetchIntFromByteArray(final List<Byte> mem, final int addr){
        // little-endian
        return  ( mem.get(addr + 3) << 24 ) & 0xFF000000 |
                ( mem.get(addr + 2) << 16 ) & 0x00FF0000 |
                ( mem.get(addr + 1) <<  8 ) & 0x0000FF00 |
                ( mem.get(addr)           ) & 0x000000FF;
    }

    public static byte[] toByteArray(List<Byte> in) {
        final int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = in.get(i);
        }
        return ret;
    }

    public static void printMemoryDump(final byte[] mem){
        printMemoryDump(mem, 0, mem.length);
    }

    public static void printMemoryDump(final byte[] mem, int from, int length){
        if( from % 16 == 0){
            System.out.print(String.format("0x%08X: 0x%02X", from, mem[from]));
        } else {
            int start = 16 * (from / 16);
            System.out.print(String.format("0x%08X:", start));
            for( int ptr = start; ptr < from; ptr ++ ){
                System.out.print("     ");
            }
            System.out.print(String.format(" 0x%02X", mem[from]));

        }
        from++;
        int rightBorder = Math.min(length, mem.length);
        for (int ptr = from; ptr < rightBorder; ptr++){
            if( ptr % 16 == 0){
                System.out.println();                
                System.out.print(String.format("0x%08X: 0x%02X", ptr, mem[ptr]));
            } else {
                System.out.print(String.format(" 0x%02X", mem[ptr]));
            }
        }
        System.out.println();                
    }

}