package com.borsoftlab.oZee;

public class OzParser{

    OzScanner scanner;
    int aheadLexeme = 0;
    byte[] memory = new byte [12];
    int pc = 0;

    public OzParser(final OzScanner scanner){
        this.scanner = scanner;
        // nextLexeme();
    }
    
    public void compile(){
        pc = 0;
        nextLexeme();
        while(aheadLexeme != 0 ){
            nextLexeme();
        }
    }

    private void nextLexeme(){
        aheadLexeme = scanner.nextLexeme();
    }

    public byte[] getExecMemModule(){
        int value = 1234567890;
        memory[pc++] = OzVm.OPCODE_PUSH;
        memory[pc++] = (byte)  (value & 0x000000FF);
        memory[pc++] = (byte) ((value & 0x0000FF00) >>  8);
        memory[pc++] = (byte) ((value & 0x00FF0000) >> 16);
        memory[pc++] = (byte) ((value & 0xFF000000) >> 24);
        memory[pc++] = OzVm.OPCODE_STOP;
        return memory;
    }
}