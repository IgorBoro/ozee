package com.borsoftlab.ozee;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

                // набор модифицированных ссылок ==
                Set<Integer> modDataSegmentRefs = new TreeSet<Integer>();

                // здесь заполняется новый модифицированный список

                // сперва в новый список добавляем модифицированные ссылки на размер смещения сегмента кода
                for (Integer ref : symbolTable.dataSegmentRefs) {
                    modDataSegmentRefs.add(ref + codeOriginAddress);
                }

                // initialize the data section
                              
                // re-binding refs
                for (Symbol symbol : symbols) {
                    if( symbol.lexeme == OzScanner.lexVARNAME) {

                       // редактируем значение ссылочного типа (массив)
                        if( symbol.isArray ){
                            symbol.refValue += codeSegmentSize;      
                            /*
                             * Проверяем есть ли у массива размер и размер массива записываем только
                             * когда он не равен нулю.
                             * Потому-что переменная создана, но память под массив может быть не распределена!
                             */
                            if( symbol.arraySize != 0 ){
                                OzUtils.storeIntToByteArray(image, symbol.refValue, symbol.arraySize);                     
                            }

//                            // для массива добавляем две ссылки относящиеся к сегменту данных
//                            // 
//                            modSymbolRefs.add(symbol.allocAddress);
//                            modSymbolRefs.add(symbol.refValue);
                            OzUtils.storeIntToByteArray(image, symbol.allocAddress, symbol.refValue);                     

                            /*
                            switch(symbol.sizeInBytes){
                                case 4:
                                OzUtils.storeIntToByteArray  (image, symbol.allocAddress, symbol.refValue);        
                                break;
                                case 2:
                                OzUtils.storeShortToByteArray(image, symbol.allocAddress, symbol.refValue);        
                                break;
                                case 1:
                                OzUtils.storeByteToByteArray (image, symbol.allocAddress, symbol.refValue);        
                                break;
                            }
                            */
                        }
  
                        /*
                        List<Integer> refList = symbol.refList;
                        for( int i = 0; i < refList.size(); i++){
                            int ref = codeOriginAddress + refList.get(i);
                            OzUtils.storeIntToByteArray(image, ref, symbol.allocAddress);        
                            refList.set(i, ref);
                        }
                        */
                    }
                }

                symbolTable.dataSegmentRefs = modDataSegmentRefs;

                // получили новый список модифицированных ссылок на сегмент данных- правим память
                for (Integer ref : symbolTable.dataSegmentRefs) {
                    int val = OzUtils.fetchIntFromByteArray(image, ref) + codeSegmentSize;
                    // модифицируем содержимое памяти по ссылкам
                    OzUtils.storeIntToByteArray(image, ref, val);        
                }

                // ==


                return image;
        }

}