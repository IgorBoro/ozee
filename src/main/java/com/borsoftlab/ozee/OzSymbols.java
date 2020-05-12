package com.borsoftlab.ozee;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
        System.out.println("; ============= SYMBOL TABLE DUMP BY NAME BEGIN ============");
        
        Map<String, Symbol> sortedMap = new TreeMap<String, Symbol>(map);
        for( Map.Entry<String, Symbol> entry : sortedMap.entrySet()){
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
        System.out.println("; ============  SYMBOL TABLE DUMP BY NAME END  =============");
    }

    public void dumpSymbolTableByAddress(){
        System.out.println("; ============= SYMBOL TABLE DUMP BY ADDR BEGIN ============");

        final Map<String, Symbol> sortedMap = map.entrySet()
                .stream()
                .sorted((e1,e2) -> (e1.getValue().allocAddress - e2.getValue().allocAddress))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        for( Map.Entry<String, Symbol> entry : sortedMap.entrySet()){
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
                System.out.println(String.format("0x%08X  %-5s %d  %-24s", sym.allocAddress, sType, sym.sizeInBytes, sym.name));
            }
        }
        System.out.println("; ============  SYMBOL TABLE DUMP BY ADDR END  =============");
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