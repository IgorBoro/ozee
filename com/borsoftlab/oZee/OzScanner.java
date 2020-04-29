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

    public int lexeme;
    private OzText text;

    public OzScanner(final OzText text){
        this.text = text;
        text.nextChar(); // seed reading
    }

    int nextLexeme(){
        skipSpaces();
        text.loc.lexemePos = text.loc.pos;

        if( Character.isLetter(text.lookAhead) ) {
            getName();
            return lexNAME;
        } else if( Character.isDigit(text.lookAhead) || text.lookAhead == '.' ) {
            getNumber();
            return lexNUMBER;
        } else
        switch(text.lookAhead){
            case ';':
                text.nextChar();
                return lexSEMICOLON;
            case '+':
                text.nextChar();
                return lexPLUS;
            case '-':
                text.nextChar();
                return lexMINUS;
            case '*':
                text.nextChar();
                return lexMUL;
            case '(':
                text.nextChar();
                return lexLPAREN;
                case ')':
                text.nextChar();
                return lexRPAREN;
            case '/':
                text.nextChar();
                if( text.lookAhead == '*' ){
                    skipBlockComment();
                    return nextLexeme();
                } else if( text.lookAhead == '/'){
                    skipLineComment();
                    return nextLexeme();
                } else {
                    return lexDIV;
                }
            case 0:
                return lexEOF;
            default:
                text.nextChar();
                return lexUNDEF;
        }
    }

    private void skipSpaces() {
        while (Character.isSpaceChar(text.lookAhead)) {
            text.nextChar();
        }
    }

    private void skipLineComment() {
        while( text.lookAhead != '\n'){
            text.nextChar();
        }
    }

    private void skipBlockComment() {
        text.nextChar();
        do{
            while(text.lookAhead != '*' && text.lookAhead != 0 ) {
                if( text.lookAhead == '/') {
                    text.nextChar();
                    if( text.lookAhead == '*' ){
                        skipBlockComment();
                    }
                } else {
                    text.nextChar();
                }
            }
            if( text.lookAhead == '*' ){
                text.nextChar();
            }

        } while( text.lookAhead != '/' && text.lookAhead != 0 );
        if( text.lookAhead == '/'){
            text.nextChar();
        } else {
            text.loc.lexemePos = text.loc.pos;
            System.out.println("\nError! Unclosed comment!");
        }
    }

    private void getNumber() {
        while( Character.isDigit( text.lookAhead ) || text.lookAhead == '.' ){
            text.nextChar();
        }
    }

    private void getName() {
        while( Character.isLetterOrDigit( text.lookAhead ) ){
            text.nextChar();
        }
    }
}