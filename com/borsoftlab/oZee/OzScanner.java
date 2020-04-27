package com.borsoftlab.oZee;

public class OzScanner{

    public int lexeme;
    private Text text;
    private char lookAhead;

    public OzScanner(final Text text){
        this.text = text;
    //    nextChar();
    }


    int nextLexeme(){
        skipSpaces();
        text.loc.lexemePos = text.loc.pos;
        nextChar();
        if( lookAhead == 0 )
            return 0;
        else {
            return 1;
        }
    }

    private void skipSpaces() {
        while (Character.isSpaceChar(lookAhead))
            nextChar();
    }

    private void nextChar(){
        lookAhead = (char) text.nextChar();
    }
    
}