package com.borsoftlab.ozee;

public class OzParser{

    OzScanner scanner;
    int aheadLexeme = 0;
    byte[] mem = new byte [12];
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
        // allocateVariable(symbol);
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
//          emit("push @" + symbol.name);
//          emit("assign");
        emitOpcode(OzVm.OPCODE_PUSH, symbol.name);
        emitOpcode(OzVm.OPCODE_ASGN);
        //emitPullDir(symbol);
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
  
        emitOpcode(OzVm.OPCODE_ADD);
//        emit("add");


//        System.out.printf("Maintenance type stack size is: %d\n", typeStack.size());
//        emitArithmeticOpCode(MachineCode.SUMF, MachineCode.SUMI);
    }

    private void sub() throws Exception {
        match(OzScanner.lexMINUS, "'-'");
        term();
        
        emitOpcode(OzVm.OPCODE_SUB);

//        emit("sub");
//        emitArithmeticOpCode(MachineCode.SUBF, MachineCode.SUBI);
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
        emitOpcode(OzVm.OPCODE_DIV);

//        emit("div");

//     emitArithmeticOpCode(MachineCode.DIVF, MachineCode.DIVI);
    }

    private void mul() throws Exception {
        match(OzScanner.lexMUL, "'*'");
        factor();
        emitOpcode(OzVm.OPCODE_MUL);

//        emit("mul");

//        emitArithmeticOpCode(MachineCode.MULF, MachineCode.MULI);
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
                        emitOpcode(OzVm.OPCODE_PUSH, Integer.toString(scanner.intNumber));

                     //   emit("push " + scanner.intNumber);
                    }
                    else {
                        emitOpcode(OzVm.OPCODE_PUSH, Float.toString(scanner.floatNumber));

//                        emit("push " + scanner.floatNumber);    
                    // emitPushImm(scanner.getNumberAsInt());
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
                    emitOpcode(OzVm.OPCODE_PUSH, symbol.name);

//                    emit("push @" + symbol.name);
                    emitOpcode(OzVm.OPCODE_EVAL);

//                    emit("eval ");
//                    emitPushDir(symbol);
                    break;
                case OzScanner.lexEOF:
                break ;   
                default:
                    OzCompileError.message(scanner, "unexpected lexeme", scanner.loc);    
            }
        }
        if( unaryMinus ) {
            emitOpcode(OzVm.OPCODE_NEG);

//            emit("neg");

  //          emitNegOpCode(MachineCode.NEGF, MachineCode.NEGI4);
        }
    }

    private void emitOpcode(int opcode) {
        String mnemonic = OzAsm.getInstance().getMnemonic(opcode);
        System.out.println(mnemonic);
    }

    private void emitOpcode(int opcode, final String arg) {
        String mnemonic = OzAsm.getInstance().getMnemonic(opcode);
        System.out.println(mnemonic + " " + arg);
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