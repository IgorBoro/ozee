package com.borsoftlab.ozee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class OzSymbols {

    private Map<String, Symbol> map = new HashMap<>();


    public Set<Integer> dataSegmentRefs = new TreeSet<Integer>();
    public Set<Integer> codeSegmentRefs = new TreeSet<Integer>();

    int usedMemory = 0;

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
            case OzScanner.VAR_TYPE_USHORT:
                return 2;
            case OzScanner.VAR_TYPE_BYTE:
                return 1;
            case OzScanner.VAR_TYPE_UBYTE:
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
                String sType = getVarTypeName(sym);
                System.out.println(String.format("%-24s %-12s %d  addr:0x%08X refVal:[0x%08X]",
                    sym.name, sType, sym.sizeInBytes, sym.allocAddress, sym.refValue));
            //    System.out.print(" refs:{ ");
            //    for (Integer ref : sym.refList) {
            //        System.out.print(String.format("0x%08X", ref));
            //        System.out.print(" ");
            //    }
            //    System.out.println("}");
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
                String sType = getVarTypeName(sym);
                System.out.println(String.format("0x%08X: %-24s %-12s %d refVal:[0x%08X]",
                    sym.allocAddress, sym.name, sType, sym.sizeInBytes, sym.refValue));
//                System.out.print(" refs:{ ");
//                for (Integer ref : sym.refList) {
//                    System.out.print(String.format("0x%08X", ref));
//                    System.out.print(" ");
//                }
//                System.out.println("}");
            }
        }
        System.out.println("; ============  SYMBOL TABLE DUMP BY ADDR END  =============");
    }

    private String getVarTypeName(Symbol sym) {
        String sType;
        switch(sym.varType){
            case OzScanner.VAR_TYPE_BYTE:
                sType = "byte";
                break;
            case OzScanner.VAR_TYPE_UBYTE:
                sType = "ubyte";
                break;
            case OzScanner.VAR_TYPE_SHORT:
                sType = "short";
                break;
            case OzScanner.VAR_TYPE_USHORT:
                sType = "ushort";
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

        if ( sym.isArray ){
            sType += " array";
        }
        return sType;
    }

    public List<Symbol> getTableOrderedByAddr(){
        ArrayList<Symbol> list = new ArrayList<Symbol>();

        final Map<String, Symbol> sortedMap = map.entrySet()
                .stream()
                .sorted((e1,e2) -> (e1.getValue().allocAddress - e2.getValue().allocAddress))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        for( Map.Entry<String, Symbol> e : sortedMap.entrySet()){
            list.add(e.getValue());
        }    

        return list;
    }

    public void dumpRefList(){
        for (Integer ref : dataSegmentRefs) {
            System.out.printf("0x%08X ", ref);
        }
        System.out.println();
    }

    public class Symbol{
        String name;
        int lexeme;
        int varType;
        int allocAddress;
        int sizeInBytes;
        int refValue;
        boolean isArray = false;
        int arraySize;

//        List<Integer> refList = new ArrayList<Integer>();

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
                if( isArray ){
                    sizeInBytes = 4;
                } else {
                    sizeInBytes = sizeOfType(varType);
                }
                allocAddress = usedMemory;
                usedMemory += sizeInBytes;
            }
        }

        public void allocateArray(int arraySize) {
            this.arraySize = arraySize;
            refValue = usedMemory;
            usedMemory += 4 + this.arraySize * sizeInBytes;
        }

        public void addDataSegmentRef(int ref){
            dataSegmentRefs.add(ref);
        }

    }

}