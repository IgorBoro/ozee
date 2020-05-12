package com.borsoftlab.ozee;

import java.util.Locale;

import com.borsoftlab.ozee.OzSymbols.Symbol;

public class OzParser {

    OzScanner scanner;
    int aheadLexeme = 0;
    byte[] mem = new byte [128];
    int pc = 0;

    /*
    * Type support stack
    */
    private IntStack tsStack = new IntStack( 64 );


    public OzParser(){
    }
   
	public void compile(final OzScanner scanner) throws Exception {
        this.scanner = scanner;
        OzCompileError.reset();
        pc = 0;
        scanner.nextLexeme();
        stmtList();


        final int value = 1234567890;
        mem[pc++] = OzVm.OPCODE_PUSH;
        OzUtils.storeIntToByteArray(mem, pc, value);
        pc += 4;
        mem[pc++] = OzVm.OPCODE_STOP;


        emitListing(OzVm.OPCODE_STOP);
    }

    void stmtList() throws Exception {
        while( scanner.lookAheadLexeme != OzScanner.lexEOF ){
    //        System.out.print(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;  ");
    //        System.out.printf("Maintenance type stack size is: %d\n", typeStack.size());
            stmt();
            match(OzScanner.lexSEMICOLON);
        }
    }

    void stmt() throws Exception {
        if( scanner.lookAheadLexeme == OzScanner.lexVARTYPE) {
            declareVarStmt();
        }
        else if( scanner.lookAheadLexeme == OzScanner.lexVARNAME) {
            assignStmt(); // TO DO
        }
        else {
            expression(); // it will be not always
        } 
    }

    private void declareVarStmt() throws Exception {
        int varType = varType();
        OzSymbols.Symbol symbol = newVariable(varType);
        if( scanner.lookAheadLexeme == OzScanner.lexASSIGN){
            assignExpression(symbol);
        } else if( scanner.lookAheadLexeme == OzScanner.lexSEMICOLON ) {
            // empty
        } else if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
            OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
        } else {
            OzCompileError.expected(scanner, "'=' or ';'", scanner.loc);
        }
    }

    private int varType() throws Exception {
        match(OzScanner.lexVARTYPE, "var type definition");
        int varType = scanner.varType;
        return varType;
    }

    private OzSymbols.Symbol newVariable(int varType) throws Exception {
        match(OzScanner.lexVARNAME, "variable name");
        OzSymbols.Symbol symbol = scanner.symbol;
        symbol.allocateVariable(varType);
        return symbol;
    }

    private OzSymbols.Symbol variable() throws Exception {
        Location loc = new Location(scanner.loc);
        match(OzScanner.lexVARNAME, "variable name");
        OzSymbols.Symbol symbol = scanner.symbol;
        if( symbol.varType == OzScanner.VAR_TYPE_UNDEF ){
            OzCompileError.message(scanner, "variable '" + symbol.name + "' not defined",
            loc);
        }
        return symbol;
    }

    public void assignStmt() throws Exception {
        OzSymbols.Symbol symbol = variable();
        if( scanner.lookAheadLexeme == OzScanner.lexASSIGN){
            assignExpression(symbol);
        } else if( scanner.lookAheadLexeme == OzScanner.lexSEMICOLON ) {
            // empty
        } else  if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
            OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
        } else {
            OzCompileError.expected(scanner, "'='", scanner.text.loc);
        }
    }
    
    private void assignExpression(OzSymbols.Symbol symbol) throws Exception {
        match(OzScanner.lexASSIGN, "'='");
        expression();
        emit(OzVm.OPCODE_PUSH, symbol);
        emit(OzVm.OPCODE_ASGN);
    }
   
    public void expression() throws Exception {
        term();
        while(true) {
            switch( scanner.lookAheadLexeme ){
                case OzScanner.lexPLUS:
                    sum();
                    break;
                case OzScanner.lexMINUS:
                    sub();
                    break;
                default:
                    return;
            }
        }
    }

    private void sum() throws Exception {
        match(OzScanner.lexPLUS, "'+'");
        term();
        emit(OzVm.OPCODE_ADD);
    }

    private void sub() throws Exception {
        match(OzScanner.lexMINUS, "'-'");
        term();
        emit(OzVm.OPCODE_SUB);
    }

    private void term() throws Exception {
        factor();
        while(true) {
            switch( scanner.lookAheadLexeme ){
                case OzScanner.lexMUL:
                    mul();
                    break;
                case OzScanner.lexDIV:
                    div();
                    break;
                default:
                    return;
            }
        }
    }    

    private void div() throws Exception {
        match(OzScanner.lexDIV, "'/'");
        factor();
        emit(OzVm.OPCODE_DIV);
    }

    private void mul() throws Exception {
        match(OzScanner.lexMUL, "'*'");
        factor();
        emit(OzVm.OPCODE_MUL);
    }

    private void factor() throws Exception {
        boolean unaryMinus = false;
        if( scanner.lookAheadLexeme == OzScanner.lexMINUS){
            match(OzScanner.lexMINUS, "unexpected lexeme");
            unaryMinus = true;
        };
        if( scanner.lookAheadLexeme == OzScanner.lexLPAREN){
            scanner.nextLexeme();
            expression();
            match(OzScanner.lexRPAREN, ")");
        } else {
            switch(scanner.lookAheadLexeme){
                case OzScanner.lexNUMBER:
                    scanner.nextLexeme();
                    tsStack.push(scanner.varType);
                    if( scanner.varType == OzScanner.VAR_TYPE_INT) {
                        emit(OzVm.OPCODE_PUSH, scanner.intNumber);
                    }
                    else {
                        emit(OzVm.OPCODE_PUSH, scanner.floatNumber);
                    }
                    break;
                /*    
                case Scanner.TOKEN_BUILTIN:
                    match(Scanner.TOKEN_BUILTIN);
                    int builtinFunc = scanner.getSymbol().getType();
                    match(Scanner.TOKEN_LPAREN);
                    expression();
                    match(Scanner.TOKEN_RPAREN);
                    builtin(builtinFunc);
                    break;
                */    
                case OzScanner.lexVARNAME:
                    /*
                    scanner.nextLexeme();
                    OzSymbols.Symbol symbol = scanner.symbol;
                    int symbolType = symbol.varType;
                    */
                    OzSymbols.Symbol symbol = variable();
                    /*
                    switch (symbolType){
                        case OzScanner.VARTYPE_INT:
                        case OzScanner.VARTYPE_SHORT:
                        case OzScanner.VARTYPE_BYTE:
                        case OzScanner.VARTYPE_FLOAT:
                        case Scanner.DATA_TYPE_INT2:
                        case Scanner.DATA_TYPE_UINT1:
                        case Scanner.DATA_TYPE_UINT2:
                        symbolType = Scanner.DATA_TYPE_INT4;
                        break;
                    }
                    */
                    tsStack.push(symbol.varType);
                    emit(OzVm.OPCODE_PUSH, symbol);
                    emit(OzVm.OPCODE_EVAL);
                    break;
                case OzScanner.lexEOF:
                break ;   
                default:
                    OzCompileError.message(scanner, "unexpected lexeme", scanner.loc);    
            }
        }
        if( unaryMinus ) {
            emit(OzVm.OPCODE_NEG);
        }
    }

    private void emit(byte opcode){
        emitListing(opcode);
        emitMem(opcode);
    }

    private void emit(byte opcode, final int arg){
        emitListing(opcode, arg);
        emitMem(opcode, arg);
    }

    private void emit(byte opcode, final float arg){
        emitListing(opcode, arg);
        emitMem(opcode, arg);
    }

    private void emit(byte opcode, final Symbol sym){
        emitListing(opcode, sym);
        emitMem(opcode, sym);
    }

    private void emitMnemonicList(byte opcode){
        String mnemonic = OzAsm.getInstance().getMnemonic(opcode);
        System.out.print(String.format("        %s", mnemonic));
    }

    private void emitHexList(byte opcode){
        System.out.print(String.format("0x%04X: 0x%02X", pc, opcode));
    }

    private void emitListing(byte opcode) {
        emitMnemonicList(opcode);
        System.out.println();

        emitHexList(opcode);
        System.out.println();

    }

    private void emitListing(byte opcode, final int arg) {
        emitMnemonicList(opcode);
        System.out.println(String.format(" %d", arg));

        emitHexList(opcode);
        System.out.println(String.format(" 0x%08X", arg));
    }

    private void emitListing(byte opcode, final float arg) {
        emitMnemonicList(opcode);
        System.out.println(String.format(Locale.US, " %f", arg));

        int i = Float.floatToIntBits(arg);
        emitHexList(opcode);
        System.out.println(String.format(" 0x%08X",  i));
    }

    private void emitListing(byte opcode, final Symbol sym) {
        emitMnemonicList(opcode);
        System.out.println(String.format(" %s", sym.name));

        emitHexList(opcode);
        System.out.println(String.format(" 0x%08X", sym.allocAddress));
    }

    private void emitMem(byte opcode){
        mem[pc++] = opcode;
    }

    private void emitMem(byte opcode, int arg){
        mem[pc++] = opcode;
        OzUtils.storeIntToByteArray(mem, pc, arg);
        pc += 4;
    }

    private void emitMem(byte opcode, float arg){
        emitMem(opcode, Float.floatToIntBits(arg));
    }

    private void emitMem(byte opcode, final Symbol sym){
        emitMem(opcode, sym.allocAddress);
    }

    public byte[] getExecMemModule() {
        final int value = 1234567890;
        mem[pc++] = OzVm.OPCODE_PUSH;
        OzUtils.storeIntToByteArray(mem, pc, value);
        pc += 4;
        mem[pc++] = OzVm.OPCODE_STOP;
        return mem;
    }

    private void match(final int lexeme, final String msg) throws Exception {
        if( scanner.lookAheadLexeme == lexeme ){
            scanner.nextLexeme();
        } else  if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
            OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
        } else {
            OzCompileError.expected(scanner, msg, scanner.loc);
        }
    }     

    private void match(final int lexeme) throws Exception {
        if( scanner.lookAheadLexeme == lexeme ){
            scanner.nextLexeme();
        } else  if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
            OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
        } else {
            OzCompileError.message(scanner, "unexpected lexeme", scanner.loc);
        }
    }     

}