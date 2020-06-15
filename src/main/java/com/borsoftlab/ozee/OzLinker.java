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


        // ссылки на сегмент кода не модифицируем здесь, так как сегмент кода начинается с нуля!

        // в каждой записи таблицы символов меняем адрес переменной на новый с учетом
        // смещения сегмента данных - это нужно только для того, чтобы после выполнения
        // программы найти нужную переменную и ее адрес соответствовал реальному
        // размещению переменной после перемещения сегмента данных
        // заодно определимся с записями экспорта
        int exportCount = 0;
        int sizeOfExportArea = 0;
        for (Symbol symbol : symbols) {
            if( symbol.lexeme == OzScanner.lexVARNAME) {
                symbol.allocAddress += dataSegmentOriginAddress;    

                if( symbol.isExport ) {
                    exportCount++;
                    sizeOfExportArea += 9;
                    sizeOfExportArea += symbol.name.length();
                }
            }
        }
        /*
        if( exportCount > 0 ){
            // добавляем завершающий секцию экспорта 0
            sizeOfExportArea += 1;
        }
        */

        // размер одной записи модификатора = 5 байтов
        int sizeOfModArea = 5 * (symbolTable.codeSegmentRefs.size() + symbolTable.dataSegmentRefs.size() );

        // create the empty image
        byte[] image = new byte[imageSize + sizeOfModArea + sizeOfExportArea];
        // копируем программу в образ
        System.arraycopy(program, 0, image, 0, program.length);

        // пропишем адрес секции метаданных - пока метаданных нет размер образа прежний
        OzUtils.storeIntToByteArray(image, 6, imageSize);        

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

        int modPtr = imageSize;

        // отладочный вывод модификаторов ссылок
        for (Integer ref : symbolTable.codeSegmentRefs) {
            System.out.printf("M0x%08X ", ref);
            OzUtils.storeByteToByteArray(image, modPtr, 'M'); modPtr++;
            OzUtils.storeIntToByteArray(image, modPtr, ref);  modPtr += 4;
        }

        for (Integer ref : symbolTable.dataSegmentRefs) {
            System.out.printf("M0x%08X ", ref);
            OzUtils.storeByteToByteArray(image, modPtr, 'M'); modPtr++;
            OzUtils.storeIntToByteArray(image, modPtr, ref);  modPtr += 4;
        }
        System.out.println();

        if( exportCount > 0 ) {
            for (Symbol symbol : symbols) {
                if( symbol.lexeme == OzScanner.lexVARNAME) {
                    if( symbol.isExport ){
                        System.out.printf("E0x%08X %s %d ", symbol.allocAddress, symbol.name, symbol.varType);
                        if( symbol.isArray){
                            System.out.print("A ");
                        } else {
                            System.out.print("  ");
                        }

                        OzUtils.storeByteToByteArray(image, modPtr, 'E');                 modPtr++;
                        OzUtils.storeIntToByteArray(image, modPtr, symbol.allocAddress);  modPtr += 4;

                        // имя
                        for (char c : symbol.name.toCharArray()) {
                            OzUtils.storeByteToByteArray(image, modPtr, (byte)c);  modPtr++;
                        }
                        // завершающий имя 0
                        OzUtils.storeByteToByteArray(image, modPtr, (byte)0);  modPtr++;
                        // тип переменной
                        OzUtils.storeByteToByteArray(image, modPtr, symbol.varType);  modPtr++;

                        // массив или нет
                        if( symbol.isArray){
                            OzUtils.storeByteToByteArray(image, modPtr, 'A');  modPtr++;
                        } else {
                            OzUtils.storeByteToByteArray(image, modPtr, ' ');  modPtr++;
                        }

                        OzUtils.storeByteToByteArray(image, modPtr, (byte)0);  modPtr++;
                    }
                }   
            }
            // завершающий 0
            // OzUtils.storeByteToByteArray(image, modPtr, (byte)0);  modPtr++;
        }

        System.out.println();
        

        return image;
    }
}