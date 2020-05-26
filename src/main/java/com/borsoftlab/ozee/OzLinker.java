package com.borsoftlab.ozee;

import java.util.List;

import com.borsoftlab.ozee.OzSymbols.Symbol;

public class OzLinker {

        public static byte[] linkImage(final List<Byte> program, final List<Symbol> symbols){
           int progSize = program.size();     
           int symbolTableSize = 0;

           for (Symbol symbol : symbols) {
                   symbolTableSize += symbol.sizeInBytes;
           }

           int imageSize = progSize + symbolTableSize;
           byte[] image = new byte[imageSize];

           System.arraycopy(program, 0, image, 0, progSize);

           // re-binding refs
           for (Symbol symbol : symbols) {
                List<Integer> refList = symbol.refList;
                for (Integer ref : refList) {
                        OzUtils.storeIntToByteArray(image, ref, progSize + symbol.allocAddress);        
                }
           }

           return image;
        }

}