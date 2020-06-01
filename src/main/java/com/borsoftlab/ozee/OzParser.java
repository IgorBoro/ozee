package com.borsoftlab.ozee;

import java.util.Locale;

import com.borsoftlab.ozee.OzSymbols.Symbol;

public class OzParser {

    OzScanner scanner;
    int aheadLexeme = 0;
    ByteArray mem = new ByteArray();
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
        emit(OzVm.OPCODE_STOP);
    }

    void stmtList() throws Exception {
        while( scanner.lookAheadLexeme != OzScanner.lexEOF ){
            if( tsStack.size() != 0 ){
                throw new Exception(String.format("Type stack size isn't wrong: %d", tsStack.size()));
            }
            stmt();
            match(OzScanner.lexSEMICOLON);
        }
    }

    void stmt() throws Exception {
        OzSymbols.Symbol symbol = null;
        int varType = 0;
        if( scanner.lookAheadLexeme == OzScanner.lexVARTYPE) {
            varType = varType();
            if( scanner.lookAheadLexeme == OzScanner.lexLSQUARE ){
                match(OzScanner.lexLSQUARE);
                match(OzScanner.lexRSQUARE);
                if( varType == OzScanner.VAR_TYPE_INT ){
                    varType = OzScanner.VAR_TYPE_INT_ARRAY;
                }
            } 
   
            symbol = newVariable(varType);

//            match(OzScanner.lexVARNAME, "variable name");
//            if( scanner.lookAheadLexeme == OzScanner.lexASSIGN){
//                if( varType == OzScanner.VAR_TYPE_INT_ARRAY ){
//                    assignArrayDefinition(symbol);
//                } else {
//                    assignExpression(symbol);
//                }
//           } else if( scanner.lookAheadLexeme == OzScanner.lexSEMICOLON ) {
//                   // empty
//           } else if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
//               OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
//           } else {
//               OzCompileError.expected(scanner, "'=' or ';'", scanner.loc);
//           }
        } else if( scanner.lookAheadLexeme == OzScanner.lexVARNAME) {
            symbol = variable();
            varType = symbol.varType;
//            match(OzScanner.lexVARNAME, "variable name");
//            if( scanner.lookAheadLexeme == OzScanner.lexASSIGN){
//                if( varType == OzScanner.VAR_TYPE_INT_ARRAY ){
//                    assignArrayDefinition(symbol);
//                } else {
//                    assignExpression(symbol);
//                }
//            } else if( scanner.lookAheadLexeme == OzScanner.lexSEMICOLON ) {
//                // empty
//            } else if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
//                OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
//            } else {
//                OzCompileError.expected(scanner, "'=' or ';'", scanner.loc);
//            }
        }

        assignExpr(symbol);
        /*
        match(OzScanner.lexVARNAME, "variable name");
        if( scanner.lookAheadLexeme == OzScanner.lexASSIGN){
            if( varType == OzScanner.VAR_TYPE_INT_ARRAY ){
                assignArrayDefinition(symbol);
            } else {
                assignExpression(symbol);
            }
       } else if( scanner.lookAheadLexeme == OzScanner.lexSEMICOLON ) {
               // empty
       } else if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
           OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
       } else {
           OzCompileError.expected(scanner, "'=' or ';'", scanner.loc);
       }
       */
    }


    private void assignExpr(OzSymbols.Symbol symbol) throws Exception {
        match(OzScanner.lexVARNAME, "variable name");
        if( scanner.lookAheadLexeme == OzScanner.lexASSIGN){
            if( symbol.varType == OzScanner.VAR_TYPE_INT_ARRAY ){
                assignArrayDefinition(symbol);
            } else {
                assignExpression(symbol);
            }
       } else if( scanner.lookAheadLexeme == OzScanner.lexSEMICOLON ) {
               // empty
       } else if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
           OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
       } else {
           OzCompileError.expected(scanner, "'=' or ';'", scanner.loc);
       }

    }

    /*
    private void declareVarAndAssignStmt(OzSymbols.Symbol symbol, int varType) throws Exception {

        match(OzScanner.lexVARNAME, "variable name");
        if( scanner.lookAheadLexeme == OzScanner.lexASSIGN){
            if( varType == OzScanner.VAR_TYPE_INT_ARRAY ){
                assignArrayDefinition(symbol);
            } else {
                assignExpression(symbol);
            }
        } else if( scanner.lookAheadLexeme == OzScanner.lexSEMICOLON ) {
                // empty
        } else if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
            OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
        } else {
            OzCompileError.expected(scanner, "'=' or ';'", scanner.loc);
        }
    }
    */

    private int varType() throws Exception {
        match(OzScanner.lexVARTYPE, "var type definition");
        int varType = scanner.varType;
        return varType;
    }

    private OzSymbols.Symbol newVariable(int varType) throws Exception {
        if( scanner.symbol.lexeme  == OzScanner.lexVARNAME &&
            scanner.symbol.varType != OzScanner.VAR_TYPE_UNDEF ){
            OzCompileError.message(scanner, "name '" + scanner.symbol.name + "' already defined",
            scanner.loc);
        }
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

//    public void assignStmt(OzSymbols.Symbol symbol) throws Exception {
//        match(OzScanner.lexVARNAME, "variable name");
//        assignExpression(symbol);
//    }
    
    private void assignExpression(OzSymbols.Symbol symbol) throws Exception {
        match(OzScanner.lexASSIGN, "'='");
        expression();
        assign(symbol);
    }

    private void assignArrayDefinition(OzSymbols.Symbol symbol) throws Exception {
        match(OzScanner.lexASSIGN, "'='");
        int varType = varType();

        if( symbol.varType == OzScanner.VAR_TYPE_INT_ARRAY &&
                   varType == OzScanner.VAR_TYPE_INT ){

            if( scanner.lookAheadLexeme == OzScanner.lexLSQUARE ){
                match(OzScanner.lexLSQUARE);

                if( scanner.lookAheadLexeme == OzScanner.lexNUMBER ){
                    match(OzScanner.lexNUMBER);
                } else if ( scanner.lookAheadLexeme == OzScanner.lexVARNAME ) {
                    match(OzScanner.lexVARNAME);
                }
                match(OzScanner.lexRSQUARE);
            }    
        } else {
            OzCompileError.message(scanner, "incompatible types", scanner.loc);
        }
    }

    private void assign(OzSymbols.Symbol symbol) throws Exception {
        genCodeConvertTypeAssign(tsStack.pop(), symbol.varType);
        emit(OzVm.OPCODE_PUSH, symbol);
        symbol.addRef(pc-4);
        switch(symbol.varType){
            case OzScanner.VAR_TYPE_INT:
            case OzScanner.VAR_TYPE_FLOAT:
                emit(OzVm.OPCODE_ASGN);
                break;
            case OzScanner.VAR_TYPE_BYTE:
            case OzScanner.VAR_TYPE_UBYTE:
                emit(OzVm.OPCODE_ASGNB);
                break;
            case OzScanner.VAR_TYPE_SHORT:
            case OzScanner.VAR_TYPE_USHORT:
                emit(OzVm.OPCODE_ASGNS);
                break;
            default:
    			OzCompileError.message(scanner, "Compilation error: assignment type error", scanner.loc);
	        }
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
                    switch(scanner.varType ){
                        case OzScanner.VAR_TYPE_INT:
                            emit(OzVm.OPCODE_PUSH, scanner.intNumber);
                            tsStack.push(OzScanner.VAR_TYPE_INT);
                            break;
                        case OzScanner.VAR_TYPE_FLOAT:
                            emit(OzVm.OPCODE_PUSH, scanner.floatNumber);
                            tsStack.push(OzScanner.VAR_TYPE_FLOAT);
                            break;
                        default:
                            break;    
                    }
                    break;
                case OzScanner.lexVARNAME:
                    OzSymbols.Symbol symbol = variable();
                    match(OzScanner.lexVARNAME, "variable name");                    
                    emit(OzVm.OPCODE_PUSH, symbol);
                    symbol.addRef(pc-4);
                    if( symbol.varType == OzScanner.VAR_TYPE_BYTE ||
                        symbol.varType == OzScanner.VAR_TYPE_UBYTE) {
                        emit(OzVm.OPCODE_EVALB);
                        if( symbol.varType == OzScanner.VAR_TYPE_BYTE ){
                            emitCommentListing("a signed bit extension for byte");
                            emit( OzVm.OPCODE_PUSH, 24 );
                            emit( OzVm.OPCODE_LSL );
                            emit( OzVm.OPCODE_PUSH, 24 );
                            emit( OzVm.OPCODE_ASR );
                            emitCommentListing("-");
                        }
                    } else
                    if(
                        symbol.varType == OzScanner.VAR_TYPE_SHORT ||
                        symbol.varType == OzScanner.VAR_TYPE_USHORT) {
                        emit(OzVm.OPCODE_EVALS);
                        if( symbol.varType == OzScanner.VAR_TYPE_SHORT ) {
                            emitCommentListing("a signed bit extension for short");
                            emit( OzVm.OPCODE_PUSH, 16 );
                            emit( OzVm.OPCODE_LSL );
                            emit( OzVm.OPCODE_PUSH, 16 );
                            emit( OzVm.OPCODE_ASR );
                            emitCommentListing("-");
                        }
                    } else {
                        emit(OzVm.OPCODE_EVAL);
                    }
                    // теперь все стало VAR_TYPE_INT и далее, вплоть до присваивания будет
                    // или VAR_TYPE_INT или VAR_TYPE_FLOAT
                    if( symbol.varType == OzScanner.VAR_TYPE_FLOAT) {
                        tsStack.push(OzScanner.VAR_TYPE_FLOAT);
                    } else {
                        tsStack.push(OzScanner.VAR_TYPE_INT);
                    }
                    break;
                case OzScanner.lexEOF:
                break ;   
                default:
                    OzCompileError.message(scanner, "unexpected lexeme", scanner.loc);    
            }
        }
        if( unaryMinus ) {
            int type = tsStack.pop();
            // имеем право проверять только на VAR_TYPE_INT или VAR_TYPE_FLOAT
            if( type == OzScanner.VAR_TYPE_INT) {
                emit(OzVm.OPCODE_NEG);
            } else {
                emit(OzVm.OPCODE_NEGF);
            }
            tsStack.push(type);
        }
    }

    private void add() throws Exception {
        match(OzScanner.lexPLUS, "'+'");
        term();
        int resultType = genCodeConvertTypeBinOp();
        switch ( resultType ){
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_ADD);
                break;
            case OzScanner.VAR_TYPE_FLOAT:    
                emit(OzVm.OPCODE_ADDF);
            break;
            default:
        }
        tsStack.push(resultType);
    }

    private void sub() throws Exception {
        match(OzScanner.lexMINUS, "'-'");
        term();
        int resultType = genCodeConvertTypeBinOp();
        switch (resultType){
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_SUB);
                break;
            case OzScanner.VAR_TYPE_FLOAT:    
                emit(OzVm.OPCODE_SUBF);
            break;
            default:
        }
        tsStack.push(resultType);
    }

    private void mul() throws Exception {
        match(OzScanner.lexMUL, "'*'");
        factor();
        int resultType = genCodeConvertTypeBinOp();
        switch ( resultType ){
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_MUL);
                break;
            case OzScanner.VAR_TYPE_FLOAT:    
                emit(OzVm.OPCODE_MULF);
            break;
            default:
        }
        tsStack.push( resultType );
    }

    private void div() throws Exception {
        match(OzScanner.lexDIV, "'/'");
        factor();
        int resultType = genCodeConvertTypeBinOp();
        switch ( resultType ){
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_DIV);
                break;
            case OzScanner.VAR_TYPE_FLOAT:    
                emit(OzVm.OPCODE_DIVF);
            break;
            default:
        }
        tsStack.push( resultType );
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

    private void emitCommentListing(final String comment){
        System.out.println(String.format("; %s", comment));
    }

    private void emitMnemonicListing(byte opcode){
        String mnemonic = OzAsm.getInstance().getMnemonic(opcode);
        System.out.print(String.format("        %s", mnemonic));
    }

    private void emitHexListing(byte opcode){
        System.out.print(String.format("0x%04X: 0x%02X", pc, opcode));
    }

    private void emitListing(byte opcode) {
        emitMnemonicListing(opcode);
        System.out.println();

        emitHexListing(opcode);
        System.out.println();
    }

    private void emitListing(byte opcode, final int arg) {
        emitMnemonicListing(opcode);
        System.out.print(String.format(" %d", arg));
        System.out.println();

        emitHexListing(opcode);
        System.out.println(String.format(" 0x%08X", arg));
    }

    private void emitListing(byte opcode, final float arg) {
        emitMnemonicListing(opcode);
        System.out.print(String.format(Locale.US, " %f", arg));
        System.out.println();

        int i = Float.floatToIntBits(arg);
        emitHexListing(opcode);
        System.out.println(String.format(" 0x%08X",  i));
    }

    private void emitListing(byte opcode, final Symbol sym) {
        emitMnemonicListing(opcode);
        System.out.print(String.format(" %s", sym.name));
        System.out.println();

        emitHexListing(opcode);
        System.out.println(String.format(" 0x%08X", sym.allocAddress));
    }

    private void emitMem(byte opcode){
        mem.add(opcode);
        pc++;
    }

    private void emitMem(byte opcode, int arg){
        emitMem(opcode);
        OzUtils.addIntToByteArray(mem, arg);
        pc += 4;
    }

    private void emitMem(byte opcode, float arg){
        emitMem(opcode, Float.floatToIntBits(arg));
    }

    private void emitMem(byte opcode, final Symbol sym){
        emitMem(opcode, sym.allocAddress);
    }

    public byte[] getProgramImage(){
        return mem.cut();
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
    
    private void genCodeConvertTypeAssign(int stackTopType, int varType){
        if( stackTopType != varType ){
            if( stackTopType == OzScanner.VAR_TYPE_INT && varType == OzScanner.VAR_TYPE_FLOAT ) {
                emitCommentListing("convert stack operand for assign");
                emit(OzVm.OPCODE_FLT);
                emitCommentListing("-");
            } else
            if (( varType == OzScanner.VAR_TYPE_INT   ||
                  varType == OzScanner.VAR_TYPE_SHORT ||
                  varType == OzScanner.VAR_TYPE_BYTE ) &&
                stackTopType == OzScanner.VAR_TYPE_FLOAT ) {
                emitCommentListing("convert stack operand for assign");
                emit(OzVm.OPCODE_INT);
                emitCommentListing("-");
            } else {
            }
        }
    }

    private int genCodeConvertTypeBinOp(){
        int typeOfTop    = tsStack.pop();
        int typeOfSubTop = tsStack.pop();
        if( typeOfTop != typeOfSubTop ){
            if( typeOfTop == OzScanner.VAR_TYPE_FLOAT ){
                emitCommentListing("convert a sub top stack operand for the binary operation");
                emit(OzVm.OPCODE_SWAP);
                emit(OzVm.OPCODE_FLT);
                emit(OzVm.OPCODE_SWAP);
                emitCommentListing("-");
                return OzScanner.VAR_TYPE_FLOAT;
            } else if ( typeOfSubTop == OzScanner.VAR_TYPE_FLOAT ){
                emitCommentListing("convert a top stack operand for the binary operation");
                emit(OzVm.OPCODE_FLT);
                emitCommentListing("-");
                return OzScanner.VAR_TYPE_FLOAT;
            }
        }
        return typeOfTop;
    }

    public class ByteArray{
        
        final static int CHUNK_SIZE = 64;
        byte[] mem = new byte[CHUNK_SIZE];
        int used = 0;

        void add(byte b){
            if( used == mem.length ){
                byte[] tmp = new byte[mem.length + CHUNK_SIZE];
                System.arraycopy(mem, 0, tmp, 0, mem.length);
                mem = tmp;
            }
            mem[used++] = b;
        }

        byte[] cut(){
            byte[] tmp = new byte[used];
            System.arraycopy(mem, 0, tmp, 0, used);
            return tmp;
        }
    }
}