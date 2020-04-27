package com.borsoftlab.oZee;

public class OzParser{

    OzScanner scanner;
    int aheadLexeme = 0;

    public OzParser(final OzScanner scanner){
        this.scanner = scanner;
        // nextLexeme();
    }
    
    public void compile(){
        nextLexeme();
        while(aheadLexeme != 0 ){
            nextLexeme();
        }
    }

    private void nextLexeme(){
        aheadLexeme = scanner.nextLexeme();
    }
}