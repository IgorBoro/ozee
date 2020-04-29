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
    private char lookAhead;

    public OzScanner(final OzText text){
        this.text = text;
        nextChar(); // seed reading
    }


    int nextLexeme(){
        skipSpaces();
        text.loc.lexemePos = text.loc.pos;

        if( Character.isLetter(lookAhead) ) {
            getName();
            return lexNAME;
        } else if( Character.isDigit(lookAhead) || lookAhead == '.' ) {
            getNumber();
            return lexNUMBER;
        } else
        switch(lookAhead){
            case ';':
                nextChar();
                return lexSEMICOLON;
            case '+':
                nextChar();
                return lexPLUS;
            case '-':
                nextChar();
                return lexMINUS;
            case '*':
                nextChar();
                return lexMUL;
            case '(':
                nextChar();
                return lexLPAREN;
                case ')':
                nextChar();
                return lexRPAREN;
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
                return lexUNDEF;
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

    private void getNumber() {
        while( Character.isDigit(lookAhead) || lookAhead == '.' ){
            nextChar();
        }
    }

    private void getName() {
        while( Character.isLetterOrDigit(lookAhead) ){
            nextChar();
        }
    }
}