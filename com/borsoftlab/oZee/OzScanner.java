package com.borsoftlab.oZee;

public class OzScanner{

    public static final int lexUNDEF       = -1;
    public static final int lexEOF         =  0;
    public static final int lexNAME        =  1;
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

    public static final int VARTYPE_UNDEF  = 0;
    public static final int VARTYPE_INT      = 1;
    public static final int VARTYPE_SHORT    = 2;
    public static final int VARTYPE_BYTE     = 3;
    public static final int VARTYPE_FLOAT    = 4;

    public int lookAheadLexeme;
    public OzText text;

    OzSymbols symbolTable = new OzSymbols();
    OzSymbols.Symbol symbol = null;

    int numberType = VARTYPE_UNDEF;
    public int intNumber = 0;
    public float floatNumber = 0;

    public static final int IDENT_MAX_SIZE = 32;
    char[] identBuffer = new char[IDENT_MAX_SIZE];


    public OzScanner(final OzText text){

        symbolTable.install( "int",   lexVARTYPE, VARTYPE_INT   );
        symbolTable.install( "short", lexVARTYPE, VARTYPE_SHORT );
        symbolTable.install( "byte",  lexVARTYPE, VARTYPE_BYTE  );
        symbolTable.install( "float", lexVARTYPE, VARTYPE_FLOAT );

        this.text = text;
        text.nextChar(); // seed reading
    }

    void nextLexeme(){
        skipSpaces();
        text.loc.lexemeLine = text.loc.line;
        text.loc.lexemePos = text.loc.pos;

        if( Character.isLetter(text.lookAheadChar) ) {
            getName();
        } else if( Character.isDigit(text.lookAheadChar) || text.lookAheadChar == '.' ) {
            getNumber();
        } else
        switch(text.lookAheadChar){
            case ';':
                text.nextChar();
                lookAheadLexeme = lexSEMICOLON;
                break;
                case '=':
                text.nextChar();
                lookAheadLexeme = lexASSIGN;
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
                    return;
                } else if( text.lookAheadChar == '/'){
                    skipLineComment();
                    nextLexeme();
                    return;
                } else {
                    lookAheadLexeme = lexDIV;
                }
                break;
            case 0:
                lookAheadLexeme = lexEOF;
                return;
            default:
                OzCompileError.message(text, "Invalid character");
        }
        text.loc.lexemeCount++;
    }

    private void skipSpaces() {
        while (Character.isSpaceChar(text.lookAheadChar) || text.lookAheadChar == '\n') {
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
            text.loc.lexemeLine = text.loc.line;
            text.loc.lexemePos = text.loc.pos;
            OzCompileError.message( text, "Unclosed comment!" );
        }
    }

    private void getNumber() {
        intNumber = 0;
        do {
            intNumber = intNumber * 10 + (text.lookAheadChar - '0');
            text.nextChar();
        } while( text.lookAheadChar != '\0' && Character.isDigit(text.lookAheadChar));
        if( text.lookAheadChar == '.' ){
            text.nextChar();
            floatNumber = 0.0f;
            float k = 10.0f;
            do {
                floatNumber += (text.lookAheadChar - '0')/k;
                k *= 10.0f;
                text.nextChar();
            } while (text.lookAheadChar != '\0' && Character.isDigit(text.lookAheadChar));
            floatNumber += intNumber;
            numberType = VARTYPE_FLOAT;
        }  else {
            numberType = VARTYPE_INT;
        }
        lookAheadLexeme =  lexNUMBER;
    }

    private void getName() {
        int i = 0;
        do{
            if( i == IDENT_MAX_SIZE)
                break;
            identBuffer[i++] = (char) text.lookAheadChar;
            text.nextChar();
        }while (Character.isLetterOrDigit(text.lookAheadChar));
        String ident = String.valueOf(identBuffer, 0, i);
        symbol = symbolTable.lookup(ident);
        if(symbol == null){
            symbol = symbolTable.install(ident, lexNAME, VARTYPE_UNDEF);
        }
        lookAheadLexeme = symbol.lexeme;
    }
}