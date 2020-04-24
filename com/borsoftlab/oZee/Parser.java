package com.borsoftlab.oZee;

public class Parser{

    Scanner scanner;
    int aheadLexeme = 0;

    public Parser(final Scanner scanner){
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