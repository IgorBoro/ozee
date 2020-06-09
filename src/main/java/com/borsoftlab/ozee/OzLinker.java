package com.borsoftlab.ozee;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.borsoftlab.ozee.OzSymbols.Symbol;

public class OzLinker {

    public static byte[] linkImage(final byte[] program, final OzSymbols symbolTable){

        int codeSegmentOriginAddress = 0;
        int progSize = program.length;     
        int dataSegmentSize = symbolTable.usedMemory;

        List<Symbol> symbols = symbolTable.getTableOrderedByAddr();
        int codeSegmentSize = codeSegmentOriginAddress + progSize;
        int dataSegmentOriginAddress = codeSegmentSize;

        int imageSize = codeSegmentSize + dataSegmentSize;

        // в каждой записи таблицы символов меняем адрес переменной на новый с учетом
        // смещения сегмента данных - это нужно только для того, чтобы после выполнения
        // программы найти нужную переменную и ее адрес соответствовал реальному
        // размещению переменной после перемещения сегмента данных
        // в будущем этот кусок кода уйдет!!!!!!!!
        for (Symbol symbol : symbols) {
            if( symbol.lexeme == OzScanner.lexVARNAME) {
                symbol.allocAddress += dataSegmentOriginAddress;    
            }
        }

        // create the empty image
        byte[] image = new byte[imageSize];
        // копируем программу в образ
        System.arraycopy(program, 0, image, 0, program.length);

        // модифицируем ссылки
        Set<Integer> modDataSegmentRefs = new TreeSet<Integer>();

        // здесь заполняется новый модифицированный список
        // сперва в новый список добавляем модифицированные ссылки на размер смещения сегмента кода

        for (Integer ref : symbolTable.dataSegmentRefs) {
            modDataSegmentRefs.add(ref + codeSegmentOriginAddress);
        }

        symbolTable.dataSegmentRefs = modDataSegmentRefs;

        // получили новый список модифицированных ссылок на сегмент данных- правим память
        for (Integer ref : symbolTable.dataSegmentRefs) {
            int value = dataSegmentOriginAddress + OzUtils.fetchIntFromByteArray(image, ref);
            // модифицируем содержимое памяти по ссылкам
            OzUtils.storeIntToByteArray(image, ref, value);        
        }

        // модифицируем память по ссылкам
        for (Symbol symbol : symbols) {
            if( symbol.lexeme == OzScanner.lexVARNAME) {
                // редактируем значение ссылочного типа (массив)
                if( symbol.isArray ) {
                    symbol.refValue += codeSegmentSize;      
                    // Проверяем есть ли у массива размер и размер массива записываем только
                    // когда он не равен нулю.
                    // Потому-что переменная создана, но память под массив может быть не распределена!
                    if( symbol.arraySize != 0 ) {
                        OzUtils.storeIntToByteArray(image, symbol.refValue, symbol.arraySize);                     
                    }
                    // запись константы в память переменной
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
                }
            }
        }
        return image;
    }
}