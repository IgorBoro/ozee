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
        scanner.nextLexeme();

        stmtList();

        System.out.println("\n" + scanner.text.loc.lexemeCount + " lexemes processed");
    }

    void stmtList(){
        while( scanner.lookAheadLexeme != OzScanner.lexEOF ){
            scanner.nextLexeme();
        }
    }

    public byte[] getExecMemModule(){
        final int value = 1234567890;
        mem[pc++] = OzVm.OPCODE_PUSH;
        OzUtils.storeIntToByteArray(mem, pc, value);
        pc += 4;
        mem[pc++] = OzVm.OPCODE_STOP;
        return mem;
    }

    private void match(final int lexeme, final String msg) {
        if( scanner.lookAheadLexeme == lexeme ){
            scanner.nextLexeme();
        } else {
            OzCompileError.expected(scanner.text, msg);
        }
    }     
}