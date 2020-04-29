package com.borsoftlab.oZee;

public class OzScanner{

    public static final int lexUNDEF       = -1;
    public static final int lexEOF         =  0;
    public static final int lexNAME       =  1;
    public static final int lexNUMBER      =  2;
    public static final int lexPLUS        =  3;
    public static final int lexMINUS       =  4;
    public static final int lexMUL         =  5;
    public static final int lexDIV         =  6;
    public static final int lexLPAREN      =  7;
    public static final int lexRPAREN      =  8;
    public static final int lexASSIGN      =  9;
    public static final int lexCOMMA       = 10; 
    public static final int lexSEMICOLON   = 11;
    public static final int lexVARTYPE     = 12;

    public int lookAheadLexeme;
    public OzText text;

    public OzScanner(final OzText text){
        this.text = text;
        text.nextChar(); // seed reading
    }

    void nextLexeme(){
        skipSpaces();
        text.loc.lexemePos = text.loc.pos;

        if( Character.isLetter(text.lookAheadChar) ) {
            getName();
            lookAheadLexeme = lexNAME;
        } else if( Character.isDigit(text.lookAheadChar) || text.lookAheadChar == '.' ) {
            getNumber();
            lookAheadLexeme =  lexNUMBER;
        } else
        switch(text.lookAheadChar){
            case ';':
                text.nextChar();
                lookAheadLexeme = lexSEMICOLON;
                break;
            case '+':
                text.nextChar();
                lookAheadLexeme = lexPLUS;
                break;
            case '-':
                text.nextChar();
                lookAheadLexeme = lexMINUS;
                break;
            case '*':
                text.nextChar();
                lookAheadLexeme = lexMUL;
                break;
            case '(':
                text.nextChar();
                lookAheadLexeme = lexLPAREN;
                break;
            case ')':
                text.nextChar();
                lookAheadLexeme = lexRPAREN;
                break;
            case '/':
                text.nextChar();
                if( text.lookAheadChar == '*' ){
                    skipBlockComment();
                    nextLexeme();
                    text.loc.lexemeCount--;
                } else if( text.lookAheadChar == '/'){
                    skipLineComment();
                    nextLexeme();
                    text.loc.lexemeCount--;
                } else {
                    lookAheadLexeme = lexDIV;
                }
                break;
            case 0:
                lookAheadLexeme = lexEOF;
                break;
            default:
                text.nextChar();
                lookAheadLexeme = lexUNDEF;
        }
        text.loc.lexemeCount++;
    }

    private void skipSpaces() {
        while (Character.isSpaceChar(text.lookAheadChar)) {
            text.nextChar();
        }
    }

    private void skipLineComment() {
        while( text.lookAheadChar != '\n'){
            text.nextChar();
        }
    }

    private void skipBlockComment() {
        text.nextChar();
        do{
            while(text.lookAheadChar != '*' && text.lookAheadChar != 0 ) {
                if( text.lookAheadChar == '/') {
                    text.nextChar();
                    if( text.lookAheadChar == '*' ){
                        skipBlockComment();
                    }
                } else {
                    text.nextChar();
                }
            }
            if( text.lookAheadChar == '*' ){
                text.nextChar();
            }

        } while( text.lookAheadChar != '/' && text.lookAheadChar != 0 );
        if( text.lookAheadChar == '/'){
            text.nextChar();
        } else {
            text.loc.lexemePos = text.loc.pos;
            OzCompileError.message( text, "Unclosed comment!" );
        }
    }

    private void getNumber() {
        while( Character.isDigit( text.lookAheadChar ) || text.lookAheadChar == '.' ){
            text.nextChar();
        }
    }

    private void getName() {
        while( Character.isLetterOrDigit( text.lookAheadChar ) ){
            text.nextChar();
        }
    }
}