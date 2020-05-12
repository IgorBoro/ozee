package com.borsoftlab.ozee;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class OzSymbols {

    private Map<String, Symbol> map = new HashMap<>();
    int curAddress = 0;

    public Symbol lookup(String key){
        return map.get(key);
    }

    public Symbol install(String name, int lexeme, int nameType){
        Symbol symbol = new Symbol(name, lexeme, nameType);
        map.put(name, symbol);
        return symbol;
    }

    public static int sizeOfType(int type){
        switch( type ){
            case OzScanner.VAR_TYPE_UNDEF:
                return 0;
            case OzScanner.VAR_TYPE_INT:
                return 4;
            case OzScanner.VAR_TYPE_SHORT:
                return 2;
            case OzScanner.VAR_TYPE_BYTE:
                return 1;
            case OzScanner.VAR_TYPE_FLOAT:
                return 4;
            default:
                return 0;
        }
    }

    public void dumpSymbolTableByName(){
        System.out.println("; =========== SYMBOL TABLE DUMP BEGIN ===================");
//        Stream<Map.Entry<String, Symbol>> sorted = map.entrySet().stream().sorted(Map.Entry.comparingByValue());
        
        Map<String, Symbol> treeMap = new TreeMap<String, Symbol>(map);
        for( Map.Entry<String, Symbol> entry : treeMap.entrySet()){
            Symbol sym = entry.getValue();
            if( sym.lexeme == OzScanner.lexVARNAME){
                String sType;
                switch(sym.varType){
                    case OzScanner.VAR_TYPE_BYTE:
                        sType = "byte";
                        break;
                    case OzScanner.VAR_TYPE_SHORT:
                        sType = "short";
                        break;
                    case OzScanner.VAR_TYPE_INT:
                        sType = "int";
                        break;
                    case OzScanner.VAR_TYPE_FLOAT:
                        sType = "float";
                        break;
                    default:
                        sType = "unknown";    
                }
                System.out.println(String.format("%-24s  %5s %d  0x%08X", sym.name, sType, sym.sizeInBytes, sym.allocAddress));
            }
        }
        System.out.println("; ============  SYMBOL TABLE DUMP END   =================");
    }

    public class Symbol{
        String name;
        int lexeme;
        int varType;
        int allocAddress;
        int sizeInBytes;

        public Symbol(String name, int lexeme, int varType){
            this.name = name;
            this.lexeme = lexeme;
            this.varType = varType;
        }

        public void allocateVariable(int varType) {
            if( this.varType == OzScanner.VAR_TYPE_UNDEF){
                this.varType = varType;
            }
            if( lexeme == OzScanner.lexVARNAME ){
                sizeInBytes = sizeOfType(varType);
                allocAddress = curAddress;
                curAddress += sizeInBytes;
            }
        }
    }    

}