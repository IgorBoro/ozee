package com.borsoftlab.oZee;

public class OzScanner{

    public static final int lexEOF         =  0;
    public static final int lexNAME        =  1;
    public static final int lexNUMBER      =  2;
    public static final int lexSEMICOLON   = 11;
    public static final int lexDIV         = 12;

    public int lexeme;
    private OzText text;
    private char lookAhead;

    public OzScanner(final OzText text){
        this.text = text;
        nextChar(); // seed reading
    }


    int nextLexeme(){
        skipSpaces();
        text.loc.lexemePos = text.loc.pos;

        switch(lookAhead){
            case ';':
                nextChar();
                return lexSEMICOLON;
            case '/':
                nextChar();
                if( lookAhead == '*' ){
                    skipBlockComment();
                    return nextLexeme();
                } else if( lookAhead == '/'){
                    skipLineComment();
                    return nextLexeme();
                } else {
                    return lexDIV;
                }
            case 0:
                return lexEOF;
            default:
                nextChar();
            return 1;
        }
    }

    private void nextChar() {
        lookAhead = (char) text.nextChar();
    }

    private void skipSpaces() {
        while (Character.isSpaceChar(lookAhead)) {
            nextChar();
        }
    }

    private void skipLineComment() {
        while( lookAhead != '\n'){
            nextChar();
        }
    }

    private void skipBlockComment() {
        nextChar();
        do{
            while(lookAhead != '*' && lookAhead != 0 ) {
                if( lookAhead == '/') {
                    nextChar();
                    if( lookAhead == '*' ){
                        skipBlockComment();
                    }
                } else {
                    nextChar();
                }
            }
            if( lookAhead == '*' ){
                nextChar();
            }

        } while(lookAhead != '/' && lookAhead != 0);
        if( lookAhead == '/'){
            nextChar();
        } else {
            text.loc.lexemePos = text.loc.pos;
            System.out.println("\nError! Unclosed comment!");
        }
    }
    
}