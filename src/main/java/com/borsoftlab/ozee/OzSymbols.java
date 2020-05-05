package com.borsoftlab.ozee;

import java.util.HashMap;
import java.util.Map;

public class OzSymbols {

    private Map<String, Symbol> map = new HashMap<>();


    public Symbol lookup(String key){
        return map.get(key);
    }

    public Symbol install(String name, int token, int type){
        Symbol symbol = new Symbol(name, token, type);
        map.put(name, symbol);
        return symbol;
    }

    public static class Symbol{
        String name;
        int lexeme;
        int varType;
        int locAddr;
        int sizeInBytes;

        public Symbol(String name, int lexeme, int type){
            this.name = name;
            this.lexeme = lexeme;
            setType( type );
        }

        public void setType(int type) {
            varType = type;
            sizeInBytes = sizeOfType(type);
        }
        public static int sizeOfType(int type){
            switch( type ){
                case OzScanner.VARTYPE_UNDEF:
                    return 0;
                case OzScanner.VARTYPE_INT:
                    return 4;
                case OzScanner.VARTYPE_SHORT:
                    return 2;
                case OzScanner.VARTYPE_BYTE:
                    return 1;
                case OzScanner.VARTYPE_FLOAT:
                    return 4;
                default:
                    return 0;
            }
        }
    
    }    
}