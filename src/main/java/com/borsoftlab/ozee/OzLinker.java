package com.borsoftlab.ozee;

import java.util.List;

import com.borsoftlab.ozee.OzSymbols.Symbol;

public class OzLinker {

        public static byte[] linkImage(final byte[] program, final List<Symbol> symbols){

                // a header of image is empty
                int headerSize = 0;
                int codeOriginAddress = 4 + headerSize;

                int progSize = program.length;     
                int symbolTableSize = 0;

                // calculate image size
                for (Symbol symbol : symbols) {
                        symbol.allocAddress += ( codeOriginAddress + progSize );        
                        symbolTableSize += symbol.sizeInBytes;
                }
                int imageSize = codeOriginAddress + progSize + symbolTableSize;

                // create the empty image
                byte[] image = new byte[imageSize];

                // put the start address at the beginning of the image
                OzUtils.storeIntToByteArray(image, 0, codeOriginAddress);

                // copy the program to the image
                for (int i = codeOriginAddress; i < progSize + codeOriginAddress; i++) {
                        image[i] = program[i - codeOriginAddress];
                }

                // initialize the data section
                               
                // re-binding refs
                for (Symbol symbol : symbols) {
                    List<Integer> refList = symbol.refList;
                    /*
                    switch(symbol.sizeInBytes){
                        case 4:
                            OzUtils.storeIntToByteArray(image, progSize + codeOriginAddress + symbol.allocAddress, symbol.value);        
                            break;
                        case 2:
                            OzUtils.storeShortToByteArray(image, progSize + codeOriginAddress + symbol.allocAddress, symbol.value);        
                            break;
                        case 1:
                            OzUtils.storeByteToByteArray(image, progSize + codeOriginAddress + symbol.allocAddress, symbol.value);        
                            break;
                    }
                    */
                    for (Integer ref : refList) {
                        OzUtils.storeIntToByteArray(image, ref + codeOriginAddress, symbol.allocAddress);        
                    }
                }
                return image;
        }

}