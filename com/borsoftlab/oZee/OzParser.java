package com.borsoftlab.oZee;

public class OzParser{

    OzScanner scanner;
    int aheadLexeme = 0;
    byte[] mem = new byte [12];
    int pc = 0;

    public OzParser(final OzScanner scanner){
        this.scanner = scanner;
    }
    
    public void compile(){
        pc = 0;
        int nLexeme = 0;
        scanner.nextLexeme();
        while( scanner.lookAheadLexeme != OzScanner.lexEOF ){
            nLexeme++;
            scanner.nextLexeme();

        }
        System.out.println("\n" + nLexeme + " lexeme processed");
    }

    public byte[] getExecMemModule(){
        final int value = 1234567890;
        mem[pc++] = OzVm.OPCODE_PUSH;
        OzUtils.storeIntToByteArray(mem, pc, value);
        pc += 4;
        mem[pc++] = OzVm.OPCODE_STOP;
        return mem;
    }
}