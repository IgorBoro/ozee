package com.borsoftlab.ozee;

import java.util.List;

import com.borsoftlab.ozee.OzSymbols.Symbol;

public class OzLinker {

        public static byte[] linkImage(final byte[] program, final List<Symbol> symbols){
                int headerSize = 0;
                int codeOriginAddress = 4 + headerSize;

                int progSize = program.length;     
                int symbolTableSize = 0;

                for (Symbol symbol : symbols) {
                        symbol.allocAddress += ( codeOriginAddress + progSize );        
                        symbolTableSize += symbol.sizeInBytes;
                }

                int imageSize = codeOriginAddress + progSize + symbolTableSize;
                byte[] image = new byte[imageSize];
                OzUtils.storeIntToByteArray(image, 0, codeOriginAddress);
                for (int i = codeOriginAddress; i < progSize + codeOriginAddress; i++) {
                        image[i] = program[i - codeOriginAddress];
                }

                // re-binding refs
                for (Symbol symbol : symbols) {
                        List<Integer> refList = symbol.refList;
                        for (Integer ref : refList) {
                                OzUtils.storeIntToByteArray(image, ref + codeOriginAddress, symbol.allocAddress);        
                        }
                }
                return image;
        }

}