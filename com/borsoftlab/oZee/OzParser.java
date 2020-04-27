package com.borsoftlab.oZee;

public class OzParser{

    Scanner scanner;
    int aheadLexeme = 0;

    public OzParser(final Scanner scanner){
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