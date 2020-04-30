package com.borsoftlab.oZee;

public class OzParser{

    OzScanner scanner;
    int aheadLexeme = 0;
    byte[] mem = new byte [12];
    int pc = 0;

    /*
    * Type maintenance stack
    */
    private IntStack typeStack = new IntStack( 32 );


    public OzParser(final OzScanner scanner){
        this.scanner = scanner;
    }
    
    public void compile(){
        pc = 0;
        scanner.nextLexeme();
        stmtList();
        System.out.println("\n" + scanner.text.loc.lexemeCount + " lexemes processed");
    }

    void stmtList(){
        while( scanner.lookAheadLexeme != OzScanner.lexEOF ){
    //        System.out.print(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;  ");
    //        System.out.printf("Maintenance type stack size is: %d\n", typeStack.size());
            stmt();
            match(OzScanner.lexSEMICOLON, "';'");
        }
    }

    void stmt(){
        if( scanner.lookAheadLexeme == OzScanner.lexVARTYPE) {
            declareVar();
        }
        else if( scanner.lookAheadLexeme == OzScanner.lexNAME) {
            assignStmt();
        }
        else {
            expression(); // it will be not always
        }
    }

    private void declareVar() {
        int type = varType();
        OzSymbols.Symbol symbol = newVariable(type);
        if( scanner.lookAheadLexeme == OzScanner.lexASSIGN){
            eatLexeme();
            assign(symbol);
        } else if( scanner.lookAheadLexeme == OzScanner.lexSEMICOLON ) {
            // empty
        } else {
            OzCompileError.expected(scanner.text, "expession");
        }
    }

    private int varType(){
        int type = scanner.symbol.varType;
        scanner.nextLexeme();
        return type;
    }

    private OzSymbols.Symbol newVariable(int type){
        OzSymbols.Symbol symbol = variable();
        symbol.setType(type);
        // allocateVariable(symbol);
        return symbol;
    }

    private OzSymbols.Symbol variable() {
        match(OzScanner.lexNAME, "identifier");
        return scanner.symbol;
    }

    public void assignStmt() {
        assign(variable());
    }

    
    private void assign(OzSymbols.Symbol symbol) {
        expression();
        emit("push @" + symbol.name);
        emit("save");
        //emitPullDir(symbol);
    }
    

    public void expression() {
        term();
        while(true) {
            switch( scanner.lookAheadLexeme ){
                case OzScanner.lexPLUS:
                    scanner.nextLexeme();
                    sum();
                    break;
                case OzScanner.lexMINUS:
                    scanner.nextLexeme();
                    sub();
                    break;
                default:
                    return;
            }
        }
    }

    private void sum() {
        term();
        emit("add");
//        System.out.printf("Maintenance type stack size is: %d\n", typeStack.size());
//        emitArithmOpCode(MachineCode.SUMF, MachineCode.SUMI);
    }

    private void sub() {
        term();
        emit("sub");
//        emitArithmOpCode(MachineCode.SUBF, MachineCode.SUBI);
    }

    private void term(){
        factor();
        while(true) {
            switch( scanner.lookAheadLexeme ){
                case OzScanner.lexMUL:
                    scanner.nextLexeme();
                    mul();
                    break;
                case OzScanner.lexDIV:
                    scanner.nextLexeme();
                    div();
                    break;
                default:
                    return;
            }
        }
    }    


    private void div() {
        factor();
        emit("div");

//        emitArithmOpCode(MachineCode.DIVF, MachineCode.DIVI);
    }

    private void mul() {
        factor();
        emit("mul");

//        emitArithmOpCode(MachineCode.MULF, MachineCode.MULI);
    }

    private void factor() {
        boolean unaryMinus = false;
        if( scanner.lookAheadLexeme == OzScanner.lexMINUS){
            scanner.nextLexeme();;
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
                    typeStack.push(scanner.numberType);
                    if( scanner.numberType == OzScanner.VARTYPE_INT)
                        emit("push " + scanner.intNumber);
                    else
                        emit("push " + scanner.floatNumber);    
                    // emitPushImm(scanner.getNumberAsInt());
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
                case OzScanner.lexNAME:
                    scanner.nextLexeme();
                    OzSymbols.Symbol symbol = scanner.symbol;
                    int symbolType = symbol.varType;
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
                    typeStack.push(symbolType);
                    emit("push @" + symbol.name);
                    emit("eval ");
//                    emitPushDir(symbol);
                    break;
                case OzScanner.lexEOF:
                break ;   
                default:
                    OzCompileError.expected(scanner.text, "expression");    
            }
        }
        if( unaryMinus ) {
  //          emitNegOpCode(MachineCode.NEGF, MachineCode.NEGI4);
        }
    }



    private void emit(String cmd) {
        System.out.println(cmd);
    }

    public byte[] getExecMemModule() {
        final int value = 1234567890;
        mem[pc++] = OzVm.OPCODE_PUSH;
        OzUtils.storeIntToByteArray(mem, pc, value);
        pc += 4;
        mem[pc++] = OzVm.OPCODE_STOP;
        return mem;
    }

    private void eatLexeme() {
        scanner.nextLexeme();
    }     

    private void match(final int lexeme, final String msg) {
        if( scanner.lookAheadLexeme == lexeme ){
            scanner.nextLexeme();
        } else {
            OzCompileError.expected(scanner.text, msg);
        }
    }     
}