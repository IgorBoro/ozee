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
    private OzIntStack tsStack = new OzIntStack( 64 );


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
            declareVarAndAssignStmt();
        }
        else if( scanner.lookAheadLexeme == OzScanner.lexVARNAME) {
            assignStmt(); // TO DO
        }
        else {
            expression(); // it will be not always
        } 
    }

    private void declareVarAndAssignStmt() throws Exception {
        int varType = varType();
        OzSymbols.Symbol symbol = newVariable(varType);
        match(OzScanner.lexVARNAME, "variable name");
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
        scanner.symbol.allocateVariable(varType);
        return scanner.symbol;
    }

    private OzSymbols.Symbol variable() throws Exception {
        if( scanner.symbol.varType == OzScanner.VAR_TYPE_UNDEF ){
            OzCompileError.message(scanner, "name '" + scanner.symbol.name + "' not defined",
            scanner.loc);
        }
        return scanner.symbol;
    }

    public void assignStmt() throws Exception {
        OzSymbols.Symbol symbol = variable();
        match(OzScanner.lexVARNAME, "variable name");
        assignExpression(symbol);
    }
    
    private void assignExpression(OzSymbols.Symbol symbol) throws Exception {
        match(OzScanner.lexASSIGN, "'='");
        expression();
        assign(symbol);
    }

    private void assign(OzSymbols.Symbol symbol){
        genCodeConvertTypeAssign(tsStack.pop(), symbol.varType);
        emit(OzVm.OPCODE_PUSH, symbol);
        emit(OzVm.OPCODE_ASGN);
    }

    public void expression() throws Exception {
        term();
        while(true) {
            switch( scanner.lookAheadLexeme ){
                case OzScanner.lexPLUS:
                    add();
                    break;
                case OzScanner.lexMINUS:
                    sub();
                    break;
                default:
                    return;
            }
        }
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

    private void factor() throws Exception {
        boolean unaryMinus = false;
        if( scanner.lookAheadLexeme == OzScanner.lexMINUS){
            match(OzScanner.lexMINUS, "unexpected lexeme");
            unaryMinus = true;
        };
        if( scanner.lookAheadLexeme == OzScanner.lexLPAREN){
            match(OzScanner.lexLPAREN, "(");
            expression();
            match(OzScanner.lexRPAREN, ")");
        } else {
            switch(scanner.lookAheadLexeme){
                case OzScanner.lexNUMBER:
                    match(OzScanner.lexNUMBER, "number");
                    if( scanner.varType == OzScanner.VAR_TYPE_INT) {
                        emit(OzVm.OPCODE_PUSH, scanner.intNumber);
                    }
                    else {
                        emit(OzVm.OPCODE_PUSH, scanner.floatNumber);
                    }
                    tsStack.push(scanner.varType);
                    break;
                case OzScanner.lexVARNAME:
                    OzSymbols.Symbol symbol = variable();
                    match(OzScanner.lexVARNAME, "variable name");                    
                    emit(OzVm.OPCODE_PUSH, symbol);
                    if( symbol.varType == OzScanner.VAR_TYPE_BYTE || symbol.varType == OzScanner.VAR_TYPE_UBYTE) {
                        emit(OzVm.OPCODE_EVALB);
                        if( symbol.varType == OzScanner.VAR_TYPE_BYTE ){
                            emit( OzVm.OPCODE_PUSH, 24 );
                            emit( OzVm.OPCODE_ASL );
                            emit( OzVm.OPCODE_PUSH, 24 );
                            emit( OzVm.OPCODE_ASR );
                        }
                    } else if( symbol.varType == OzScanner.VAR_TYPE_SHORT || symbol.varType == OzScanner.VAR_TYPE_USHORT) {
                        emit(OzVm.OPCODE_EVALS);
                        if( symbol.varType == OzScanner.VAR_TYPE_SHORT ) {
                            emit( OzVm.OPCODE_PUSH, 16 );
                            emit( OzVm.OPCODE_ASL );
                            emit( OzVm.OPCODE_PUSH, 16 );
                            emit( OzVm.OPCODE_ASR );
                        }
                    } else {
                        emit(OzVm.OPCODE_EVAL);
                    }
                    tsStack.push(symbol.varType);
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

    private void add() throws Exception {
        match(OzScanner.lexPLUS, "'+'");
        term();
        switch (genCodeConvertTypeBinOp()){
            case OzScanner.VAR_TYPE_BYTE:
            case OzScanner.VAR_TYPE_UBYTE:
            case OzScanner.VAR_TYPE_SHORT:
            case OzScanner.VAR_TYPE_USHORT:
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_ADD);
                break;
            case OzScanner.VAR_TYPE_FLOAT:    
                emit(OzVm.OPCODE_ADDF);
            break;
            default:
        }
    }

    private void sub() throws Exception {
        match(OzScanner.lexMINUS, "'-'");
        term();
        genCodeConvertTypeBinOp();
        switch (genCodeConvertTypeBinOp()){
            case OzScanner.VAR_TYPE_BYTE:
            case OzScanner.VAR_TYPE_UBYTE:
            case OzScanner.VAR_TYPE_SHORT:
            case OzScanner.VAR_TYPE_USHORT:
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_SUB);
                break;
            case OzScanner.VAR_TYPE_FLOAT:    
                emit(OzVm.OPCODE_SUBF);
            break;
            default:
        }
    }

    private void mul() throws Exception {
        match(OzScanner.lexMUL, "'*'");
        factor();
        genCodeConvertTypeBinOp();
        switch (genCodeConvertTypeBinOp()){
            case OzScanner.VAR_TYPE_BYTE:
            case OzScanner.VAR_TYPE_UBYTE:
            case OzScanner.VAR_TYPE_SHORT:
            case OzScanner.VAR_TYPE_USHORT:
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_MUL);
                break;
            case OzScanner.VAR_TYPE_FLOAT:    
                emit(OzVm.OPCODE_MULF);
            break;
            default:
        }
    }

    private void div() throws Exception {
        match(OzScanner.lexDIV, "'/'");
        factor();
        genCodeConvertTypeBinOp();
        switch (genCodeConvertTypeBinOp()){
            case OzScanner.VAR_TYPE_BYTE:
            case OzScanner.VAR_TYPE_UBYTE:
            case OzScanner.VAR_TYPE_SHORT:
            case OzScanner.VAR_TYPE_USHORT:
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_DIV);
                break;
            case OzScanner.VAR_TYPE_FLOAT:    
                emit(OzVm.OPCODE_DIVF);
            break;
            default:
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
    
    private void genCodeConvertTypeAssign(int srcType, int dstType){
        if( srcType != dstType ){
            if(
                (srcType == OzScanner.VAR_TYPE_INT ||
                srcType == OzScanner.VAR_TYPE_SHORT ||
                srcType == OzScanner.VAR_TYPE_BYTE ) &&
                dstType == OzScanner.VAR_TYPE_FLOAT
                ) {
                    emit(OzVm.OPCODE_FLT);

            } else if ((dstType == OzScanner.VAR_TYPE_INT ||
                dstType == OzScanner.VAR_TYPE_SHORT ||
                dstType == OzScanner.VAR_TYPE_BYTE ) &&
                srcType == OzScanner.VAR_TYPE_FLOAT ) {
                    emit(OzVm.OPCODE_INT);

            } else {

            }
        }
    }

    private int genCodeConvertTypeBinOp(){
        int topType    = tsStack.pop();
        int subTopType = tsStack.pop();
      //  if( )
 
      return 0;
    }

}