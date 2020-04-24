package com.borsoftlab.oZee;

public class Parser{

    Scanner scanner;

    public Parser(final Scanner scanner){
        this.scanner = scanner;
    }
    
    public void compile(){

        int lexeme  = scanner.nextLexeme();
        while(lexeme != 0 ){
            lexeme  = scanner.nextLexeme();
        }
    }

}