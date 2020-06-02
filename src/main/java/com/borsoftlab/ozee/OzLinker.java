package com.borsoftlab.ozee;

import java.util.List;

import com.borsoftlab.ozee.OzSymbols.Symbol;

public class OzLinker {

        public static byte[] linkImage(final byte[] program, final OzSymbols symbolTable){

                // a header of image is empty
                int headerSize = 0;
                int codeOriginAddress = 4 + headerSize;

                int progSize = program.length;     
                int dataSegmentSize = symbolTable.usedMemory;

                List<Symbol> symbols = symbolTable.getTableOrderedByAddr();
                int codeSegmentSize = codeOriginAddress + progSize;
                // calculate image size
                for (Symbol symbol : symbols) {
                        if( symbol.lexeme == OzScanner.lexVARNAME) {
                            symbol.allocAddress += codeSegmentSize;    
                        }
                       
                        /*
                         * does not take into account the size of arrays    
                         * dataSectionSize += symbol.sizeInBytes;
                         */
                }
                int imageSize = codeSegmentSize + dataSegmentSize;

                // create the empty image
                byte[] image = new byte[imageSize];

                // put the start address at the beginning of the image
                OzUtils.storeIntToByteArray(image, 0, codeOriginAddress);

                // copy the program to the image
                for (int targetImageAddress = codeOriginAddress;
                         targetImageAddress < codeOriginAddress + progSize;
                         targetImageAddress++) {
                        image[targetImageAddress] = program[targetImageAddress - codeOriginAddress];
                }

                // initialize the data section
                              
                // re-binding refs
                for (Symbol symbol : symbols) {
                    if( symbol.lexeme == OzScanner.lexVARNAME) {

                       // редактируем значение ссылочного типа (массив)
                        if( symbol.varType == OzScanner.VAR_TYPE_INT_ARRAY ){
                            symbol.value += codeSegmentSize;      
                            OzUtils.storeIntToByteArray(image, symbol.value, symbol.arraySize);                     
                        }
                                
                        switch(symbol.sizeInBytes){
                            case 4:
                                OzUtils.storeIntToByteArray  (image, symbol.allocAddress, symbol.value);        
                                break;
                            case 2:
                                OzUtils.storeShortToByteArray(image, symbol.allocAddress, symbol.value);        
                                break;
                            case 1:
                                OzUtils.storeByteToByteArray (image, symbol.allocAddress, symbol.value);        
                                break;
                        }
                    
                        List<Integer> refList = symbol.refList;
                        for (Integer ref : refList) {
                            OzUtils.storeIntToByteArray(image, codeOriginAddress + ref, symbol.allocAddress);        
                        }
                    }
                }
                return image;
        }

}