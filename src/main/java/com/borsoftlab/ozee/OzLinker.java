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

                for (int i = 0; i < progSize; i++) {
                        image[i] = program.get(i);
                }

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